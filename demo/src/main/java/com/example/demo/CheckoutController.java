package com.example.demo;

import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/checkout")
@CrossOrigin
public class CheckoutController {

    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final TicketCategoryRepository ticketCategoryRepository;
    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;
    private final TicketRepository ticketRepository;
    private final NotificationRepository notificationRepository;
    private final OtpChallengeService otpChallengeService;
    private final EmailDeliveryService emailDeliveryService;
    private final TicketPackageService ticketPackageService;

    public CheckoutController(
            UserRepository userRepository,
            EventRepository eventRepository,
            TicketCategoryRepository ticketCategoryRepository,
            BookingRepository bookingRepository,
            PaymentRepository paymentRepository,
            TicketRepository ticketRepository,
            NotificationRepository notificationRepository,
            OtpChallengeService otpChallengeService,
            EmailDeliveryService emailDeliveryService,
            TicketPackageService ticketPackageService
    ) {
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
        this.ticketCategoryRepository = ticketCategoryRepository;
        this.bookingRepository = bookingRepository;
        this.paymentRepository = paymentRepository;
        this.ticketRepository = ticketRepository;
        this.notificationRepository = notificationRepository;
        this.otpChallengeService = otpChallengeService;
        this.emailDeliveryService = emailDeliveryService;
        this.ticketPackageService = ticketPackageService;
    }

    @PostMapping("/start")
    @Transactional
    public OtpChallengeResponse startCheckout(@RequestBody CheckoutStartRequest request) {
        User customer = userRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Customer not found"));
        Event event = eventRepository.findById(request.getEventId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found"));
        TicketCategory category = ticketCategoryRepository.findById(request.getTicketCategoryId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ticket category not found"));

        if (!category.getEventId().equals(event.getEventId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ticket category does not belong to the selected event");
        }
        if (category.getName() != null && category.getName().trim().toLowerCase().startsWith("internal usage -")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Internal usage categories are not available for public checkout.");
        }
        if (request.getQuantity() == null || request.getQuantity() <= 0 || request.getQuantity() > 10) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ticket quantity must be between 1 and 10.");
        }
        if (request.getQuantity() > category.getAvailableQty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Requested quantity exceeds remaining tickets.");
        }
        if (!"published".equalsIgnoreCase(event.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "This event is not available for booking.");
        }
        if (category.getSaleStartDate() != null && category.getSaleStartDate().after(Timestamp.from(Instant.now()))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ticket sales for this category have not started yet.");
        }
        if (event.getEndTime() != null && !event.getEndTime().isAfter(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "This event has already ended.");
        }

        Booking booking = new Booking();
        booking.setBookingId(UUID.randomUUID().toString());
        booking.setUserId(customer.getUser_id());
        booking.setEventId(event.getEventId());
        booking.setQuantity(request.getQuantity());
        booking.setTotalCost(category.getPrice().multiply(BigDecimal.valueOf(request.getQuantity())));
        booking.setPaymentStatus("otp_pending");
        booking.setBookingTimestamp(Timestamp.from(Instant.now()));
        bookingRepository.save(booking);

        Payment payment = new Payment();
        payment.setPaymentId(UUID.randomUUID().toString());
        payment.setBookingId(booking.getBookingId());
        payment.setAmount(booking.getTotalCost());
        payment.setMethod(request.getPaymentMethod() == null || request.getPaymentMethod().isBlank() ? "upi" : request.getPaymentMethod());
        payment.setStatus("otp_pending");
        payment.setTransactionTimestamp(Timestamp.from(Instant.now()));
        paymentRepository.save(payment);

        OtpChallengeResponse response = otpChallengeService.issuePaymentOtp(
                OtpChallengeService.PURPOSE_CUSTOMER_PAYMENT,
                customer.getEmail(),
                customer.getUser_id(),
                payment.getPaymentId(),
                Map.of(
                        "bookingId", booking.getBookingId(),
                        "paymentId", payment.getPaymentId(),
                        "categoryId", category.getCategoryId(),
                        "quantity", request.getQuantity(),
                        "eventId", event.getEventId()
                )
        );
        response.setBookingId(booking.getBookingId());
        response.setPaymentId(payment.getPaymentId());
        response.setMessage("Payment OTP sent to your email.");
        return response;
    }

    @PostMapping("/confirm")
    @Transactional
    public CheckoutConfirmResponse confirmCheckout(@RequestBody CheckoutConfirmRequest request) {
        AuthOtpChallenge challenge = otpChallengeService.verifyAndConsume(
                request.getChallengeId(),
                OtpChallengeService.PURPOSE_CUSTOMER_PAYMENT,
                request.getOtpCode()
        );
        Map<String, Object> payload = otpChallengeService.readPayload(challenge);

        String bookingId = String.valueOf(payload.getOrDefault("bookingId", request.getBookingId()));
        String paymentId = String.valueOf(payload.getOrDefault("paymentId", request.getPaymentId()));
        String categoryId = String.valueOf(payload.getOrDefault("categoryId", ""));
        int quantity = Integer.parseInt(String.valueOf(payload.getOrDefault("quantity", "0")));

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Booking not found"));
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Payment not found"));
        TicketCategory category = ticketCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ticket category not found"));
        Event event = eventRepository.findById(booking.getEventId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found"));
        User customer = userRepository.findById(booking.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Customer not found"));

        if ("success".equalsIgnoreCase(payment.getStatus())) {
            CheckoutConfirmResponse response = new CheckoutConfirmResponse();
            response.setBookingId(booking.getBookingId());
            response.setPaymentId(payment.getPaymentId());
            response.setTicketIds(ticketRepository.findByBookingId(booking.getBookingId()).stream().map(Ticket::getTicketId).toList());
            return response;
        }

        if (quantity > category.getAvailableQty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Requested quantity exceeds remaining tickets.");
        }

        List<String> ticketIds = new ArrayList<>();
        for (int index = 0; index < quantity; index += 1) {
            Ticket ticket = new Ticket();
            String ticketId = UUID.randomUUID().toString();
            ticket.setTicketId(ticketId);
            ticket.setQrCode(UUID.randomUUID().toString() + "-" + ticketId);
            ticket.setCategoryId(category.getCategoryId());
            ticket.setBookingId(booking.getBookingId());
            ticket.setStatus(TicketStatus.booked);
            ticketRepository.save(ticket);
            ticketIds.add(ticketId);
        }

        payment.setStatus("success");
        payment.setTransactionTimestamp(Timestamp.from(Instant.now()));
        paymentRepository.save(payment);

        booking.setPaymentStatus("success");
        bookingRepository.save(booking);

        Notification notification = new Notification();
        notification.setNotificationId(UUID.randomUUID().toString());
        notification.setType("payment");
        notification.setMessage(quantity + " ticket(s) booked for " + event.getName() + ".");
        notification.setSentAt(Timestamp.from(Instant.now()));
        notification.setEventId(event.getEventId());
        notification.setUserId(customer.getUser_id());
        notification.setAudienceScope("DIRECT");
        notification.setMetadata("{}");
        notificationRepository.save(notification);

        List<Ticket> issuedTickets = ticketRepository.findByBookingId(booking.getBookingId());
        Map<String, TicketCategory> categoryMap = Map.of(category.getCategoryId(), category);
        int zipSizeBytes = ticketPackageService.calculateAttachmentSizeBytes(booking.getBookingId(), issuedTickets, categoryMap);
        String emailBody = "Your booking is confirmed.\n\nEvent: " + event.getName()
                + "\nBooking ID: " + booking.getBookingId()
                + "\nTickets: " + String.join(", ", ticketIds);

        if (zipSizeBytes <= 20 * 1024 * 1024) {
            emailDeliveryService.sendPlainText(
                    customer.getEmail(),
                    "Your EMTS tickets for " + event.getName(),
                    emailBody + "\n\nYour ticket QR ZIP is attached.",
                    List.of(ticketPackageService.buildTicketZipAttachment(booking.getBookingId(), issuedTickets, categoryMap))
            );
        } else {
            emailDeliveryService.sendPlainText(
                    customer.getEmail(),
                    "Your EMTS tickets for " + event.getName(),
                    emailBody + "\n\nYour ticket ZIP was too large to email. Please open the EMTS portal to download your QR files."
            );
        }

        CheckoutConfirmResponse response = new CheckoutConfirmResponse();
        response.setBookingId(booking.getBookingId());
        response.setPaymentId(payment.getPaymentId());
        response.setTicketIds(ticketIds);
        return response;
    }
}

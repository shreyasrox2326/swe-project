package com.example.demo;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/corporate-booking-requests")
@CrossOrigin
public class CorporateBookingRequestController {

    private final CorporateBookingRequestRepository requestRepo;
    private final CorporateBookingRequestItemRepository itemRepo;
    private final CorporateRepository corporateRepo;
    private final OrganizerRepository organizerRepo;
    private final EventRepository eventRepo;
    private final TicketCategoryRepository categoryRepo;
    private final BookingRepository bookingRepo;
    private final PaymentRepository paymentRepo;
    private final TicketRepository ticketRepo;
    private final NotificationRepository notificationRepo;

    public CorporateBookingRequestController(
            CorporateBookingRequestRepository requestRepo,
            CorporateBookingRequestItemRepository itemRepo,
            CorporateRepository corporateRepo,
            OrganizerRepository organizerRepo,
            EventRepository eventRepo,
            TicketCategoryRepository categoryRepo,
            BookingRepository bookingRepo,
            PaymentRepository paymentRepo,
            TicketRepository ticketRepo,
            NotificationRepository notificationRepo
    ) {
        this.requestRepo = requestRepo;
        this.itemRepo = itemRepo;
        this.corporateRepo = corporateRepo;
        this.organizerRepo = organizerRepo;
        this.eventRepo = eventRepo;
        this.categoryRepo = categoryRepo;
        this.bookingRepo = bookingRepo;
        this.paymentRepo = paymentRepo;
        this.ticketRepo = ticketRepo;
        this.notificationRepo = notificationRepo;
    }

    @GetMapping
    public List<CorporateBookingRequest> getAll() {
        return requestRepo.findAll();
    }

    @GetMapping("/{requestId}")
    public CorporateBookingRequestView getById(@PathVariable String requestId) {
        CorporateBookingRequest request = requestRepo.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Corporate booking request not found"));
        return new CorporateBookingRequestView(request, itemRepo.findByRequestId(requestId));
    }

    @GetMapping("/corporate/{corporateUserId}")
    public List<CorporateBookingRequestView> getByCorporate(@PathVariable String corporateUserId) {
        return requestRepo.findByCorporateUserIdOrderByCreatedAtDesc(corporateUserId).stream()
                .map(request -> new CorporateBookingRequestView(request, itemRepo.findByRequestId(request.getRequestId())))
                .toList();
    }

    @GetMapping("/organizer/{organizerUserId}")
    public List<CorporateBookingRequestView> getByOrganizer(@PathVariable String organizerUserId) {
        return requestRepo.findByOrganizerUserIdOrderByCreatedAtDesc(organizerUserId).stream()
                .map(request -> new CorporateBookingRequestView(request, itemRepo.findByRequestId(request.getRequestId())))
                .toList();
    }

    @PostMapping
    @Transactional
    public CorporateBookingRequestView create(@RequestBody CreateCorporateBookingRequest payload) {
        if (payload.getItems() == null || payload.getItems().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "At least one ticket category is required.");
        }

        corporateRepo.findById(payload.getCorporateUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Corporate profile not found"));

        Event event = eventRepo.findById(payload.getEventId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found"));
        if (!"published".equalsIgnoreCase(event.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Corporate requests can only be created for published events");
        }

        organizerRepo.findById(event.getOrganizerId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Organizer profile not found"));

        int totalQuantity = 0;
        for (CorporateRequestItemInput itemInput : payload.getItems()) {
            TicketCategory category = categoryRepo.findById(itemInput.getCategoryId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ticket category not found"));
            if (!category.getEventId().equals(payload.getEventId())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ticket category does not belong to the selected event");
            }
            if (itemInput.getQuantity() == null || itemInput.getQuantity() <= 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Requested quantity must be positive");
            }
            if (itemInput.getQuantity() > category.getAvailableQty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Requested quantity exceeds current available inventory");
            }
            totalQuantity += itemInput.getQuantity();
        }

        CorporateBookingRequest request = new CorporateBookingRequest();
        request.setRequestId(payload.getRequestId() == null || payload.getRequestId().isBlank() ? UUID.randomUUID().toString() : payload.getRequestId());
        request.setCorporateUserId(payload.getCorporateUserId());
        request.setOrganizerUserId(event.getOrganizerId());
        request.setEventId(payload.getEventId());
        request.setStatus("submitted");
        request.setRequestedTotalQty(totalQuantity);
        request.setCorporateNote(payload.getCorporateNote());
        requestRepo.save(request);

        for (CorporateRequestItemInput itemInput : payload.getItems()) {
            CorporateBookingRequestItem item = new CorporateBookingRequestItem();
            item.setRequestItemId(UUID.randomUUID().toString());
            item.setRequestId(request.getRequestId());
            item.setCategoryId(itemInput.getCategoryId());
            item.setRequestedQty(itemInput.getQuantity());
            item.setReservedQty(0);
            itemRepo.save(item);
        }

        createDirectNotification(
                event.getOrganizerId(),
                payload.getEventId(),
                "corporate_request_submitted",
                "A new corporate booking request has been submitted for event " + event.getName() + "."
        );

        return new CorporateBookingRequestView(request, itemRepo.findByRequestId(request.getRequestId()));
    }

    @PostMapping("/{requestId}/approve")
    @Transactional
    public CorporateBookingRequestView approve(@PathVariable String requestId, @RequestBody ApproveCorporateBookingRequest payload) {
        CorporateBookingRequest request = requestRepo.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Corporate booking request not found"));

        if (!"submitted".equalsIgnoreCase(request.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only submitted requests can be approved");
        }
        if (payload.getItems() == null || payload.getItems().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Approved ticket breakdown is required");
        }
        if (payload.getExpiresAt() == null || payload.getExpiresAt().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Offer deadline is required");
        }

        List<CorporateBookingRequestItem> existingItems = itemRepo.findByRequestId(requestId);
        BigDecimal offeredTotal = BigDecimal.ZERO;
        int approvedTotalQty = 0;
        LocalDateTime expiresAt;
        try {
            expiresAt = LocalDateTime.parse(payload.getExpiresAt()).truncatedTo(ChronoUnit.MINUTES);
        } catch (Exception exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Offer deadline is invalid");
        }
        LocalDateTime now = LocalDateTime.now();
        if (!expiresAt.isAfter(now)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Offer deadline must be in the future");
        }
        if (ChronoUnit.MINUTES.between(now, expiresAt) < 15) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Offer deadline must be at least 15 minutes from now");
        }

        for (CorporateRequestItemInput approvedItem : payload.getItems()) {
            CorporateBookingRequestItem item = existingItems.stream()
                    .filter(entry -> entry.getCategoryId().equals(approvedItem.getCategoryId()))
                    .findFirst()
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Approved category does not exist in the request"));

            TicketCategory category = categoryRepo.findById(item.getCategoryId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ticket category not found"));

            int approvedQty = approvedItem.getQuantity() == null ? 0 : approvedItem.getQuantity();
            if (approvedQty <= 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Approved quantity must be positive");
            }
            if (approvedQty > item.getRequestedQty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Approved quantity cannot exceed requested quantity");
            }
            if (approvedQty > category.getAvailableQty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Approved quantity exceeds currently available inventory");
            }
            if (approvedItem.getOfferedUnitPrice() == null || approvedItem.getOfferedUnitPrice().compareTo(BigDecimal.ZERO) < 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Offered unit price must be zero or positive");
            }

            category.setAvailableQty(category.getAvailableQty() - approvedQty);
            categoryRepo.save(category);

            item.setApprovedQty(approvedQty);
            item.setReservedQty(approvedQty);
            item.setOfferedUnitPrice(approvedItem.getOfferedUnitPrice());
            itemRepo.save(item);

            approvedTotalQty += approvedQty;
            offeredTotal = offeredTotal.add(approvedItem.getOfferedUnitPrice().multiply(BigDecimal.valueOf(approvedQty)));
        }

        request.setOrganizerNote(payload.getOrganizerNote());
        request.setRequestedTotalQty(approvedTotalQty);
        request.setOfferedTotalAmount(offeredTotal);
        request.setStatus("approved_pending_payment");
        request.setApprovedAt(new Timestamp(System.currentTimeMillis()));
        request.setDecisionAt(new Timestamp(System.currentTimeMillis()));
        request.setExpiresAt(Timestamp.valueOf(expiresAt));
        requestRepo.save(request);

        createDirectNotification(
                request.getCorporateUserId(),
                request.getEventId(),
                "corporate_request_approved",
                "Your corporate request has been approved. Review the offer and complete payment before it expires."
        );

        return new CorporateBookingRequestView(request, itemRepo.findByRequestId(requestId));
    }

    @PostMapping("/{requestId}/reject")
    @Transactional
    public CorporateBookingRequestView reject(@PathVariable String requestId, @RequestBody ApproveCorporateBookingRequest payload) {
        CorporateBookingRequest request = requestRepo.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Corporate booking request not found"));

        if (!"submitted".equalsIgnoreCase(request.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only submitted requests can be rejected");
        }

        request.setStatus("rejected");
        request.setOrganizerNote(payload.getOrganizerNote());
        request.setDecisionAt(new Timestamp(System.currentTimeMillis()));
        requestRepo.save(request);

        createDirectNotification(
                request.getCorporateUserId(),
                request.getEventId(),
                "corporate_request_rejected",
                "Your corporate booking request was rejected by the organizer."
        );

        return new CorporateBookingRequestView(request, itemRepo.findByRequestId(requestId));
    }

    @PostMapping("/{requestId}/cancel")
    @Transactional
    public CorporateBookingRequestView cancel(@PathVariable String requestId) {
        CorporateBookingRequest request = requestRepo.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Corporate booking request not found"));

        if ("approved_pending_payment".equalsIgnoreCase(request.getStatus())) {
            releaseReservedInventory(requestId);
        }

        request.setStatus("cancelled");
        request.setCancelledAt(new Timestamp(System.currentTimeMillis()));
        requestRepo.save(request);

        createDirectNotification(
                request.getOrganizerUserId(),
                request.getEventId(),
                "corporate_request_cancelled",
                "A corporate client cancelled an open booking request."
        );

        return new CorporateBookingRequestView(request, itemRepo.findByRequestId(requestId));
    }

    @PostMapping("/{requestId}/expire")
    @Transactional
    public CorporateBookingRequestView expire(@PathVariable String requestId) {
        CorporateBookingRequest request = requestRepo.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Corporate booking request not found"));

        if (!"approved_pending_payment".equalsIgnoreCase(request.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only approved pending payment requests can expire");
        }

        releaseReservedInventory(requestId);
        request.setStatus("expired");
        request.setDecisionAt(new Timestamp(System.currentTimeMillis()));
        request.setExpiresAt(null);
        requestRepo.save(request);

        createDirectNotification(
                request.getCorporateUserId(),
                request.getEventId(),
                "corporate_request_expired",
                "Your corporate booking request expired before payment was completed."
        );

        return new CorporateBookingRequestView(request, itemRepo.findByRequestId(requestId));
    }

    @PostMapping("/{requestId}/pay")
    @Transactional
    public CorporateBookingRequestView pay(@PathVariable String requestId, @RequestBody CorporatePaymentRequest payload) {
        CorporateBookingRequest request = requestRepo.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Corporate booking request not found"));

        if (!"approved_pending_payment".equalsIgnoreCase(request.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "This request is not ready for payment");
        }
        if (request.getExpiresAt() == null || request.getExpiresAt().before(new Timestamp(System.currentTimeMillis()))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "This payment window has expired");
        }

        List<CorporateBookingRequestItem> items = itemRepo.findByRequestId(requestId);
        Event event = eventRepo.findById(request.getEventId())
                .orElseThrow(() -> new RuntimeException("Event not found"));

        Booking booking = new Booking();
        booking.setBookingId(UUID.randomUUID().toString());
        booking.setUserId(request.getCorporateUserId());
        booking.setEventId(request.getEventId());
        booking.setQuantity(items.stream().mapToInt(item -> item.getReservedQty() == null ? 0 : item.getReservedQty()).sum());
        booking.setTotalCost(request.getOfferedTotalAmount());
        booking.setPaymentStatus("success");
        booking.setBookingTimestamp(new Timestamp(System.currentTimeMillis()));
        bookingRepo.save(booking);

        Payment payment = new Payment();
        payment.setPaymentId(UUID.randomUUID().toString());
        payment.setBookingId(booking.getBookingId());
        payment.setAmount(request.getOfferedTotalAmount());
        payment.setMethod(payload.getMethod() == null || payload.getMethod().isBlank() ? "corporate" : payload.getMethod());
        payment.setStatus("success");
        payment.setTransactionTimestamp(new Timestamp(System.currentTimeMillis()));
        paymentRepo.save(payment);

        for (CorporateBookingRequestItem item : items) {
            for (int index = 0; index < item.getReservedQty(); index++) {
                Ticket ticket = new Ticket();
                String ticketId = UUID.randomUUID().toString();
                ticket.setTicketId(ticketId);
                ticket.setQrCode(UUID.randomUUID().toString());
                ticket.setCategoryId(item.getCategoryId());
                ticket.setBookingId(booking.getBookingId());
                ticket.setStatus(TicketStatus.booked);
                ticketRepo.save(ticket);
            }

            item.setReservedQty(0);
            itemRepo.save(item);
        }

        request.setStatus("paid");
        request.setPaidAt(new Timestamp(System.currentTimeMillis()));
        request.setBookingId(booking.getBookingId());
        request.setPaymentId(payment.getPaymentId());
        request.setExpiresAt(null);
        requestRepo.save(request);

        createDirectNotification(
                request.getCorporateUserId(),
                request.getEventId(),
                "corporate_payment_success",
                "Corporate payment completed successfully for " + event.getName() + "."
        );
        createDirectNotification(
                request.getOrganizerUserId(),
                request.getEventId(),
                "corporate_payment_success",
                "A corporate booking payment was completed for " + event.getName() + "."
        );

        return new CorporateBookingRequestView(request, itemRepo.findByRequestId(requestId));
    }

    private void releaseReservedInventory(String requestId) {
        List<CorporateBookingRequestItem> items = itemRepo.findByRequestId(requestId);
        for (CorporateBookingRequestItem item : items) {
            if (item.getReservedQty() != null && item.getReservedQty() > 0) {
                TicketCategory category = categoryRepo.findById(item.getCategoryId())
                        .orElseThrow(() -> new RuntimeException("Ticket category not found"));
                category.setAvailableQty(category.getAvailableQty() + item.getReservedQty());
                categoryRepo.save(category);
                item.setReservedQty(0);
                itemRepo.save(item);
            }
        }
    }

    private void createDirectNotification(String userId, String eventId, String type, String message) {
        Notification notification = new Notification();
        notification.setNotificationId(UUID.randomUUID().toString());
        notification.setType(type);
        notification.setMessage(message);
        notification.setSentAt(new Timestamp(System.currentTimeMillis()));
        notification.setEventId(eventId);
        notification.setUserId(userId);
        notification.setAudienceScope("DIRECT");
        notification.setMetadata("{}");
        notificationRepo.save(notification);
    }
}

package com.example.demo;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
public class InternalTicketService {

    private static final String INTERNAL_PREFIX = "Internal Usage - ";

    private final EventRepository eventRepo;
    private final UserRepository userRepo;
    private final TicketCategoryRepository categoryRepo;
    private final BookingRepository bookingRepo;
    private final PaymentRepository paymentRepo;
    private final TicketRepository ticketRepo;
    private final NotificationRepository notificationRepo;

    public InternalTicketService(
            EventRepository eventRepo,
            UserRepository userRepo,
            TicketCategoryRepository categoryRepo,
            BookingRepository bookingRepo,
            PaymentRepository paymentRepo,
            TicketRepository ticketRepo,
            NotificationRepository notificationRepo
    ) {
        this.eventRepo = eventRepo;
        this.userRepo = userRepo;
        this.categoryRepo = categoryRepo;
        this.bookingRepo = bookingRepo;
        this.paymentRepo = paymentRepo;
        this.ticketRepo = ticketRepo;
        this.notificationRepo = notificationRepo;
    }

    public boolean isInternalCategoryName(String value) {
        return value != null && value.trim().toLowerCase().startsWith(INTERNAL_PREFIX.toLowerCase());
    }

    private String toInternalCategoryName(String type) {
        String normalized = (type == null || type.isBlank()) ? "Internal" : type.trim();
        return INTERNAL_PREFIX + normalized;
    }

    @Transactional
    public InternalTicketIssueResponse issueInternalTickets(String eventId, String recipientUserId, String type, int quantity, String createdByUserId) {
        if (quantity <= 0) {
            throw new RuntimeException("Quantity must be positive");
        }

        Event event = eventRepo.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));
        if ("deleted".equalsIgnoreCase(event.getStatus()) || "cancelled".equalsIgnoreCase(event.getStatus())) {
            throw new RuntimeException("Internal tickets cannot be issued for this event");
        }

        User recipientUser = userRepo.findById(recipientUserId)
                .orElseThrow(() -> new RuntimeException("Recipient user not found"));

        List<TicketCategory> eventCategories = categoryRepo.findByEventId(eventId);
        TicketCategory sourceCategory = eventCategories.stream()
                .filter(category -> !isInternalCategoryName(category.getName()))
                .filter(category -> category.getAvailableQty() >= quantity)
                .max(Comparator.comparingInt(TicketCategory::getAvailableQty))
                .orElseThrow(() -> new RuntimeException("No public ticket inventory is available to allocate internal usage tickets."));

        String internalCategoryName = toInternalCategoryName(type);
        TicketCategory internalCategory = eventCategories.stream()
                .filter(category -> internalCategoryName.equalsIgnoreCase(category.getName()))
                .findFirst()
                .orElseGet(() -> {
                    TicketCategory created = new TicketCategory();
                    created.setCategoryId("category-" + UUID.randomUUID());
                    created.setEventId(eventId);
                    created.setName(internalCategoryName);
                    created.setPrice(BigDecimal.ZERO);
                    created.setTotalQty(0);
                    created.setAvailableQty(0);
                    created.setSaleStartDate(new Timestamp(System.currentTimeMillis()));
                    return created;
                });

        sourceCategory.setAvailableQty(sourceCategory.getAvailableQty() - quantity);
        categoryRepo.save(sourceCategory);

        internalCategory.setTotalQty(internalCategory.getTotalQty() + quantity);
        internalCategory.setAvailableQty(internalCategory.getAvailableQty() + quantity);
        internalCategory.setPrice(BigDecimal.ZERO);
        internalCategory.setSaleStartDate(new Timestamp(System.currentTimeMillis()));
        categoryRepo.save(internalCategory);

        String bookingId = "booking-" + UUID.randomUUID().toString().replace("-", "");
        Booking booking = new Booking();
        booking.setBookingId(bookingId);
        booking.setUserId(recipientUserId);
        booking.setEventId(eventId);
        booking.setQuantity(quantity);
        booking.setTotalCost(BigDecimal.ZERO);
        booking.setPaymentStatus("success");
        booking.setBookingTimestamp(new Timestamp(System.currentTimeMillis()));
        bookingRepo.save(booking);

        String paymentId = "payment-" + UUID.randomUUID().toString().replace("-", "");
        Payment payment = new Payment();
        payment.setPaymentId(paymentId);
        payment.setBookingId(bookingId);
        payment.setAmount(BigDecimal.ZERO);
        payment.setMethod("internal");
        payment.setStatus("success");
        payment.setTransactionTimestamp(new Timestamp(System.currentTimeMillis()));
        paymentRepo.save(payment);

        for (int index = 0; index < quantity; index += 1) {
            Ticket ticket = new Ticket();
            ticket.setTicketId("ticket-" + UUID.randomUUID().toString().replace("-", ""));
            ticket.setQrCode("qr-" + UUID.randomUUID().toString().replace("-", ""));
            ticket.setBookingId(bookingId);
            ticket.setCategoryId(internalCategory.getCategoryId());
            ticket.setStatus(TicketStatus.booked);
            ticketRepo.save(ticket);
        }

        Notification notification = new Notification();
        notification.setNotificationId("notification-" + UUID.randomUUID().toString().replace("-", ""));
        notification.setType("internal_ticket");
        notification.setUserId(recipientUser.getUser_id());
        notification.setEventId(eventId);
        notification.setAudienceScope("DIRECT");
        notification.setMessage(quantity == 1
                ? String.format("An internal usage pass for %s was issued to you.", event.getName())
                : String.format("%d internal usage passes for %s were issued to you.", quantity, event.getName()));
        notification.setSentAt(new Timestamp(System.currentTimeMillis()));
        notification.setCreatedByUserId(createdByUserId);
        notificationRepo.save(notification);

        return new InternalTicketIssueResponse(bookingId, paymentId, internalCategory.getCategoryId(), quantity);
    }
}

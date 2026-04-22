package com.example.demo;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class BulkActionService {
    private final EventRepository eventRepository;
    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;
    private final TicketRepository ticketRepository;
    private final RefundPolicyRepository refundPolicyRepository;
    private final NotificationRepository notificationRepository;
    private final CorporateBookingRequestRepository corporateRequestRepository;
    private final StaffEventAssignmentRepository staffAssignmentRepository;

    @PersistenceContext
    private EntityManager entityManager;

    public BulkActionService(
            EventRepository eventRepository,
            BookingRepository bookingRepository,
            PaymentRepository paymentRepository,
            TicketRepository ticketRepository,
            RefundPolicyRepository refundPolicyRepository,
            NotificationRepository notificationRepository,
            CorporateBookingRequestRepository corporateRequestRepository,
            StaffEventAssignmentRepository staffAssignmentRepository
    ) {
        this.eventRepository = eventRepository;
        this.bookingRepository = bookingRepository;
        this.paymentRepository = paymentRepository;
        this.ticketRepository = ticketRepository;
        this.refundPolicyRepository = refundPolicyRepository;
        this.notificationRepository = notificationRepository;
        this.corporateRequestRepository = corporateRequestRepository;
        this.staffAssignmentRepository = staffAssignmentRepository;
    }

    @Transactional
    public BulkActionResult cancelEvent(String eventId, String targetStatus) {
        Event event = getEvent(eventId);
        ensureEventNotEnded(event);

        String normalizedStatus = targetStatus == null || targetStatus.isBlank() ? "cancelled" : targetStatus.toLowerCase();
        if (!normalizedStatus.equals("cancelled") && !normalizedStatus.equals("deleted")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported event bulk status.");
        }

        List<Booking> bookings = bookingRepository.findByEventId(eventId);
        List<String> bookingIds = bookings.stream().map(Booking::getBookingId).toList();
        List<Payment> payments = paymentRepository.findByBookingIdIn(bookingIds);
        RefundDecision refund = decideRefund(event);

        int eventUpdates = entityManager.createNativeQuery("UPDATE events SET status = :status WHERE event_id = :eventId")
                .setParameter("status", normalizedStatus)
                .setParameter("eventId", eventId)
                .executeUpdate();
        if (eventUpdates == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found");
        }

        int affectedTickets = bookingIds.isEmpty() ? 0 : entityManager.createNativeQuery(
                        "UPDATE tickets SET status = 'cancelled' WHERE booking_id IN (:bookingIds) AND status <> 'cancelled'")
                .setParameter("bookingIds", bookingIds)
                .executeUpdate();

        int affectedBookings = bookingIds.isEmpty() ? 0 : entityManager.createNativeQuery(
                        "UPDATE bookings SET payment_status = 'cancelled' WHERE booking_id IN (:bookingIds) AND payment_status <> 'cancelled'")
                .setParameter("bookingIds", bookingIds)
                .executeUpdate();

        int affectedPayments = updatePaymentsForBookings(bookingIds, refund);

        List<CorporateBookingRequest> corporateRequests = corporateRequestRepository.findByEventIdOrderByCreatedAtDesc(eventId);
        int affectedCorporateRequests = entityManager.createNativeQuery("""
                        UPDATE corporate_booking_requests
                        SET status = 'cancelled',
                            cancelled_at = COALESCE(cancelled_at, CURRENT_TIMESTAMP),
                            updated_at = CURRENT_TIMESTAMP
                        WHERE event_id = :eventId
                          AND status NOT IN ('cancelled', 'rejected', 'expired')
                        """)
                .setParameter("eventId", eventId)
                .executeUpdate();

        Set<String> recipientIds = new LinkedHashSet<>();
        bookings.forEach(booking -> recipientIds.add(booking.getUserId()));
        corporateRequests.forEach(request -> recipientIds.add(request.getCorporateUserId()));
        staffAssignmentRepository.findByEventId(eventId).forEach(assignment -> recipientIds.add(assignment.getStaffUserId()));

        int notificationCount = createNotifications(
                recipientIds,
                eventId,
                normalizedStatus.equals("deleted") ? "event_deleted" : "event_cancelled",
                "Event \"" + event.getName() + "\" has been " + normalizedStatus + ". Refund status: " + refund.paymentStatus + ".",
                event.getOrganizerId()
        );

        BulkActionResult result = new BulkActionResult();
        result.setAction("event_" + normalizedStatus);
        result.setEventId(eventId);
        result.setStatus(normalizedStatus);
        result.setRefundMode(refund.mode);
        result.setPaymentStatus(refund.paymentStatus);
        result.setEligibleAmount(payments.stream().map(Payment::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add));
        result.setApprovedAmount(calculateApprovedAmount(result.getEligibleAmount(), refund));
        result.setAffectedBookings(affectedBookings);
        result.setAffectedTickets(affectedTickets);
        result.setAffectedPayments(affectedPayments);
        result.setAffectedCorporateRequests(affectedCorporateRequests);
        result.setNotificationsCreated(notificationCount);
        result.setMessage("Event " + normalizedStatus + " in one transaction.");
        return result;
    }

    @Transactional
    public BulkActionResult cancelBooking(String bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found"));
        Event event = getEvent(booking.getEventId());
        ensureEventNotEnded(event);

        List<Ticket> activeTickets = ticketRepository.findByBookingId(bookingId).stream()
                .filter(ticket -> ticket.getStatus() == TicketStatus.booked)
                .toList();
        if (activeTickets.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No unused tickets remain in this booking.");
        }

        RefundDecision refund = decideRefund(event);
        Payment payment = paymentRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Payment not found for booking"));
        BigDecimal originalAmount = payment.getAmount();

        int affectedTickets = entityManager.createNativeQuery(
                        "UPDATE tickets SET status = 'cancelled' WHERE booking_id = :bookingId AND status = 'booked'")
                .setParameter("bookingId", bookingId)
                .executeUpdate();

        int affectedBookings = entityManager.createNativeQuery(
                        "UPDATE bookings SET payment_status = 'cancelled' WHERE booking_id = :bookingId")
                .setParameter("bookingId", bookingId)
                .executeUpdate();

        applyRefundToPayment(payment, refund, originalAmount);
        Payment updatedPayment = paymentRepository.save(payment);

        createNotifications(
                Set.of(booking.getUserId()),
                booking.getEventId(),
                "refund",
                refund.mode.equals("none")
                        ? "Booking " + bookingId + " was cancelled. Tickets were released, but no refund applied because the refund window has closed."
                        : "Booking " + bookingId + " was cancelled. Refund status: " + refund.paymentStatus + ".",
                null
        );

        BulkActionResult result = new BulkActionResult();
        result.setAction("booking_cancelled");
        result.setEventId(booking.getEventId());
        result.setBookingId(bookingId);
        result.setStatus("cancelled");
        result.setRefundMode(refund.mode);
        result.setPaymentStatus(updatedPayment.getStatus());
        result.setEligibleAmount(originalAmount);
        result.setApprovedAmount(calculateApprovedAmount(originalAmount, refund));
        result.setAffectedBookings(affectedBookings);
        result.setAffectedTickets(affectedTickets);
        result.setAffectedPayments(1);
        result.setNotificationsCreated(1);
        result.setPayment(updatedPayment);
        result.setTickets(ticketRepository.findByBookingId(bookingId));
        result.setMessage("Booking cancelled in one transaction.");
        return result;
    }

    @Transactional
    public BulkActionResult cancelTicket(String ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket not found"));
        if (ticket.getStatus() == TicketStatus.cancelled) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ticket is already cancelled.");
        }
        if (ticket.getStatus() == TicketStatus.used) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Used tickets cannot be cancelled.");
        }

        Booking booking = bookingRepository.findById(ticket.getBookingId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found"));
        Event event = getEvent(booking.getEventId());
        ensureEventNotEnded(event);
        Payment payment = paymentRepository.findByBookingId(booking.getBookingId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Payment not found for booking"));

        RefundDecision refund = decideRefund(event);
        BigDecimal ticketAmount = booking.getQuantity() > 0
                ? booking.getTotalCost().divide(BigDecimal.valueOf(booking.getQuantity()), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        int affectedTickets = entityManager.createNativeQuery(
                        "UPDATE tickets SET status = 'cancelled' WHERE ticket_id = :ticketId AND status = 'booked'")
                .setParameter("ticketId", ticketId)
                .executeUpdate();
        if (affectedTickets == 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ticket could not be cancelled.");
        }

        Long remainingActive = entityManager.createQuery(
                        "select count(t) from Ticket t where t.bookingId = :bookingId and t.status = :status",
                        Long.class)
                .setParameter("bookingId", booking.getBookingId())
                .setParameter("status", TicketStatus.booked)
                .getSingleResult();

        int affectedBookings = 0;
        if (remainingActive == 0) {
            affectedBookings = entityManager.createNativeQuery(
                            "UPDATE bookings SET payment_status = 'cancelled' WHERE booking_id = :bookingId")
                    .setParameter("bookingId", booking.getBookingId())
                    .executeUpdate();
        }

        applyRefundToPayment(payment, refund, ticketAmount);
        Payment updatedPayment = paymentRepository.save(payment);

        createNotifications(
                Set.of(booking.getUserId()),
                booking.getEventId(),
                "refund",
                refund.mode.equals("none")
                        ? "Ticket " + ticketId + " was cancelled. The seat was released, but no refund applied because the refund window has closed."
                        : "Ticket " + ticketId + " was cancelled. Refund status: " + refund.paymentStatus + ".",
                null
        );

        Ticket updatedTicket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket not found after cancellation"));

        BulkActionResult result = new BulkActionResult();
        result.setAction("ticket_cancelled");
        result.setEventId(booking.getEventId());
        result.setBookingId(booking.getBookingId());
        result.setTicketId(ticketId);
        result.setStatus("cancelled");
        result.setRefundMode(refund.mode);
        result.setPaymentStatus(updatedPayment.getStatus());
        result.setEligibleAmount(ticketAmount);
        result.setApprovedAmount(calculateApprovedAmount(ticketAmount, refund));
        result.setAffectedBookings(affectedBookings);
        result.setAffectedTickets(affectedTickets);
        result.setAffectedPayments(1);
        result.setNotificationsCreated(1);
        result.setPayment(updatedPayment);
        result.setTicket(updatedTicket);
        result.setMessage("Ticket cancelled in one transaction.");
        return result;
    }

    private Event getEvent(String eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found"));
    }

    private void ensureEventNotEnded(Event event) {
        if (event.getEndTime() != null && !event.getEndTime().isAfter(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "This action is not allowed after the event has ended.");
        }
    }

    private RefundDecision decideRefund(Event event) {
        RefundPolicy policy = refundPolicyRepository.findByEventId(event.getEventId())
                .orElse(null);
        if (policy == null || event.getStartTime() == null) {
            return new RefundDecision("none", "no_refund", BigDecimal.ZERO);
        }

        long hoursBeforeEvent = Duration.between(LocalDateTime.now(), event.getStartTime()).toHours();
        if (hoursBeforeEvent >= policy.getFullRefundHours()) {
            return new RefundDecision("full", "refunded_full", BigDecimal.ONE);
        }
        if (hoursBeforeEvent >= policy.getPartialRefundHours()) {
            return new RefundDecision(
                    "partial",
                    "refunded_partial",
                    BigDecimal.valueOf(policy.getPartialRefundPct()).divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP)
            );
        }
        return new RefundDecision("none", "no_refund", BigDecimal.ZERO);
    }

    private BigDecimal calculateApprovedAmount(BigDecimal eligibleAmount, RefundDecision refund) {
        return eligibleAmount.multiply(refund.multiplier).setScale(2, RoundingMode.HALF_UP);
    }

    private int updatePaymentsForBookings(List<String> bookingIds, RefundDecision refund) {
        if (bookingIds.isEmpty()) {
            return 0;
        }
        return entityManager.createNativeQuery("""
                        UPDATE payments
                        SET status = :status,
                            amount = ROUND((amount * CAST(:multiplier AS numeric)), 2)
                        WHERE booking_id IN (:bookingIds)
                          AND lower(status) NOT IN ('refunded_full', 'refunded_partial', 'no_refund')
                        """)
                .setParameter("status", refund.paymentStatus)
                .setParameter("multiplier", refund.multiplier)
                .setParameter("bookingIds", bookingIds)
                .executeUpdate();
    }

    private void applyRefundToPayment(Payment payment, RefundDecision refund, BigDecimal eligibleAmount) {
        payment.setStatus(refund.paymentStatus);
        payment.setAmount(calculateApprovedAmount(eligibleAmount, refund));
    }

    private int createNotifications(Set<String> userIds, String eventId, String type, String message, String createdByUserId) {
        List<Notification> notifications = new ArrayList<>();
        for (String userId : userIds) {
            if (userId == null || userId.isBlank()) {
                continue;
            }
            Notification notification = new Notification();
            notification.setNotificationId(UUID.randomUUID().toString());
            notification.setType(type);
            notification.setMessage(message);
            notification.setEventId(eventId);
            notification.setUserId(userId);
            notification.setAudienceScope("DIRECT");
            notification.setMetadata("{}");
            notification.setCreatedByUserId(createdByUserId);
            notification.setSentAt(new Timestamp(System.currentTimeMillis()));
            notifications.add(notification);
        }
        notificationRepository.saveAll(notifications);
        return notifications.size();
    }

    private record RefundDecision(String mode, String paymentStatus, BigDecimal multiplier) {}
}

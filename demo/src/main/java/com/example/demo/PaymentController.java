package com.example.demo;

import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/payments")
@CrossOrigin
public class PaymentController {

    private final PaymentRepository paymentRepo;
    private final BookingRepository bookingRepo;
    private final RefundPolicyRepository refundPolicyRepo;
    private final EventRepository eventRepo;

    public PaymentController(PaymentRepository paymentRepo, BookingRepository bookingRepo, RefundPolicyRepository refundPolicyRepo, EventRepository eventRepo) {
        this.paymentRepo = paymentRepo;
        this.bookingRepo = bookingRepo;
        this.refundPolicyRepo = refundPolicyRepo;
        this.eventRepo = eventRepo;
    }

    // CREATE PAYMENT
    @PostMapping
    public Payment create(@RequestBody Payment payment) {

        // Check booking exists
        bookingRepo.findById(payment.getBookingId())
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        // Ensure only one payment per booking (UNIQUE constraint)
        if (paymentRepo.findByBookingId(payment.getBookingId()).isPresent()) {
            throw new RuntimeException("Payment already exists for this booking");
        }

        // Basic validations
        if (payment.getAmount().compareTo(java.math.BigDecimal.ZERO) < 0)
            throw new RuntimeException("Amount must be zero or greater");

        if (payment.getMethod() == null || payment.getMethod().isEmpty())
            throw new RuntimeException("Payment method required");

        return paymentRepo.save(payment);
    }

    @PostMapping("/refund/{paymentId}")
    public Payment processRefund(@PathVariable String paymentId) {
        Payment payment = paymentRepo.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        Booking booking = bookingRepo.findById(payment.getBookingId())
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        // Get policy for the booking's event
        RefundPolicy policy = refundPolicyRepo.findByEventId(booking.getEventId())
                .orElseThrow(() -> new RuntimeException("No refund policy for this event"));

        // Calculate hours before the event start
        Event event = eventRepo.findById(booking.getEventId())
                .orElseThrow(() -> new RuntimeException("Event not found"));
        long hoursBeforeEvent = java.time.Duration.between(java.time.LocalDateTime.now(), event.getStartTime()).toHours();

        BigDecimal refundAmount;
        if (hoursBeforeEvent >= policy.getFullRefundHours()) {
            // Full refund
            refundAmount = payment.getAmount();
            payment.setStatus("refunded_full");
        } else if (hoursBeforeEvent >= policy.getPartialRefundHours()) {
            // Partial refund
            refundAmount = payment.getAmount()
                    .multiply(BigDecimal.valueOf(policy.getPartialRefundPct()))
                    .divide(BigDecimal.valueOf(100));
            payment.setStatus("refunded_partial");
        } else {
            // No refund
            refundAmount = BigDecimal.ZERO;
            payment.setStatus("no_refund");
        }

        payment.setAmount(refundAmount);
        return paymentRepo.save(payment);
    }

    // GET ALL PAYMENTS
    @GetMapping
    public List<Payment> getAll() {
        return paymentRepo.findAll();
    }

    // GET BY ID
    @GetMapping("/{id}")
    public Payment getById(@PathVariable String id) {
        return paymentRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Payment not found"));
    }

    // GET BY BOOKING ID
    @GetMapping("/booking/{bookingId}")
    public Payment getByBooking(@PathVariable String bookingId) {
        return paymentRepo.findByBookingId(bookingId)
                .orElseThrow(() -> new RuntimeException("Payment not found for booking"));
    }

    // UPDATE STATUS (e.g. success / failed)
    @PatchMapping("/{id}/status")
    public Payment updateStatus(@PathVariable String id, @RequestParam String status) {
        Payment payment = paymentRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        payment.setStatus(status.toLowerCase());
        return paymentRepo.save(payment);
    }

    // DELETE
    @DeleteMapping("/{id}")
    public void delete(@PathVariable String id) {
        paymentRepo.deleteById(id);
    }
}

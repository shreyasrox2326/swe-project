package com.example.demo;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.sql.Timestamp;

@Entity
@Table(name = "bookings")
public class Booking {

    @Id
    @Column(name = "booking_id", nullable = false)
    private String bookingId;

    @Column(name = "user_id", nullable = false)
    private String userId; // store only user ID

    @Column(name = "event_id", nullable = false)
    private String eventId; // new field to link booking to an event

    @Column(name = "quantity", nullable = false)
    private int quantity;

    @Column(name = "total_cost", nullable = false)
    private BigDecimal totalCost;

    @Column(name = "payment_status", nullable = false)
    private String paymentStatus = "pending";

    @Column(name = "booking_timestamp", nullable = false)
    private Timestamp bookingTimestamp = new Timestamp(System.currentTimeMillis());

    // Getters and setters
    public String getBookingId() { return bookingId; }
    public void setBookingId(String bookingId) { this.bookingId = bookingId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public BigDecimal getTotalCost() { return totalCost; }
    public void setTotalCost(BigDecimal totalCost) { this.totalCost = totalCost; }

    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }

    public Timestamp getBookingTimestamp() { return bookingTimestamp; }
    public void setBookingTimestamp(Timestamp bookingTimestamp) { this.bookingTimestamp = bookingTimestamp; }
}
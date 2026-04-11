package com.example.demo;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "tickets")
public class Ticket {

    @Id
    @Column(name = "ticket_id", nullable = false)
    private String ticketId;

    @Column(name = "qr_code", nullable = false, unique = true)
    private String qrCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TicketStatus status = TicketStatus.booked;

    @Column(name = "category_id", nullable = false)
    private String categoryId; // store only category ID

    @Column(name = "booking_id")
    private String bookingId; // store only booking ID

    public Ticket() {
        this.ticketId = UUID.randomUUID().toString();
        this.qrCode = UUID.randomUUID().toString();
    }

    // Getters and setters
    public String getTicketId() { return ticketId; }
    public void setTicketId(String ticketId) { this.ticketId = ticketId; }

    public String getQrCode() { return qrCode; }
    public void setQrCode(String qrCode) { this.qrCode = qrCode; }

    public TicketStatus getStatus() { return status; }
    public void setStatus(TicketStatus status) { this.status = status; }

    public String getCategoryId() { return categoryId; }
    public void setCategoryId(String categoryId) { this.categoryId = categoryId; }

    public String getBookingId() { return bookingId; }
    public void setBookingId(String bookingId) { this.bookingId = bookingId; }
}
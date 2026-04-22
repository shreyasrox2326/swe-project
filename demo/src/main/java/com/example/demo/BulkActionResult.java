package com.example.demo;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class BulkActionResult {
    private String action;
    private String eventId;
    private String bookingId;
    private String ticketId;
    private String status;
    private String refundMode;
    private String paymentStatus;
    private BigDecimal eligibleAmount = BigDecimal.ZERO;
    private BigDecimal approvedAmount = BigDecimal.ZERO;
    private int affectedBookings;
    private int affectedTickets;
    private int affectedPayments;
    private int affectedCorporateRequests;
    private int notificationsCreated;
    private String message;
    private Payment payment;
    private Ticket ticket;
    private List<Ticket> tickets = new ArrayList<>();

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }
    public String getBookingId() { return bookingId; }
    public void setBookingId(String bookingId) { this.bookingId = bookingId; }
    public String getTicketId() { return ticketId; }
    public void setTicketId(String ticketId) { this.ticketId = ticketId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getRefundMode() { return refundMode; }
    public void setRefundMode(String refundMode) { this.refundMode = refundMode; }
    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }
    public BigDecimal getEligibleAmount() { return eligibleAmount; }
    public void setEligibleAmount(BigDecimal eligibleAmount) { this.eligibleAmount = eligibleAmount; }
    public BigDecimal getApprovedAmount() { return approvedAmount; }
    public void setApprovedAmount(BigDecimal approvedAmount) { this.approvedAmount = approvedAmount; }
    public int getAffectedBookings() { return affectedBookings; }
    public void setAffectedBookings(int affectedBookings) { this.affectedBookings = affectedBookings; }
    public int getAffectedTickets() { return affectedTickets; }
    public void setAffectedTickets(int affectedTickets) { this.affectedTickets = affectedTickets; }
    public int getAffectedPayments() { return affectedPayments; }
    public void setAffectedPayments(int affectedPayments) { this.affectedPayments = affectedPayments; }
    public int getAffectedCorporateRequests() { return affectedCorporateRequests; }
    public void setAffectedCorporateRequests(int affectedCorporateRequests) { this.affectedCorporateRequests = affectedCorporateRequests; }
    public int getNotificationsCreated() { return notificationsCreated; }
    public void setNotificationsCreated(int notificationsCreated) { this.notificationsCreated = notificationsCreated; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public Payment getPayment() { return payment; }
    public void setPayment(Payment payment) { this.payment = payment; }
    public Ticket getTicket() { return ticket; }
    public void setTicket(Ticket ticket) { this.ticket = ticket; }
    public List<Ticket> getTickets() { return tickets; }
    public void setTickets(List<Ticket> tickets) { this.tickets = tickets; }
}

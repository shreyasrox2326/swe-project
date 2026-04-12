package com.example.demo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.sql.Timestamp;

@Entity
@Table(name = "corporate_booking_requests")
public class CorporateBookingRequest {

    @Id
    @Column(name = "request_id", nullable = false)
    private String requestId;

    @Column(name = "corporate_user_id", nullable = false)
    private String corporateUserId;

    @Column(name = "organizer_user_id", nullable = false)
    private String organizerUserId;

    @Column(name = "event_id", nullable = false)
    private String eventId;

    @Column(name = "status", nullable = false)
    private String status = "submitted";

    @Column(name = "requested_total_qty", nullable = false)
    private Integer requestedTotalQty;

    @Column(name = "offered_total_amount")
    private BigDecimal offeredTotalAmount;

    @Column(name = "corporate_note")
    private String corporateNote;

    @Column(name = "organizer_note")
    private String organizerNote;

    @Column(name = "created_at", nullable = false)
    private Timestamp createdAt = new Timestamp(System.currentTimeMillis());

    @Column(name = "updated_at", nullable = false)
    private Timestamp updatedAt = new Timestamp(System.currentTimeMillis());

    @Column(name = "decision_at")
    private Timestamp decisionAt;

    @Column(name = "approved_at")
    private Timestamp approvedAt;

    @Column(name = "expires_at")
    private Timestamp expiresAt;

    @Column(name = "cancelled_at")
    private Timestamp cancelledAt;

    @Column(name = "paid_at")
    private Timestamp paidAt;

    @Column(name = "booking_id")
    private String bookingId;

    @Column(name = "payment_id")
    private String paymentId;

    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }
    public String getCorporateUserId() { return corporateUserId; }
    public void setCorporateUserId(String corporateUserId) { this.corporateUserId = corporateUserId; }
    public String getOrganizerUserId() { return organizerUserId; }
    public void setOrganizerUserId(String organizerUserId) { this.organizerUserId = organizerUserId; }
    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Integer getRequestedTotalQty() { return requestedTotalQty; }
    public void setRequestedTotalQty(Integer requestedTotalQty) { this.requestedTotalQty = requestedTotalQty; }
    public BigDecimal getOfferedTotalAmount() { return offeredTotalAmount; }
    public void setOfferedTotalAmount(BigDecimal offeredTotalAmount) { this.offeredTotalAmount = offeredTotalAmount; }
    public String getCorporateNote() { return corporateNote; }
    public void setCorporateNote(String corporateNote) { this.corporateNote = corporateNote; }
    public String getOrganizerNote() { return organizerNote; }
    public void setOrganizerNote(String organizerNote) { this.organizerNote = organizerNote; }
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }
    public Timestamp getDecisionAt() { return decisionAt; }
    public void setDecisionAt(Timestamp decisionAt) { this.decisionAt = decisionAt; }
    public Timestamp getApprovedAt() { return approvedAt; }
    public void setApprovedAt(Timestamp approvedAt) { this.approvedAt = approvedAt; }
    public Timestamp getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Timestamp expiresAt) { this.expiresAt = expiresAt; }
    public Timestamp getCancelledAt() { return cancelledAt; }
    public void setCancelledAt(Timestamp cancelledAt) { this.cancelledAt = cancelledAt; }
    public Timestamp getPaidAt() { return paidAt; }
    public void setPaidAt(Timestamp paidAt) { this.paidAt = paidAt; }
    public String getBookingId() { return bookingId; }
    public void setBookingId(String bookingId) { this.bookingId = bookingId; }
    public String getPaymentId() { return paymentId; }
    public void setPaymentId(String paymentId) { this.paymentId = paymentId; }
}

package com.example.demo;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.sql.Timestamp;

@Entity
@Table(name = "payments")
public class Payment {

    @Id
    @Column(name = "payment_id", nullable = false)
    private String paymentId;

    @Column(name = "amount", nullable = false)
    private BigDecimal amount;

    @Column(name = "method", nullable = false)
    private String method;

    @Column(name = "status", nullable = false)
    private String status = "pending";

    @Column(name = "transaction_timestamp")
    private Timestamp transactionTimestamp = new Timestamp(System.currentTimeMillis());

    @Column(name = "booking_id", nullable = false)
    private String bookingId; // only ID, no object

    // Getters and setters
    public String getPaymentId() { return paymentId; }
    public void setPaymentId(String paymentId) { this.paymentId = paymentId; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Timestamp getTransactionTimestamp() { return transactionTimestamp; }
    public void setTransactionTimestamp(Timestamp transactionTimestamp) { this.transactionTimestamp = transactionTimestamp; }

    public String getBookingId() { return bookingId; }
    public void setBookingId(String bookingId) { this.bookingId = bookingId; }
}
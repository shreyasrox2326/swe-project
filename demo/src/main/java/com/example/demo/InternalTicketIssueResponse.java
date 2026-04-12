package com.example.demo;

public class InternalTicketIssueResponse {

    private String bookingId;
    private String paymentId;
    private String categoryId;
    private int quantity;

    public InternalTicketIssueResponse() {
    }

    public InternalTicketIssueResponse(String bookingId, String paymentId, String categoryId, int quantity) {
        this.bookingId = bookingId;
        this.paymentId = paymentId;
        this.categoryId = categoryId;
        this.quantity = quantity;
    }

    public String getBookingId() {
        return bookingId;
    }

    public void setBookingId(String bookingId) {
        this.bookingId = bookingId;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}

package com.example.demo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "corporate_booking_request_items")
public class CorporateBookingRequestItem {

    @Id
    @Column(name = "request_item_id", nullable = false)
    private String requestItemId;

    @Column(name = "request_id", nullable = false)
    private String requestId;

    @Column(name = "category_id", nullable = false)
    private String categoryId;

    @Column(name = "requested_qty", nullable = false)
    private Integer requestedQty;

    @Column(name = "approved_qty")
    private Integer approvedQty;

    @Column(name = "reserved_qty", nullable = false)
    private Integer reservedQty = 0;

    @Column(name = "offered_unit_price")
    private BigDecimal offeredUnitPrice;

    public String getRequestItemId() { return requestItemId; }
    public void setRequestItemId(String requestItemId) { this.requestItemId = requestItemId; }
    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }
    public String getCategoryId() { return categoryId; }
    public void setCategoryId(String categoryId) { this.categoryId = categoryId; }
    public Integer getRequestedQty() { return requestedQty; }
    public void setRequestedQty(Integer requestedQty) { this.requestedQty = requestedQty; }
    public Integer getApprovedQty() { return approvedQty; }
    public void setApprovedQty(Integer approvedQty) { this.approvedQty = approvedQty; }
    public Integer getReservedQty() { return reservedQty; }
    public void setReservedQty(Integer reservedQty) { this.reservedQty = reservedQty; }
    public BigDecimal getOfferedUnitPrice() { return offeredUnitPrice; }
    public void setOfferedUnitPrice(BigDecimal offeredUnitPrice) { this.offeredUnitPrice = offeredUnitPrice; }
}

package com.example.demo;

import java.math.BigDecimal;

public class CorporateRequestItemInput {
    private String categoryId;
    private Integer quantity;
    private BigDecimal offeredUnitPrice;

    public String getCategoryId() { return categoryId; }
    public void setCategoryId(String categoryId) { this.categoryId = categoryId; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public BigDecimal getOfferedUnitPrice() { return offeredUnitPrice; }
    public void setOfferedUnitPrice(BigDecimal offeredUnitPrice) { this.offeredUnitPrice = offeredUnitPrice; }
}

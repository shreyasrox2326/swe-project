package com.example.demo;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.sql.Timestamp;

@Entity
@Table(name = "ticket_categories")
public class TicketCategory {

    @Id
    @Column(name = "category_id", nullable = false)
    private String categoryId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "price", nullable = false)
    private BigDecimal price;

    @Column(name = "total_qty", nullable = false)
    private int totalQty;

    @Column(name = "available_qty", nullable = false)
    private int availableQty;

    @Column(name = "sale_start_date")
    private Timestamp saleStartDate;

    @Column(name = "event_id", nullable = false)
    private String eventId; // store only event ID

    // Getters and setters
    public String getCategoryId() { return categoryId; }
    public void setCategoryId(String categoryId) { this.categoryId = categoryId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public int getTotalQty() { return totalQty; }
    public void setTotalQty(int totalQty) { this.totalQty = totalQty; }

    public int getAvailableQty() { return availableQty; }
    public void setAvailableQty(int availableQty) { this.availableQty = availableQty; }

    public Timestamp getSaleStartDate() { return saleStartDate; }
    public void setSaleStartDate(Timestamp saleStartDate) { this.saleStartDate = saleStartDate; }

    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }
}
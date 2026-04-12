package com.example.demo;

import java.util.List;

public class ApproveCorporateBookingRequest {
    private String organizerNote;
    private String expiresAt;
    private List<CorporateRequestItemInput> items;

    public String getOrganizerNote() { return organizerNote; }
    public void setOrganizerNote(String organizerNote) { this.organizerNote = organizerNote; }
    public String getExpiresAt() { return expiresAt; }
    public void setExpiresAt(String expiresAt) { this.expiresAt = expiresAt; }
    public List<CorporateRequestItemInput> getItems() { return items; }
    public void setItems(List<CorporateRequestItemInput> items) { this.items = items; }
}

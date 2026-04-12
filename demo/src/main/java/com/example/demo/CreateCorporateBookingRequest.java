package com.example.demo;

import java.util.List;

public class CreateCorporateBookingRequest {
    private String requestId;
    private String corporateUserId;
    private String eventId;
    private String corporateNote;
    private List<CorporateRequestItemInput> items;

    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }
    public String getCorporateUserId() { return corporateUserId; }
    public void setCorporateUserId(String corporateUserId) { this.corporateUserId = corporateUserId; }
    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }
    public String getCorporateNote() { return corporateNote; }
    public void setCorporateNote(String corporateNote) { this.corporateNote = corporateNote; }
    public List<CorporateRequestItemInput> getItems() { return items; }
    public void setItems(List<CorporateRequestItemInput> items) { this.items = items; }
}

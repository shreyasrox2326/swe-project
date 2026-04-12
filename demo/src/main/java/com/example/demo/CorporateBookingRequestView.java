package com.example.demo;

import java.util.List;

public class CorporateBookingRequestView {
    private CorporateBookingRequest request;
    private List<CorporateBookingRequestItem> items;

    public CorporateBookingRequestView(CorporateBookingRequest request, List<CorporateBookingRequestItem> items) {
        this.request = request;
        this.items = items;
    }

    public CorporateBookingRequest getRequest() { return request; }
    public void setRequest(CorporateBookingRequest request) { this.request = request; }
    public List<CorporateBookingRequestItem> getItems() { return items; }
    public void setItems(List<CorporateBookingRequestItem> items) { this.items = items; }
}

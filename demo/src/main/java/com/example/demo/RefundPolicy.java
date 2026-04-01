package com.example.demo;

import jakarta.persistence.*;

@Entity
@Table(name = "refund_policies")
public class RefundPolicy {

    @Id
    @Column(name = "policy_id")
    private String policyId;

    @Column(name = "full_refund_hours", nullable = false)
    private int fullRefundHours;

    @Column(name = "partial_refund_hours", nullable = false)
    private int partialRefundHours;

    @Column(name = "partial_refund_pct", nullable = false)
    private int partialRefundPct;

    @Column(name = "event_id", nullable = false)
    private String eventId; // only ID

    // getters/setters
    public String getPolicyId() { return policyId; }
    public void setPolicyId(String policyId) { this.policyId = policyId; }

    public int getFullRefundHours() { return fullRefundHours; }
    public void setFullRefundHours(int fullRefundHours) { this.fullRefundHours = fullRefundHours; }

    public int getPartialRefundHours() { return partialRefundHours; }
    public void setPartialRefundHours(int partialRefundHours) { this.partialRefundHours = partialRefundHours; }

    public int getPartialRefundPct() { return partialRefundPct; }
    public void setPartialRefundPct(int partialRefundPct) { this.partialRefundPct = partialRefundPct; }

    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }
}
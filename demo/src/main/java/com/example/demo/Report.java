package com.example.demo;

import jakarta.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "reports")
public class Report {

    @Id
    @Column(name = "report_id", nullable = false)
    private String reportId;

    @Column(name = "generated_date", nullable = false)
    private Timestamp generatedDate = new Timestamp(System.currentTimeMillis());

    @Column(name = "data", nullable = false, columnDefinition = "jsonb")
    private String data; // store JSON as string

    @Column(name = "organizer_id", nullable = false)
    private String organizerId;

    // Getters and setters
    public String getReportId() { return reportId; }
    public void setReportId(String reportId) { this.reportId = reportId; }

    public Timestamp getGeneratedDate() { return generatedDate; }
    public void setGeneratedDate(Timestamp generatedDate) { this.generatedDate = generatedDate; }

    public String getData() { return data; }
    public void setData(String data) { this.data = data; }

    public String getOrganizerId() { return organizerId; }
    public void setOrganizerId(String organizerId) { this.organizerId = organizerId; }
}
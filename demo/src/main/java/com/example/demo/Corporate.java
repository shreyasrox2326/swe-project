package com.example.demo;

import jakarta.persistence.*;

@Entity
@Table(name = "corporates")
public class Corporate {

    @Id
    @Column(name = "user_id")
    private String userId;

    @Column(name = "company_name")
    private String companyName;

    @Column(name = "gst_number")
    private String gstNumber;

    // getters + setters
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public String getGstNumber() { return gstNumber; }
    public void setGstNumber(String gstNumber) { this.gstNumber = gstNumber; }
}

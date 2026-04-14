package com.example.demo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.sql.Timestamp;

@Entity
@Table(name = "auth_otp_challenges")
public class AuthOtpChallenge {

    @Id
    @Column(name = "challenge_id", nullable = false)
    private String challengeId;

    @Column(name = "purpose", nullable = false)
    private String purpose;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "user_id")
    private String userId;

    @Column(name = "reference_id")
    private String referenceId;

    @Column(name = "otp_hash", nullable = false)
    private String otpHash;

    @Column(name = "payload", nullable = false)
    private String payload = "{}";

    @Column(name = "created_at", nullable = false)
    private Timestamp createdAt = new Timestamp(System.currentTimeMillis());

    @Column(name = "expires_at", nullable = false)
    private Timestamp expiresAt;

    @Column(name = "consumed_at")
    private Timestamp consumedAt;

    @Column(name = "attempt_count", nullable = false)
    private Integer attemptCount = 0;

    public String getChallengeId() {
        return challengeId;
    }

    public void setChallengeId(String challengeId) {
        this.challengeId = challengeId;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(String referenceId) {
        this.referenceId = referenceId;
    }

    public String getOtpHash() {
        return otpHash;
    }

    public void setOtpHash(String otpHash) {
        this.otpHash = otpHash;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Timestamp expiresAt) {
        this.expiresAt = expiresAt;
    }

    public Timestamp getConsumedAt() {
        return consumedAt;
    }

    public void setConsumedAt(Timestamp consumedAt) {
        this.consumedAt = consumedAt;
    }

    public Integer getAttemptCount() {
        return attemptCount;
    }

    public void setAttemptCount(Integer attemptCount) {
        this.attemptCount = attemptCount;
    }
}

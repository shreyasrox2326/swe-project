package com.example.demo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.security.SecureRandom;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;

@Service
public class OtpChallengeService {

    public static final String PURPOSE_REGISTRATION = "REGISTRATION";
    public static final String PURPOSE_PASSWORD_RESET = "PASSWORD_RESET";
    public static final String PURPOSE_CUSTOMER_PAYMENT = "CUSTOMER_PAYMENT";
    public static final String PURPOSE_CORPORATE_PAYMENT = "CORPORATE_PAYMENT";

    private final AuthOtpChallengeRepository challengeRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final EmailDeliveryService emailDeliveryService;
    private final ObjectMapper objectMapper;
    private final SecureRandom random = new SecureRandom();
    private final long defaultTtlMinutes;
    private final long paymentTtlMinutes;
    private final long registrationTtlMinutes;
    private final long passwordResetTtlMinutes;

    public OtpChallengeService(
            AuthOtpChallengeRepository challengeRepository,
            BCryptPasswordEncoder passwordEncoder,
            EmailDeliveryService emailDeliveryService,
            ObjectMapper objectMapper,
            @Value("${app.otp.default-ttl-minutes:10}") long defaultTtlMinutes,
            @Value("${app.otp.payment-ttl-minutes:10}") long paymentTtlMinutes,
            @Value("${app.otp.registration-ttl-minutes:10}") long registrationTtlMinutes,
            @Value("${app.otp.password-reset-ttl-minutes:10}") long passwordResetTtlMinutes
    ) {
        this.challengeRepository = challengeRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailDeliveryService = emailDeliveryService;
        this.objectMapper = objectMapper;
        this.defaultTtlMinutes = defaultTtlMinutes;
        this.paymentTtlMinutes = paymentTtlMinutes;
        this.registrationTtlMinutes = registrationTtlMinutes;
        this.passwordResetTtlMinutes = passwordResetTtlMinutes;
    }

    public OtpChallengeResponse issueRegistrationOtp(String email, Map<String, Object> payload) {
        return issueChallenge(
                PURPOSE_REGISTRATION,
                email,
                null,
                null,
                payload,
                registrationTtlMinutes,
                "Verify your EMTS account",
                "Use this OTP to finish creating your EMTS account."
        );
    }

    public OtpChallengeResponse issuePasswordResetOtp(String email, String userId) {
        return issueChallenge(
                PURPOSE_PASSWORD_RESET,
                email,
                userId,
                null,
                Collections.emptyMap(),
                passwordResetTtlMinutes,
                "Reset your EMTS password",
                "Use this OTP to reset your EMTS password."
        );
    }

    public OtpChallengeResponse issuePaymentOtp(String purpose, String email, String userId, String referenceId, Map<String, Object> payload) {
        return issueChallenge(
                purpose,
                email,
                userId,
                referenceId,
                payload,
                paymentTtlMinutes,
                "Confirm your EMTS payment",
                "Use this OTP to confirm your payment."
        );
    }

    private OtpChallengeResponse issueChallenge(
            String purpose,
            String email,
            String userId,
            String referenceId,
            Map<String, Object> payload,
            long ttlMinutes,
            String subject,
            String intro
    ) {
        String otpCode = String.format("%06d", random.nextInt(1_000_000));
        AuthOtpChallenge challenge = new AuthOtpChallenge();
        challenge.setChallengeId(UUID.randomUUID().toString());
        challenge.setPurpose(purpose);
        challenge.setEmail(email.trim().toLowerCase());
        challenge.setUserId(userId);
        challenge.setReferenceId(referenceId);
        challenge.setOtpHash(passwordEncoder.encode(otpCode));
        challenge.setPayload(writePayload(payload));
        challenge.setCreatedAt(Timestamp.from(Instant.now()));
        challenge.setExpiresAt(Timestamp.from(Instant.now().plus(ttlMinutes, ChronoUnit.MINUTES)));
        challenge.setAttemptCount(0);
        challengeRepository.save(challenge);

        emailDeliveryService.sendPlainText(
                email,
                subject,
                intro
                        + "\n\nOTP: "
                        + otpCode
                        + "\n\nThis code expires in "
                        + ttlMinutes
                        + " minutes."
                        + "\n\nIf you did not request this, you can ignore this email."
        );

        OtpChallengeResponse response = new OtpChallengeResponse();
        response.setChallengeId(challenge.getChallengeId());
        response.setEmail(challenge.getEmail());
        response.setExpiresAt(challenge.getExpiresAt());
        response.setMessage("OTP sent successfully.");
        response.setPaymentId(referenceId);
        return response;
    }

    public AuthOtpChallenge verifyAndConsume(String challengeId, String expectedPurpose, String otpCode) {
        AuthOtpChallenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "OTP challenge not found"));

        if (!expectedPurpose.equalsIgnoreCase(challenge.getPurpose())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "OTP challenge purpose mismatch");
        }
        if (challenge.getConsumedAt() != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "OTP has already been used");
        }
        if (challenge.getExpiresAt() == null || challenge.getExpiresAt().before(Timestamp.from(Instant.now()))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "OTP has expired");
        }

        if (!passwordEncoder.matches(otpCode, challenge.getOtpHash())) {
            challenge.setAttemptCount((challenge.getAttemptCount() == null ? 0 : challenge.getAttemptCount()) + 1);
            challengeRepository.save(challenge);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid OTP");
        }

        challenge.setConsumedAt(Timestamp.from(Instant.now()));
        challengeRepository.save(challenge);
        return challenge;
    }

    public Map<String, Object> readPayload(AuthOtpChallenge challenge) {
        if (challenge.getPayload() == null || challenge.getPayload().isBlank()) {
            return Collections.emptyMap();
        }

        try {
            return objectMapper.readValue(challenge.getPayload(), new TypeReference<>() {});
        } catch (JsonProcessingException exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to read OTP payload");
        }
    }

    private String writePayload(Map<String, Object> payload) {
        try {
            return objectMapper.writeValueAsString(payload == null ? Collections.emptyMap() : payload);
        } catch (JsonProcessingException exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to store OTP payload");
        }
    }
}

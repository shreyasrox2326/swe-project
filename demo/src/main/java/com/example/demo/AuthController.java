package com.example.demo;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/auth")
@CrossOrigin
public class AuthController {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final OtpChallengeService otpChallengeService;
    private final EmailDeliveryService emailDeliveryService;

    public AuthController(
            UserRepository userRepository,
            BCryptPasswordEncoder passwordEncoder,
            OtpChallengeService otpChallengeService,
            EmailDeliveryService emailDeliveryService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.otpChallengeService = otpChallengeService;
        this.emailDeliveryService = emailDeliveryService;
    }

    @PostMapping("/register/start")
    public OtpChallengeResponse startRegistration(@RequestBody RegistrationStartRequest request) {
        if (request.getFullName() == null || request.getFullName().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Full name is required");
        }
        if (request.getEmail() == null || request.getEmail().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email is required");
        }
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password is required");
        }
        if (userRepository.findByEmailIgnoreCase(request.getEmail()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "An account with this email already exists.");
        }

        return otpChallengeService.issueRegistrationOtp(
                request.getEmail(),
                Map.of(
                        "fullName", request.getFullName().trim(),
                        "email", request.getEmail().trim().toLowerCase(),
                        "phone", request.getPhone() == null ? "" : request.getPhone().trim(),
                        "passwordHash", passwordEncoder.encode(request.getPassword()),
                        "type", UserType.customer.name()
                )
        );
    }

    @PostMapping("/register/verify")
    public ResponseEntity<LoginResponse> verifyRegistration(@RequestBody OtpVerificationRequest request) {
        AuthOtpChallenge challenge = otpChallengeService.verifyAndConsume(
                request.getChallengeId(),
                OtpChallengeService.PURPOSE_REGISTRATION,
                request.getOtpCode()
        );
        Map<String, Object> payload = otpChallengeService.readPayload(challenge);
        String email = String.valueOf(payload.getOrDefault("email", "")).trim().toLowerCase();

        if (userRepository.findByEmailIgnoreCase(email).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "An account with this email already exists.");
        }

        User user = new User();
        user.setUser_id(UUID.randomUUID().toString());
        user.setName(String.valueOf(payload.getOrDefault("fullName", "")).trim());
        user.setEmail(email);
        user.setPhone(String.valueOf(payload.getOrDefault("phone", "")).trim());
        user.setPassword_hash(String.valueOf(payload.getOrDefault("passwordHash", "")));
        user.setType(UserType.valueOf(String.valueOf(payload.getOrDefault("type", UserType.customer.name()))));
        userRepository.save(user);

        emailDeliveryService.sendPlainText(
                user.getEmail(),
                "Welcome to EMTS",
                "Your EMTS account is now active.\n\n"
                        + "Name: " + user.getName()
                        + "\nEmail: " + user.getEmail()
                        + "\n\nYou can now sign in and start booking tickets."
        );

        return ResponseEntity.ok(new LoginResponse(user));
    }

    @PostMapping("/password-reset/start")
    public OtpChallengeResponse startPasswordReset(@RequestBody PasswordResetStartRequest request) {
        if (request.getEmail() == null || request.getEmail().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email is required");
        }

        User user = userRepository.findByEmailIgnoreCase(request.getEmail())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No account was found for this email."));

        return otpChallengeService.issuePasswordResetOtp(user.getEmail(), user.getUser_id());
    }

    @PostMapping("/password-reset/complete")
    public ResponseEntity<Void> completePasswordReset(@RequestBody PasswordResetCompleteRequest request) {
        if (request.getNewPassword() == null || request.getNewPassword().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "New password is required");
        }

        AuthOtpChallenge challenge = otpChallengeService.verifyAndConsume(
                request.getChallengeId(),
                OtpChallengeService.PURPOSE_PASSWORD_RESET,
                request.getOtpCode()
        );

        User user = userRepository.findById(challenge.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found for password reset."));
        user.setPassword_hash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        emailDeliveryService.sendPlainText(
                user.getEmail(),
                "Your EMTS password was changed",
                "Your EMTS password has been updated successfully.\n\n"
                        + "If you did not make this change, reset your password again immediately."
        );
        return ResponseEntity.noContent().build();
    }
}

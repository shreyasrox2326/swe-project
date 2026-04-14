package com.example.demo;

import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class EmailDeliveryService {

    private final Resend resend;
    private final String fromAddress;

    public EmailDeliveryService(
            @Value("${app.mail.resend-api-key}") String resendApiKey,
            @Value("${app.mail.from}") String fromAddress
    ) {
        this.resend = new Resend(resendApiKey);
        this.fromAddress = fromAddress;
    }

    public void sendPlainText(String to, String subject, String body) {
        try {
            CreateEmailOptions options = CreateEmailOptions.builder()
                    .from(fromAddress)
                    .to(to)
                    .subject(subject)
                    .text(body)
                    .build();
            resend.emails().send(options);
        } catch (ResendException exception) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Unable to send email right now. Check Resend configuration and try again.",
                    exception
            );
        }
    }
}

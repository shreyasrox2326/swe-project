package com.example.demo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;

@Service
public class EmailDeliveryService {

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String relayUrl;
    private final String relaySecret;

    public EmailDeliveryService(
            ObjectMapper objectMapper,
            @Value("${app.mail.relay-url}") String relayUrl,
            @Value("${app.mail.relay-secret}") String relaySecret
    ) {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.objectMapper = objectMapper;
        this.relayUrl = relayUrl;
        this.relaySecret = relaySecret;
    }

    public void sendPlainText(String to, String subject, String body) {
        sendPlainText(to, subject, body, List.of());
    }

    public void sendPlainText(String to, String subject, String body, List<MailAttachment> attachments) {
        try {
            String payload = objectMapper.writeValueAsString(Map.of(
                    "to", to,
                    "subject", subject,
                    "text", body,
                    "attachments", attachments
            ));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(relayUrl))
                    .timeout(Duration.ofSeconds(15))
                    .header("Content-Type", "application/json")
                    .header("x-mail-relay-secret", relaySecret)
                    .POST(HttpRequest.BodyPublishers.ofString(payload))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new ResponseStatusException(
                        HttpStatus.SERVICE_UNAVAILABLE,
                        "Unable to send email right now. Mail relay returned " + response.statusCode() + "."
                );
            }
        } catch (JsonProcessingException exception) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Unable to prepare email payload.",
                    exception
            );
        } catch (IOException | InterruptedException exception) {
            if (exception instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Unable to send email right now. Check mail relay configuration and try again.",
                    exception
            );
        }
    }
}

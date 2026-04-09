package com.qburst.training.personalfinancetracker.service.mail;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
public class MailgunMailService implements MailService {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${app.mailgun.enabled:false}")
    private boolean enabled;

    @Value("${app.mailgun.base-url:https://api.mailgun.net}")
    private String baseUrl;

    @Value("${app.mailgun.fail-open:true}")
    private boolean failOpen;

    @Value("${app.mailgun.domain:}")
    private String domain;

    @Value("${app.mailgun.api-key:}")
    private String apiKey;

    @Value("${app.mailgun.from:}")
    private String from;

    @Value("${app.mailgun.sandbox-recipient:}")
    private String sandboxRecipient;

    @Override
    public void sendLoginOtp(String recipientEmail, String recipientName, String otpCode) {
        if (!enabled) {
            log.info("Mailgun disabled. OTP for {} is generated but not sent.", recipientEmail);
            return;
        }

        validateMailgunConfiguration();

        String to = resolveRecipient(recipientEmail);
        String endpoint = String.format("%s/v3/%s/messages", baseUrl, domain);

        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth("api", apiKey);
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("from", from);
        body.add("to", to);
        body.add("subject", "Your FinTrack Login OTP");
        body.add("text", String.format(
                "Hello %s,\n\nYour OTP for FinTrack login is: %s\nThis OTP is valid for 5 minutes.\n\nIf you did not request this, please ignore this email.",
                recipientName,
                otpCode
        ));

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(endpoint, new HttpEntity<>(body, headers), String.class);
            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new IllegalStateException("Failed to send OTP email via Mailgun");
            }
        } catch (Exception ex) {
            if (failOpen) {
                log.warn("Mailgun OTP delivery failed for recipient {}. Proceeding due to fail-open mode.", to, ex);
                return;
            }
            throw ex;
        }
    }

    private void validateMailgunConfiguration() {
        if (isBlank(domain) || isBlank(apiKey) || isBlank(from)) {
            throw new IllegalStateException("Mailgun is enabled but required configuration values are missing");
        }
    }

    private String resolveRecipient(String recipientEmail) {
        if (domain.startsWith("sandbox") && !isBlank(sandboxRecipient)) {
            return sandboxRecipient;
        }
        return recipientEmail;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}

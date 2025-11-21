package com.fiadopay.backend.webhook;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

@Service
public class WebhookSignatureService {
    private final String secret;

    public WebhookSignatureService(@Value("${webhook.secret}") String secret) {
        this.secret = secret;
    }

    public boolean isValid(String body, String signature) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] hmac = mac.doFinal(body.getBytes(StandardCharsets.UTF_8));
            String hex = toHex(hmac);
            return hex.equalsIgnoreCase(signature);
        } catch (Exception e) {
            return false;
        }
    }

    private String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
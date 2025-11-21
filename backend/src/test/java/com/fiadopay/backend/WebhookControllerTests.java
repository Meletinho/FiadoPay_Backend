package com.fiadopay.backend;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class WebhookControllerTests {

    @Autowired
    private MockMvc mockMvc;

    private String hmac(String secret, String body) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        byte[] h = mac.doFinal(body.getBytes(StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder(h.length * 2);
        for (byte b : h) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    @Test
    void webhookInvalidSignatureReturns400() throws Exception {
        String body = "{\"paymentId\":\"00000000-0000-0000-0000-000000000000\",\"status\":\"APPROVED\"}";
        mockMvc.perform(post("/webhook/provider")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer x")
                .header("X-Signature", "deadbeef")
                .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void webhookValidSignatureReturns200() throws Exception {
        String body = "{\"paymentId\":\"00000000-0000-0000-0000-000000000000\",\"status\":\"APPROVED\"}";
        String sig = hmac("change_me", body);
        mockMvc.perform(post("/webhook/provider")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer x")
                .header("X-Signature", sig)
                .content(body))
                .andExpect(status().isOk());
    }
}
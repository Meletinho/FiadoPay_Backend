package com.fiadopay.backend.webhook;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fiadopay.backend.entity.Payment;
import com.fiadopay.backend.entity.PaymentStatus;
import com.fiadopay.backend.repository.PaymentRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/webhook")
public class WebhookController {
    private final WebhookSignatureService signatureService;
    private final PaymentRepository paymentRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public WebhookController(WebhookSignatureService signatureService, PaymentRepository paymentRepository) {
        this.signatureService = signatureService;
        this.paymentRepository = paymentRepository;
    }

    @PostMapping("/provider")
    public ResponseEntity<Void> handle(@RequestBody String body, @RequestHeader("X-Signature") String signature) {
        if (!signatureService.isValid(body, signature)) {
            return ResponseEntity.badRequest().build();
        }
        try {
            JsonNode node = objectMapper.readTree(body);
            if (node.has("paymentId") && node.has("status")) {
                UUID id = UUID.fromString(node.get("paymentId").asText());
                String status = node.get("status").asText();
                Optional<Payment> opt = paymentRepository.findById(id);
                if (opt.isPresent()) {
                    Payment p = opt.get();
                    if ("APPROVED".equalsIgnoreCase(status)) {
                        p.setStatus(PaymentStatus.APPROVED);
                    } else if ("DECLINED".equalsIgnoreCase(status)) {
                        p.setStatus(PaymentStatus.DECLINED);
                    } else if ("SETTLED".equalsIgnoreCase(status)) {
                        p.setStatus(PaymentStatus.SETTLED);
                    }
                    paymentRepository.save(p);
                }
            }
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
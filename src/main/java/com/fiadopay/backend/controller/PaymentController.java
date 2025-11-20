package com.fiadopay.backend.controller;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fiadopay.backend.dto.PaymentRequest;
import com.fiadopay.backend.dto.PaymentResponse;
import com.fiadopay.backend.entity.Payment;
import com.fiadopay.backend.service.PaymentService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/payments")
@Validated
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    public ResponseEntity<PaymentResponse> create(@Valid @RequestBody PaymentRequest request,
                                                  @RequestHeader(name = "X-Idempotency-Key") String idempotencyKey) {
        Payment p = paymentService.create(request, idempotencyKey);
        return new ResponseEntity<>(toResponse(p), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentResponse> getById(@PathVariable("id") UUID id) {
        return paymentService.findById(id)
                .map(e -> ResponseEntity.ok(toResponse(e)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @GetMapping
    public ResponseEntity<List<PaymentResponse>> list() {
        List<PaymentResponse> list = paymentService.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    private PaymentResponse toResponse(Payment p) {
        PaymentResponse r = new PaymentResponse();
        r.setId(p.getId());
        r.setAmount(p.getAmount());
        r.setTotalAmount(p.getTotalAmount());
        r.setInterestRate(p.getInterestRate());
        r.setCurrency(p.getCurrency());
        r.setMethod(p.getMethod());
        r.setInstallments(p.getInstallments());
        r.setStatus(p.getStatus());
        r.setCreatedAt(p.getCreatedAt());
        r.setUpdatedAt(p.getUpdatedAt());
        r.setDeclineReason(p.getDeclineReason());
        return r;
    }
}

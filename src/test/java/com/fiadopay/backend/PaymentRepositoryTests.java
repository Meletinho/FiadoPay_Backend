package com.fiadopay.backend;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import com.fiadopay.backend.entity.Payment;
import com.fiadopay.backend.entity.PaymentMethodType;
import com.fiadopay.backend.entity.PaymentStatus;
import com.fiadopay.backend.repository.PaymentRepository;

@DataJpaTest
class PaymentRepositoryTests {

    @Autowired
    private PaymentRepository paymentRepository;

    private Payment buildPayment(String key) {
        Payment p = new Payment();
        p.setAmount(new BigDecimal("100.00"));
        p.setCurrency("BRL");
        p.setMethod(PaymentMethodType.CARD);
        p.setInstallments(1);
        p.setInterestRate(BigDecimal.ZERO);
        p.setTotalAmount(new BigDecimal("100.00"));
        p.setStatus(PaymentStatus.PENDING);
        p.setIdempotencyKey(key);
        return p;
    }

    @Test
    void saveGeneratesIdAndPersists() {
        String key = UUID.randomUUID().toString();
        Payment saved = paymentRepository.save(buildPayment(key));
        assertNotNull(saved.getId());
        assertEquals("BRL", saved.getCurrency());
    }

    @Test
    void findByIdempotencyKeyReturnsPayment() {
        String key = UUID.randomUUID().toString();
        Payment saved = paymentRepository.save(buildPayment(key));
        Optional<Payment> found = paymentRepository.findByIdempotencyKey(key);
        assertTrue(found.isPresent());
        assertEquals(saved.getId(), found.get().getId());
    }

    @Test
    void uniqueIdempotencyKeyConstraint() {
        String key = UUID.randomUUID().toString();
        paymentRepository.save(buildPayment(key));
        Payment duplicate = buildPayment(key);
        assertThrows(DataIntegrityViolationException.class, () -> {
            paymentRepository.saveAndFlush(duplicate);
        });
    }

    @Test
    void timestampsAreSetOnCreateAndUpdate() throws Exception {
        String key = UUID.randomUUID().toString();
        Payment p = paymentRepository.saveAndFlush(buildPayment(key));
        Payment reloaded = paymentRepository.findById(p.getId()).orElseThrow();
        Instant created = reloaded.getCreatedAt();
        Instant updated = reloaded.getUpdatedAt();
        assertNotNull(created);
        assertNotNull(updated);
        Thread.sleep(5);
        reloaded.setStatus(PaymentStatus.APPROVED);
        paymentRepository.saveAndFlush(reloaded);
        Payment afterUpdate = paymentRepository.findById(reloaded.getId()).orElseThrow();
        assertTrue(afterUpdate.getUpdatedAt().isAfter(updated));
    }
}
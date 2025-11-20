package com.fiadopay.backend;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
class PaymentControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void postPaymentCreatesWithValidHeadersAndBody() throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("amount", 100);
        body.put("currency", "BRL");
        body.put("method", "CARD");
        body.put("installments", 1);
        String json = objectMapper.writeValueAsString(body);

        String idem = UUID.randomUUID().toString();

        mockMvc.perform(post("/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer x")
                .header("X-Idempotency-Key", idem)
                .content(json))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.currency").value("BRL"))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.amount").value(100));
    }

    @Test
    void postPaymentReturns401WithoutAuthorization() throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("amount", 100);
        body.put("currency", "BRL");
        body.put("method", "CARD");
        body.put("installments", 1);
        String json = objectMapper.writeValueAsString(body);

        mockMvc.perform(post("/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void postPaymentReturns400WithInvalidBody() throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("amount", -1);
        body.put("currency", "BR");
        body.put("method", "CARD");
        body.put("installments", 0);
        String json = objectMapper.writeValueAsString(body);

        mockMvc.perform(post("/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer x")
                .header("X-Idempotency-Key", UUID.randomUUID().toString())
                .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getPaymentsReturnsListAfterCreate() throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("amount", 50);
        body.put("currency", "BRL");
        body.put("method", "PIX");
        body.put("installments", 1);
        String json = objectMapper.writeValueAsString(body);

        mockMvc.perform(post("/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer x")
                .header("X-Idempotency-Key", UUID.randomUUID().toString())
                .content(json))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/payments")
                .header("Authorization", "Bearer x"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void getPaymentByIdReturns404WhenNotFound() throws Exception {
        mockMvc.perform(get("/payments/" + UUID.randomUUID())
                .header("Authorization", "Bearer x"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getPaymentByIdReturns200WhenExists() throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("amount", 75);
        body.put("currency", "BRL");
        body.put("method", "BOLETO");
        body.put("installments", 1);
        String json = objectMapper.writeValueAsString(body);

        MvcResult result = mockMvc.perform(post("/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer x")
                .header("X-Idempotency-Key", UUID.randomUUID().toString())
                .content(json))
                .andExpect(status().isCreated())
                .andReturn();

        Map<?, ?> response = objectMapper.readValue(result.getResponse().getContentAsString(), Map.class);
        String id = response.get("id").toString();

        mockMvc.perform(get("/payments/" + id)
                .header("Authorization", "Bearer x"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id));
    }
}
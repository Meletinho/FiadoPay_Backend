package com.fiadopay.backend.spi;

public interface WebhookSinkHandler {
    void handle(String payload);
}
package com.fiadopay.backend.webhook;

import com.fiadopay.backend.annotations.WebhookSink;
import com.fiadopay.backend.spi.WebhookSinkHandler;

@WebhookSink(name = "Default", endpoint = "/webhook/provider")
public class DefaultSink implements WebhookSinkHandler {
    @Override
    public void handle(String payload) {
    }
}
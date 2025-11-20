package com.fiadopay.backend.registry;

import java.util.Set;

import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.ClasspathHelper;
import org.springframework.stereotype.Component;

import com.fiadopay.backend.annotations.AntiFraud;
import com.fiadopay.backend.annotations.PaymentMethod;
import com.fiadopay.backend.annotations.WebhookSink;
import com.fiadopay.backend.entity.PaymentMethodType;
import com.fiadopay.backend.spi.AntiFraudRule;
import com.fiadopay.backend.spi.PaymentMethodHandler;
import com.fiadopay.backend.spi.WebhookSinkHandler;

import jakarta.annotation.PostConstruct;

@Component
public class AnnotationRegistry {
    private final PaymentMethodRegistry paymentMethodRegistry;
    private final AntiFraudRegistry antiFraudRegistry;
    private final WebhookSinkRegistry webhookSinkRegistry;

    public AnnotationRegistry(PaymentMethodRegistry paymentMethodRegistry,
                              AntiFraudRegistry antiFraudRegistry,
                              WebhookSinkRegistry webhookSinkRegistry) {
        this.paymentMethodRegistry = paymentMethodRegistry;
        this.antiFraudRegistry = antiFraudRegistry;
        this.webhookSinkRegistry = webhookSinkRegistry;
    }

    @PostConstruct
    public void scan() {
        ConfigurationBuilder config = new ConfigurationBuilder()
                .setScanners(Scanners.TypesAnnotated, Scanners.SubTypes)
                .setUrls(ClasspathHelper.forPackage("com.fiadopay.backend"));

        Reflections reflections = new Reflections(config);

        Set<Class<?>> pm = reflections.getTypesAnnotatedWith(PaymentMethod.class);
        for (Class<?> c : pm) {
            PaymentMethod ann = c.getAnnotation(PaymentMethod.class);
            if (PaymentMethodHandler.class.isAssignableFrom(c)) {
                paymentMethodRegistry.register(ann.type(), c.asSubclass(PaymentMethodHandler.class));
            }
        }

        Set<Class<?>> af = reflections.getTypesAnnotatedWith(AntiFraud.class);
        for (Class<?> c : af) {
            AntiFraud ann = c.getAnnotation(AntiFraud.class);
            if (AntiFraudRule.class.isAssignableFrom(c)) {
                antiFraudRegistry.register(ann.name(), ann.threshold(), c.asSubclass(AntiFraudRule.class));
            }
        }

        Set<Class<?>> wh = reflections.getTypesAnnotatedWith(WebhookSink.class);
        for (Class<?> c : wh) {
            WebhookSink ann = c.getAnnotation(WebhookSink.class);
            if (WebhookSinkHandler.class.isAssignableFrom(c)) {
                webhookSinkRegistry.register(ann.name(), ann.endpoint(), c.asSubclass(WebhookSinkHandler.class));
            }
        }
    }
}
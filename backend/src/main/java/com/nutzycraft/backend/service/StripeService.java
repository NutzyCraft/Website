package com.nutzycraft.backend.service;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;



@Service
public class StripeService {

    @Value("${stripe.api.key:missing_key}")
    private String apiKey;

    @Value("${stripe.currency:usd}")
    private String currency;

    @PostConstruct
    public void init() {
        Stripe.apiKey = apiKey;
    }

    public PaymentIntent createPaymentIntent(Long amountInCents, String description, String customerEmail) throws StripeException {
        PaymentIntentCreateParams params =
                PaymentIntentCreateParams.builder()
                        .setAmount(amountInCents)
                        .setCurrency(currency)
                        .setDescription(description)
                        .setReceiptEmail(customerEmail)
                        .setAutomaticPaymentMethods(
                                PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                        .setEnabled(true)
                                        .build()
                        )
                        .build();

        return PaymentIntent.create(params);
    }
}

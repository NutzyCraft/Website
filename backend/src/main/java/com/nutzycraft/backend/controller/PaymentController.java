package com.nutzycraft.backend.controller;

import com.nutzycraft.backend.service.StripeService;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/payment")
public class PaymentController {

    @Autowired
    private StripeService stripeService;

    @PostMapping("/create-payment-intent")
    public ResponseEntity<Map<String, String>> createPaymentIntent(@RequestBody Map<String, Object> request) {
        try {
            // safely parse amount to Long
            Object amountObj = request.get("amount");
            Long amount = 0L;
            if (amountObj instanceof Integer) {
                 amount = ((Integer) amountObj).longValue();
            } else if (amountObj instanceof Long) {
                 amount = (Long) amountObj;
            } else if (amountObj instanceof Double) {
                 amount = ((Double) amountObj).longValue();
            }

            String description = (String) request.getOrDefault("description", "NutzyCraft Payment");
            String email = (String) request.getOrDefault("email", "customer@example.com");

            PaymentIntent paymentIntent = stripeService.createPaymentIntent(amount, description, email);

            Map<String, String> responseData = new HashMap<>();
            responseData.put("clientSecret", paymentIntent.getClientSecret());
            return ResponseEntity.ok(responseData);
        } catch (StripeException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @org.springframework.beans.factory.annotation.Value("${stripe.public.key:missing_public_key}")
    private String stripePublicKey;

    @GetMapping("/config")
    public ResponseEntity<Map<String, String>> getConfig() {
        Map<String, String> response = new HashMap<>();
        response.put("publishableKey", stripePublicKey);
        return ResponseEntity.ok(response);
    }
}

package com.nutzycraft.backend.controller;

import com.nutzycraft.backend.entity.Transaction;
import com.nutzycraft.backend.entity.User;
import com.nutzycraft.backend.repository.UserRepository;
import com.nutzycraft.backend.service.PayHereService;
import com.nutzycraft.backend.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * PaymentController — Agency Model
 *
 * All payments route exclusively to the primary Nutzycraft Pvt Ltd merchant account.
 * There are NO split-payment, sub-merchant, or multi-vendor parameters.
 *
 * Endpoints:
 *  POST /api/payment/payhere/initiate  — Generate signed PayHere checkout params
 *  POST /api/payment/payhere/notify    — Receive and verify PayHere payment notification
 */
@RestController
@RequestMapping("/api/payment")
public class PaymentController {

    @Autowired
    private PayHereService payHereService;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private UserRepository userRepository;

    /**
     * Generates the signed parameters required for the PayHere checkout form POST.
     * Called by payment.html before redirecting the client to PayHere.
     *
     * Request body:
     *   { milestoneId, jobId, amount, currency, itemDescription, firstName, lastName, email, phone }
     *
     * Response:
     *   { merchantId, orderId, items, amount, currency, hash, returnUrl, cancelUrl, notifyUrl, live }
     */
    @PostMapping("/payhere/initiate")
    public ResponseEntity<Map<String, String>> initiatePayment(@RequestBody Map<String, Object> request) {
        try {
            String amount = String.valueOf(request.get("amount"));
            String itemDescription = (String) request.getOrDefault("itemDescription", "NutzyCraft Project Milestone");
            Long milestoneId = request.get("milestoneId") != null
                    ? Long.parseLong(String.valueOf(request.get("milestoneId")))
                    : null;

            Map<String, String> params = payHereService.generateCheckoutParams(amount, itemDescription, milestoneId);
            return ResponseEntity.ok(params);

        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * PayHere server-to-server notification endpoint.
     * Called by PayHere's servers when a payment succeeds (status_code=2) or fails.
     *
     * PayHere docs: https://support.payhere.lk/api-&-library/payhere-checkout#3-payment-notification
     *
     * This endpoint MUST return HTTP 200 with no body, or PayHere will retry.
     */
    @PostMapping("/payhere/notify")
    public ResponseEntity<Void> payHereNotify(
            @RequestParam String merchant_id,
            @RequestParam String order_id,
            @RequestParam String payment_id,
            @RequestParam String payhere_amount,
            @RequestParam String payhere_currency,
            @RequestParam String status_code,
            @RequestParam String md5sig,
            @RequestParam(required = false) String custom_1, // milestoneId
            @RequestParam(required = false) String custom_2  // clientEmail
    ) {
        // 1. Verify the notification signature
        boolean valid = payHereService.verifyNotification(merchant_id, order_id, payhere_amount,
                payhere_currency, status_code, md5sig);

        if (!valid) {
            // Invalid signature — ignore silently, return 200 to stop PayHere retries
            return ResponseEntity.ok().build();
        }

        // 2. Only process successful payments (status_code = 2)
        if (!"2".equals(status_code)) {
            return ResponseEntity.ok().build();
        }

        try {
            // 3. Record the inbound payment
            Long milestoneId = null;
            if (custom_1 != null && !custom_1.isBlank()) {
                try { milestoneId = Long.parseLong(custom_1); } catch (NumberFormatException ignored) {}
            }

            User client = null;
            if (custom_2 != null && !custom_2.isBlank()) {
                client = userRepository.findByEmail(custom_2).orElse(null);
            }

            String description = "PayHere Payment: " + order_id + " (Ref: " + payment_id + ")";
            double amount = Double.parseDouble(payhere_amount);

            paymentService.recordInboundPayment(description, client, amount, milestoneId);

        } catch (Exception e) {
            // Log but still return 200 to prevent PayHere from retrying endlessly
            System.err.println("[PayHere Notify] Error processing notification: " + e.getMessage());
        }

        return ResponseEntity.ok().build();
    }
}

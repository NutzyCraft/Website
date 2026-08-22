package com.nutzycraft.backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * PayHere Integration Service (Agency Model).
 *
 * All payments route 100% to the primary Nutzycraft merchant account.
 * There are NO sub-merchant, split-payment, or transfer_data parameters.
 *
 * PayHere checkout flow:
 *  1. Backend generates a signed hash using merchant_id + order_id + amount + currency + MD5(merchant_secret).
 *  2. Frontend receives the hash and submits a form POST to PayHere's checkout URL.
 *  3. PayHere calls our notify_url (webhook) on payment success.
 *  4. We verify the payment on the notify endpoint and record the INBOUND_PAYHERE transaction.
 *
 * PayHere docs: https://support.payhere.lk/api-&-library/payhere-checkout
 */
@Service
public class PayHereService {

    @Value("${payhere.merchant.id:TEST_MERCHANT_ID}")
    private String merchantId;

    @Value("${payhere.merchant.secret:TEST_MERCHANT_SECRET}")
    private String merchantSecret;

    @Value("${payhere.live:false}")
    private boolean live;

    @Value("${app.url:http://localhost:8080}")
    private String appUrl;

    /**
     * Generates all parameters needed for the PayHere checkout form.
     * The hash is computed as:
     *   MD5( merchant_id + order_id + amount_formatted + currency + MD5(merchant_secret).toUpperCase() )
     *
     * @param amountLkr        Amount in LKR (as a string, e.g. "1500.00")
     * @param itemDescription  Description of the milestone/project
     * @param milestoneId      Milestone ID for tracking (used as part of order_id)
     */
    public Map<String, String> generateCheckoutParams(String amountLkr, String itemDescription, Long milestoneId) {
        String orderId = "NC-" + (milestoneId != null ? milestoneId : UUID.randomUUID().toString().substring(0, 8).toUpperCase());

        // Format amount to 2 decimal places as required by PayHere
        String formattedAmount;
        try {
            formattedAmount = new BigDecimal(amountLkr).setScale(2, RoundingMode.HALF_UP).toPlainString();
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid amount: " + amountLkr);
        }

        String currency = "LKR";
        String hash = generateHash(orderId, formattedAmount, currency);

        Map<String, String> params = new HashMap<>();
        params.put("merchantId", merchantId);
        params.put("orderId", orderId);
        params.put("items", itemDescription);
        params.put("amount", formattedAmount);
        params.put("currency", currency);
        params.put("hash", hash);
        params.put("returnUrl", appUrl + "/client-track-job.html?payment=success");
        params.put("cancelUrl", appUrl + "/client-track-job.html?payment=cancelled");
        params.put("notifyUrl", appUrl + "/api/payment/payhere/notify");
        params.put("live", String.valueOf(live));

        return params;
    }

    /**
     * Verifies the PayHere notify (webhook) call to confirm a payment is genuine.
     * PayHere sends: merchant_id, order_id, payment_id, payhere_amount, payhere_currency,
     *               status_code, md5sig
     *
     * Verification hash: MD5( merchant_id + order_id + payhere_amount + payhere_currency + status_code + MD5(merchant_secret).toUpperCase() )
     */
    public boolean verifyNotification(String merchantIdReceived, String orderId, String amount,
            String currency, String statusCode, String receivedMd5Sig) {
        try {
            String secretHash = md5(merchantSecret).toUpperCase();
            String localSig = md5(merchantIdReceived + orderId + amount + currency + statusCode + secretHash).toUpperCase();
            return localSig.equals(receivedMd5Sig);
        } catch (Exception e) {
            return false;
        }
    }

    private String generateHash(String orderId, String amount, String currency) {
        try {
            String secretHash = md5(merchantSecret).toUpperCase();
            return md5(merchantId + orderId + amount + currency + secretHash).toUpperCase();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate PayHere hash", e);
        }
    }

    private String md5(String input) throws Exception {
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] bytes = md.digest(input.getBytes(StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    public boolean isLive() {
        return live;
    }
}

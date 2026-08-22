package com.nutzycraft.backend.controller;

import com.nutzycraft.backend.entity.Transaction;
import com.nutzycraft.backend.repository.TransactionRepository;
import com.nutzycraft.backend.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private AdminService adminService;

    /**
     * Returns the logged-in freelancer's transaction history (OUTBOUND_MANUAL records
     * represent their payout entries).
     */
    @GetMapping
    public List<Transaction> getMyTransactions() {
        return transactionRepository.findByRelatedUser_EmailOrderByDateDesc(
                com.nutzycraft.backend.security.CurrentUser.email());
    }

    /**
     * Freelancer requests a manual bank transfer for an approved (PENDING) milestone payout.
     * Transitions status: PENDING → PROCESSING, which surfaces in the admin Pending Payouts table.
     *
     * POST /api/transactions/{id}/request-payout
     */
    @PostMapping("/{id}/request-payout")
    public ResponseEntity<?> requestPayout(@PathVariable Long id) {
        try {
            String email = com.nutzycraft.backend.security.CurrentUser.email();
            adminService.createPayoutRequest(id, email);
            return ResponseEntity.ok(Map.of("message", "Payout requested successfully. Admin will process your bank transfer shortly."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}

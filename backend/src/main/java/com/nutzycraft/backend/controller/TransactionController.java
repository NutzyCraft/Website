package com.nutzycraft.backend.controller;

import com.nutzycraft.backend.entity.Transaction;
import com.nutzycraft.backend.repository.TransactionRepository;
import com.nutzycraft.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public List<Transaction> getMyTransactions() {
        return transactionRepository.findByRelatedUser_EmailOrderByDateDesc(com.nutzycraft.backend.security.CurrentUser.email());
    }

    // Add dummy transaction for testing if empty
    @PostMapping("/test-seed")
    public Transaction seedTestTransaction() {
        Transaction t = new Transaction();
        t.setRelatedUser(userRepository.findByEmail(com.nutzycraft.backend.security.CurrentUser.email()).orElseThrow());
        t.setDescription("Test Payment");
        t.setAmount(500.0);
        t.setType(Transaction.TransactionType.CREDIT);
        t.setStatus(Transaction.TransactionStatus.PROCESSED);
        t.setDate(LocalDateTime.now());
        return transactionRepository.save(t);
    }
}

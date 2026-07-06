package com.nutzycraft.backend.controller;

import com.nutzycraft.backend.entity.Transaction;
import com.nutzycraft.backend.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    @Autowired
    private TransactionRepository transactionRepository;

    @GetMapping
    public List<Transaction> getMyTransactions() {
        return transactionRepository.findByRelatedUser_EmailOrderByDateDesc(com.nutzycraft.backend.security.CurrentUser.email());
    }
}

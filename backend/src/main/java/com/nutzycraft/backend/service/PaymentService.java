package com.nutzycraft.backend.service;

import com.nutzycraft.backend.entity.Job;
import com.nutzycraft.backend.entity.SystemSetting;
import com.nutzycraft.backend.entity.Transaction;
import com.nutzycraft.backend.repository.JobRepository;
import com.nutzycraft.backend.repository.SystemSettingRepository;
import com.nutzycraft.backend.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class PaymentService {

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private SystemSettingRepository systemSettingRepository;

    @Transactional
    public void completeJob(Long jobId) {
        if (jobId == null) {
            throw new IllegalArgumentException("Job ID cannot be null");
        }
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found"));

        if (!"IN_PROGRESS".equals(job.getStatus())) {
            throw new RuntimeException("Job must be IN_PROGRESS to complete");
        }

        if (job.getFreelancer() == null) {
            throw new RuntimeException("No freelancer assigned to this job");
        }

        double budget = job.getBudget();

        // 1. Get Platform Fee
        String feeStr = systemSettingRepository.findById("platform_fee")
                .map(SystemSetting::getValue)
                .orElse("10"); // Default 10%
        double feePercentage = 10.0;
        try {
            feePercentage = Double.parseDouble(feeStr);
        } catch (NumberFormatException e) {
            // ignore, use default
        }

        double commission = budget * (feePercentage / 100.0);
        double payoutAmount = budget - commission;

        // 2. Create Transaction: Client pays total (CREDIT to System/Revenue)
        Transaction clientTx = new Transaction();
        clientTx.setDescription("Job Payment: " + job.getTitle());
        clientTx.setRelatedUser(job.getClient());
        clientTx.setAmount(budget);
        clientTx.setType(Transaction.TransactionType.CREDIT);
        clientTx.setStatus(Transaction.TransactionStatus.RECEIVED);
        clientTx.setDate(LocalDateTime.now());
        transactionRepository.save(clientTx);

        // 3. Create Transaction: Payout to Freelancer (DEBIT from System)
        Transaction freelancerTx = new Transaction();
        freelancerTx.setDescription("Payout for Job: " + job.getTitle());
        freelancerTx.setRelatedUser(job.getFreelancer());
        freelancerTx.setAmount(payoutAmount);
        freelancerTx.setType(Transaction.TransactionType.DEBIT);
        freelancerTx.setStatus(Transaction.TransactionStatus.PENDING); // Pending admin approval/payout
        freelancerTx.setDate(LocalDateTime.now());
        transactionRepository.save(freelancerTx);

        // 4. Update Job Status
        job.setStatus("COMPLETED");
        jobRepository.save(job);
    }
}

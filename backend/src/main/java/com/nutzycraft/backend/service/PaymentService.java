package com.nutzycraft.backend.service;

import com.nutzycraft.backend.entity.Freelancer;
import com.nutzycraft.backend.entity.Job;
import com.nutzycraft.backend.entity.SystemSetting;
import com.nutzycraft.backend.entity.Transaction;
import com.nutzycraft.backend.repository.FreelancerRepository;
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

    @Autowired
    private FreelancerRepository freelancerRepository;

    /**
     * Called when a client approves a milestone / completes a job.
     *
     * Flow (Agency Model):
     *  1. Record the full client payment as INBOUND_PAYHERE (money received by NutzyCraft).
     *  2. Calculate the subcontractor share based on the Agency Margin setting.
     *  3. Queue an OUTBOUND_MANUAL transaction (PENDING) so the admin knows to
     *     perform a manual bank transfer to the freelancer.
     *  4. Mark the job as COMPLETED.
     *
     * The client never sees the margin split — they only see the total project price.
     */
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

        // 1. Read Agency Margin (internal — never shown to clients)
        String marginStr = systemSettingRepository.findById("platform_fee")
                .map(SystemSetting::getValue)
                .orElse("3"); // Default 3% agency margin
        double marginPercentage = 3.0;
        try {
            marginPercentage = Double.parseDouble(marginStr);
        } catch (NumberFormatException e) {
            // use default
        }

        double agencyMargin = budget * (marginPercentage / 100.0);
        double subcontractorPayout = budget - agencyMargin;

        // 2. Record inbound payment from client (via PayHere)
        Transaction inboundTx = new Transaction();
        inboundTx.setDescription("Milestone Payment: " + job.getTitle());
        inboundTx.setRelatedUser(job.getClient());
        inboundTx.setAmount(budget);
        inboundTx.setType(Transaction.TransactionType.INBOUND_PAYHERE);
        inboundTx.setStatus(Transaction.TransactionStatus.RECEIVED);
        inboundTx.setDate(LocalDateTime.now());
        transactionRepository.save(inboundTx);

        // 3. Queue outbound manual payout to subcontractor
        // Freelancer is looked up by their associated user to get the Freelancer entity id
        Freelancer freelancer = freelancerRepository.findByUser_Id(job.getFreelancer().getId()).orElse(null);

        Transaction payoutTx = new Transaction();
        payoutTx.setDescription("Subcontractor Payout: " + job.getTitle());
        payoutTx.setRelatedUser(job.getFreelancer());
        payoutTx.setAmount(subcontractorPayout);
        payoutTx.setType(Transaction.TransactionType.OUTBOUND_MANUAL);
        payoutTx.setStatus(Transaction.TransactionStatus.PENDING); // Queued for admin bank transfer
        payoutTx.setDate(LocalDateTime.now());
        if (freelancer != null) {
            payoutTx.setFreelancerId(freelancer.getId());
        }
        transactionRepository.save(payoutTx);

        // 4. Update Job Status
        job.setStatus("COMPLETED");
        jobRepository.save(job);
    }

    /**
     * Records a confirmed inbound PayHere payment for a specific milestone.
     * Called from the PaymentController webhook/notify handler.
     */
    @Transactional
    public Transaction recordInboundPayment(String description, com.nutzycraft.backend.entity.User client,
            double amount, Long milestoneId) {
        Transaction tx = new Transaction();
        tx.setDescription(description);
        tx.setRelatedUser(client);
        tx.setAmount(amount);
        tx.setType(Transaction.TransactionType.INBOUND_PAYHERE);
        tx.setStatus(Transaction.TransactionStatus.RECEIVED);
        tx.setDate(LocalDateTime.now());
        tx.setMilestoneId(milestoneId);
        return transactionRepository.save(tx);
    }
}

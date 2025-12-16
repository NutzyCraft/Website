package com.nutzycraft.backend.controller;

import com.nutzycraft.backend.entity.Job;
import com.nutzycraft.backend.entity.Proposal;
import com.nutzycraft.backend.entity.User;
import com.nutzycraft.backend.repository.JobRepository;
import com.nutzycraft.backend.repository.ProposalRepository;
import com.nutzycraft.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/proposals")
@CrossOrigin(origins = "*")
public class ProposalController {

    @Autowired
    private ProposalRepository proposalRepository;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/my-proposals")
    public List<Proposal> getMyProposals(@RequestParam String email) {
        return proposalRepository.findByFreelancerEmail(email);
    }

    @GetMapping("/job/{jobId}")
    public List<Proposal> getProposalsByJob(@PathVariable Long jobId) {
        return proposalRepository.findByJobId(jobId);
    }

    @PostMapping
    public Proposal createProposal(@RequestBody ProposalRequest request) {
        if (request.getEmail() == null || request.getJobId() == null) {
            throw new IllegalArgumentException("Email and Job ID are required");
        }

        User freelancer = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // request.getJobId() is checked for null above
        if (request.getJobId() == null) {
             throw new IllegalArgumentException("Job ID cannot be null");
        }
        Long jobId = request.getJobId();
        
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found"));

        Proposal proposal = new Proposal();
        proposal.setFreelancer(freelancer);
        proposal.setJob(job);
        proposal.setBidAmount(request.getBidAmount());
        proposal.setDeliveryTime(request.getDeliveryTime());
        proposal.setCoverLetter(request.getCoverLetter());

        return proposalRepository.save(proposal);
    }

    @PostMapping("/{id}/accept")
    public void acceptProposal(@PathVariable Long id) {
        Proposal proposal = proposalRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Proposal not found"));
        
        Job job = proposal.getJob();
        if (!"OPEN".equals(job.getStatus())) {
             throw new RuntimeException("Job is not open for assignment");
        }
        
        job.setFreelancer(proposal.getFreelancer());
        job.setBudget(proposal.getBidAmount()); // Update budget to agreed amount
        job.setStatus("IN_PROGRESS");
        jobRepository.save(job);
        
        // Optionally reject other proposals or notify users here
    }

    // Simple DTO for request
    public static class ProposalRequest {
        private String email;
        private Long jobId;
        private Double bidAmount;
        private String deliveryTime;
        private String coverLetter;

        // Getters and Setters
        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public Long getJobId() {
            return jobId;
        }

        public void setJobId(Long jobId) {
            this.jobId = jobId;
        }

        public Double getBidAmount() {
            return bidAmount;
        }

        public void setBidAmount(Double bidAmount) {
            this.bidAmount = bidAmount;
        }

        public String getDeliveryTime() {
            return deliveryTime;
        }

        public void setDeliveryTime(String deliveryTime) {
            this.deliveryTime = deliveryTime;
        }

        public String getCoverLetter() {
            return coverLetter;
        }

        public void setCoverLetter(String coverLetter) {
            this.coverLetter = coverLetter;
        }
    }
}

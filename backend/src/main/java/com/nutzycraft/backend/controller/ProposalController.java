package com.nutzycraft.backend.controller;

import com.nutzycraft.backend.entity.Job;
import com.nutzycraft.backend.entity.Proposal;
import com.nutzycraft.backend.entity.User;
import com.nutzycraft.backend.repository.JobRepository;
import com.nutzycraft.backend.repository.ProposalRepository;
import com.nutzycraft.backend.repository.UserRepository;
import com.nutzycraft.backend.entity.Notification;
import com.nutzycraft.backend.repository.NotificationRepository;
import com.nutzycraft.backend.service.EmailNotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/proposals")
public class ProposalController {

    @Autowired
    private ProposalRepository proposalRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailNotificationService emailNotificationService;

    @GetMapping("/my-proposals")
    public List<Proposal> getMyProposals() {
        return proposalRepository.findByFreelancerEmail(com.nutzycraft.backend.security.CurrentUser.email());
    }

    @GetMapping("/job/{jobId}")
    public List<Proposal> getProposalsByJob(@PathVariable Long jobId) {
        return proposalRepository.findByJobId(jobId);
    }

    @GetMapping("/{id}")
    public Proposal getProposalById(@PathVariable @NonNull Long id) {
        return proposalRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Proposal not found"));
    }

    @PostMapping
    public Proposal createProposal(@RequestBody ProposalRequest request) {
        if (request.getJobId() == null) {
            throw new IllegalArgumentException("Job ID is required");
        }

        // The proposal is always created for the authenticated user, never a body-supplied email
        User freelancer = userRepository.findByEmail(com.nutzycraft.backend.security.CurrentUser.email())
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (freelancer.getRole() != User.Role.FREELANCER) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.FORBIDDEN, "Only freelancers can submit proposals");
        }

        // request.getJobId() is checked for null above
        Long jobId = request.getJobId();
        if (jobId == null) {
            throw new IllegalArgumentException("Job ID cannot be null");
        }

        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found"));

        Proposal proposal = new Proposal();
        proposal.setFreelancer(freelancer);
        proposal.setJob(job);
        proposal.setBidAmount(request.getBidAmount());
        proposal.setDeliveryTime(request.getDeliveryTime());
        proposal.setCoverLetter(request.getCoverLetter());

        // Handle attachments (List<String> -> comma separated String)
        if (request.getAttachments() != null && !request.getAttachments().isEmpty()) {
            proposal.setAttachments(String.join(",", request.getAttachments()));
        }

        Proposal saved = proposalRepository.save(proposal);

        // Notify the client that a new bid has been placed on their job
        User client = job.getClient();
        if (client != null) {
            emailNotificationService.sendTemplateEmail(
                    client.getEmail(),
                    "freelancer-give-proposal",
                    Map.of(
                            "freelancer_name", freelancer.getFullName() != null ? freelancer.getFullName() : "A freelancer",
                            "client_name",    client.getFullName()    != null ? client.getFullName()    : "there",
                            "job_title",      job.getTitle()
                    )
            );
        }

        return saved;
    }

    @PostMapping("/{id}/accept")
    public void acceptProposal(@PathVariable @NonNull Long id) {
        Proposal proposal = proposalRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Proposal not found"));

        Job job = proposal.getJob();
        String callerEmail = com.nutzycraft.backend.security.CurrentUser.email();
        if (job.getClient() == null || !job.getClient().getEmail().equalsIgnoreCase(callerEmail)) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.FORBIDDEN, "Only the job's client can accept a proposal");
        }
        if (!"OPEN".equals(job.getStatus())) {
            throw new RuntimeException("Job is not open for assignment");
        }

        // 1. Assign freelancer and update job
        job.setFreelancer(proposal.getFreelancer());
        job.setBudget(proposal.getBidAmount()); // Update budget to agreed amount
        job.setStatus("IN_PROGRESS");
        jobRepository.save(job);

        // Create Notification
        Notification notification = new Notification();
        notification.setRecipient(proposal.getFreelancer());
        notification.setTitle("Proposal Accepted");
        notification.setMessage("Your proposal for '" + job.getTitle() + "' has been accepted!");
        notification.setType("INFO");
        notificationRepository.save(notification);

        // 2. Mark this proposal as ACCEPTED
        proposal.setStatus("ACCEPTED");
        proposalRepository.save(proposal);

        // 3. Mark all other proposals for this job as DECLINED
        List<Proposal> otherProposals = proposalRepository.findByJobId(job.getId());
        for (Proposal p : otherProposals) {
            if (!p.getId().equals(proposal.getId())) {
                p.setStatus("DECLINED");
                proposalRepository.save(p);
            }
        }

        // Notify the freelancer that their proposal was accepted
        User freelancer = proposal.getFreelancer();
        User client = job.getClient();
        if (freelancer != null && client != null) {
            emailNotificationService.sendTemplateEmail(
                    freelancer.getEmail(),
                    "client-accept-proposal",
                    Map.of(
                            "freelancer_name", freelancer.getFullName() != null ? freelancer.getFullName() : "there",
                            "client_name",     client.getFullName()     != null ? client.getFullName()     : "The client",
                            "job_title",       job.getTitle()
                    )
            );
        }
    }

    // Simple DTO for request
    public static class ProposalRequest {
        private String email;
        private Long jobId;
        private Double bidAmount;
        private String deliveryTime;
        private String coverLetter;
        private List<String> attachments;

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

        public List<String> getAttachments() {
            return attachments;
        }

        public void setAttachments(List<String> attachments) {
            this.attachments = attachments;
        }
    }
}

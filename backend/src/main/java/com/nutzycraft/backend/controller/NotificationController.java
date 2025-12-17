package com.nutzycraft.backend.controller;

import com.nutzycraft.backend.entity.Job;
import com.nutzycraft.backend.entity.Proposal;
import com.nutzycraft.backend.entity.User;
import com.nutzycraft.backend.repository.JobRepository;
import com.nutzycraft.backend.repository.ProposalRepository;
import com.nutzycraft.backend.repository.UserRepository;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = "*")
public class NotificationController {

    @Autowired
    private ProposalRepository proposalRepository;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public List<NotificationDTO> getNotifications(@RequestParam String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<NotificationDTO> notifications = new ArrayList<>();

        if (user.getRole() == User.Role.CLIENT) {
            // 1. Get Proposals for Client's Jobs
            List<Job> myJobs = jobRepository.findByClient_Email(email);
            for (Job job : myJobs) {
                List<Proposal> proposals = proposalRepository.findByJobId(job.getId());
                for (Proposal p : proposals) {
                    // Ideally check !p.isRead(), but for now showing all recent ones or unread ones
                    if (!p.isRead()) {
                        notifications.add(new NotificationDTO(
                                "New Proposal",
                                p.getFreelancer().getFullName() + " submitted a proposal for \"" + job.getTitle()
                                        + "\".",
                                "proposal",
                                p.getCreatedAt(),
                                "/client-job-proposals.html?jobId=" + job.getId() // Link action
                        ));
                    }
                }
            }
        } else if (user.getRole() == User.Role.FREELANCER) {
            // Freelancer logic (e.g. Job application accepted)
            List<Proposal> myProposals = proposalRepository.findByFreelancerEmail(email);
            for (Proposal p : myProposals) {
                if ("ACCEPTED".equalsIgnoreCase(p.getStatus())) { // Assuming Proposal has status or derived from Job
                    // Check if job is in progress
                    if ("IN_PROGRESS".equals(p.getJob().getStatus())) {
                        notifications.add(new NotificationDTO(
                                "Proposal Accepted",
                                "Your proposal for \"" + p.getJob().getTitle() + "\" was accepted!",
                                "job_accepted",
                                p.getJob().getPostedAt(), // Approximation
                                "/freelancer-my-jobs.html"));
                    }
                }
            }
        }

        // Sort by date desc
        notifications.sort((n1, n2) -> n2.getTimestamp().compareTo(n1.getTimestamp()));
        return notifications;
    }

    @PostMapping("/mark-read")
    public void markRead(@RequestParam String type, @RequestParam Long id) {
        // Implementation for marking specific items read
    }

    @Data
    public static class NotificationDTO {
        private String title;
        private String message;
        private String type; // proposal, system, message
        private LocalDateTime timestamp;
        private String actionLink;

        public NotificationDTO(String title, String message, String type, LocalDateTime timestamp, String actionLink) {
            this.title = title;
            this.message = message;
            this.type = type;
            this.timestamp = timestamp;
            this.actionLink = actionLink;
        }
    }
}

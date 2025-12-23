package com.nutzycraft.backend.controller;

import com.nutzycraft.backend.entity.Job;
import com.nutzycraft.backend.entity.Proposal;
import com.nutzycraft.backend.repository.JobRepository;
import com.nutzycraft.backend.entity.User;

import com.nutzycraft.backend.service.ChatService;
import com.nutzycraft.backend.dto.ConversationDTO;
import com.nutzycraft.backend.repository.ProposalRepository;
import com.nutzycraft.backend.repository.UserRepository;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(origins = "*")
public class DashboardController {

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private ProposalRepository proposalRepository;

    @Autowired
    private ChatService chatService;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/client")
    public ClientDashboardDTO getClientDashboard(@RequestParam String email) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));

        // Fetch jobs for client
        List<Job> jobs = jobRepository.findByClient_Email(email);

        long activeJobsCount = jobs.stream()
                .filter(j -> "OPEN".equalsIgnoreCase(j.getStatus()) || "IN_PROGRESS".equalsIgnoreCase(j.getStatus()))
                .count();
        long totalHires = jobs.stream().filter(j -> "COMPLETED".equalsIgnoreCase(j.getStatus())).count();
        long jobViews = jobs.size() * 10L; // Mock data
        double totalSpent = totalHires * 500.0; // Mock data

        // Get Active Projects (IN_PROGRESS)
        List<Job> activeProjects = jobs.stream()
                .filter(j -> "IN_PROGRESS".equalsIgnoreCase(j.getStatus()) || "OPEN".equalsIgnoreCase(j.getStatus()))
                .limit(5)
                .toList();

        // Get Recent Messages
        List<ConversationDTO> messages = chatService.getConversations(email).stream().limit(5).toList();

        ClientDashboardDTO stats = new ClientDashboardDTO();
        stats.setActiveJobs(activeJobsCount);
        stats.setTotalHires(totalHires);
        stats.setTotalSpent(totalSpent);
        stats.setJobViews(jobViews);
        stats.setActiveProjects(activeProjects);
        stats.setRecentMessages(messages);

        return stats;
    }

    @GetMapping("/freelancer")
    public FreelancerStatsDTO getFreelancerStats(@RequestParam String email) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));

        // Fetch proposals to calc stats
        List<Proposal> proposals = proposalRepository.findByFreelancerEmail(email);
        long activeProposals = proposals.stream().filter(p -> "ACCEPTED".equalsIgnoreCase(p.getStatus())).count();

        // Fetch actual Active/Completed Jobs
        List<Job> myJobs = jobRepository.findByFreelancer_Email(email);
        long activeJobsVal = myJobs.stream().filter(j -> "IN_PROGRESS".equalsIgnoreCase(j.getStatus())).count();
        long completedJobsVal = myJobs.stream().filter(j -> "COMPLETED".equalsIgnoreCase(j.getStatus())).count();

        // Calc earnings (Mock or from transactions)
        double totalEarnings = completedJobsVal * 500.0;

        // Get Active Orders (List)
        List<Job> activeOrders = myJobs.stream()
                .filter(j -> "IN_PROGRESS".equalsIgnoreCase(j.getStatus()))
                .toList();

        // Get Recent Messages
        // Get Recent Messages
        List<ConversationDTO> messages = chatService.getConversations(email).stream().limit(5).toList();

        // Get Recommended Jobs (Open jobs, exclude my jobs)
        List<Job> recommended = jobRepository.findAll().stream()
                .filter(j -> "OPEN".equalsIgnoreCase(j.getStatus()))
                .limit(5)
                .toList();

        FreelancerStatsDTO stats = new FreelancerStatsDTO();
        stats.setActiveJobs(activeJobsVal > 0 ? activeJobsVal : activeProposals); // Fallback to proposals if job
                                                                                  // assignment not fully used
        stats.setCompletedJobs(completedJobsVal);
        stats.setTotalEarnings(totalEarnings);
        stats.setRating(5.0);
        stats.setActiveOrders(activeOrders);
        stats.setRecentMessages(messages);
        stats.setRecommendedJobs(recommended);

        return stats;
    }

    @Data
    public static class ClientDashboardDTO {
        private long activeJobs;
        private long totalHires;
        private double totalSpent;
        private long jobViews;
        private List<Job> activeProjects;
        private List<ConversationDTO> recentMessages;
    }

    @Data
    public static class FreelancerStatsDTO {
        private double totalEarnings;
        private long completedJobs;
        private long activeJobs;
        private double rating;
        private List<Job> activeOrders;
        private List<ConversationDTO> recentMessages;
        private List<Job> recommendedJobs;
    }
}

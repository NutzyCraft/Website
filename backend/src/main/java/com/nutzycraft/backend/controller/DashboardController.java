package com.nutzycraft.backend.controller;

import com.nutzycraft.backend.entity.Job;
import com.nutzycraft.backend.entity.Proposal;
import com.nutzycraft.backend.repository.JobRepository;
import com.nutzycraft.backend.repository.ProposalRepository;
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

    @GetMapping("/client")
    public ClientStatsDTO getClientStats(@RequestParam String email) {
        // Fetch jobs for client
        List<Job> jobs = jobRepository.findByClient_Email(email);

        long activeJobs = jobs.stream().filter(j -> "OPEN".equalsIgnoreCase(j.getStatus())).count();
        long totalHires = jobs.stream().filter(j -> "COMPLETED".equalsIgnoreCase(j.getStatus())).count(); // Simplification
        long jobViews = jobs.size() * 10L; // Mock data
        double totalSpent = totalHires * 500.0; // Mock data

        ClientStatsDTO stats = new ClientStatsDTO();
        stats.setActiveJobs(activeJobs);
        stats.setTotalHires(totalHires);
        stats.setTotalSpent(totalSpent);
        stats.setJobViews(jobViews);
        return stats;
    }

    @GetMapping("/freelancer")
    public FreelancerStatsDTO getFreelancerStats(@RequestParam String email) {
        // Fetch proposals for freelancer (as proxy for activity)
        List<Proposal> proposals = proposalRepository.findByFreelancerEmail(email);

        long activeJobs = proposals.stream().filter(p -> "ACCEPTED".equalsIgnoreCase(p.getStatus())).count();
        long completedJobs = 0; // Need 'completed' status in Proposal or Job linkage
        double totalEarnings = activeJobs * 500.0; // Mock

        FreelancerStatsDTO stats = new FreelancerStatsDTO();
        stats.setActiveJobs(activeJobs);
        stats.setCompletedJobs(completedJobs);
        stats.setTotalEarnings(totalEarnings);
        stats.setRating(5.0); // Mock
        return stats;
    }

    @Data
    public static class ClientStatsDTO {
        private long activeJobs;
        private long totalHires;
        private double totalSpent;
        private long jobViews;
    }

    @Data
    public static class FreelancerStatsDTO {
        private double totalEarnings;
        private long completedJobs;
        private long activeJobs;
        private double rating;
    }
}

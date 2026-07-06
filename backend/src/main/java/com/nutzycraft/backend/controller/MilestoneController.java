package com.nutzycraft.backend.controller;

import com.nutzycraft.backend.entity.Job;
import com.nutzycraft.backend.entity.Milestone;
import com.nutzycraft.backend.entity.User;
import com.nutzycraft.backend.repository.JobRepository;
import com.nutzycraft.backend.repository.MilestoneRepository;
import com.nutzycraft.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/milestones")
public class MilestoneController {

    @Autowired
    private MilestoneRepository milestoneRepository;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/job/{jobId}")
    public List<Milestone> getMilestonesByJob(@PathVariable Long jobId) {
        return milestoneRepository.findByJobId(jobId);
    }

    private void requireJobParticipant(Job job) {
        String email = com.nutzycraft.backend.security.CurrentUser.email();
        boolean isClient = job.getClient() != null && job.getClient().getEmail().equalsIgnoreCase(email);
        boolean isFreelancer = job.getFreelancer() != null && job.getFreelancer().getEmail().equalsIgnoreCase(email);
        if (!isClient && !isFreelancer) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.FORBIDDEN, "You are not a participant on this job");
        }
    }

    @PostMapping
    public Milestone createMilestone(@RequestBody MilestoneRequest request) {
        Long jobId = request.getJobId();
        if (jobId == null) {
            throw new IllegalArgumentException("Job ID is required");
        }
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found"));
        requireJobParticipant(job);

        String creatorEmail = com.nutzycraft.backend.security.CurrentUser.email();
        User creator = userRepository.findByEmail(creatorEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Milestone milestone = new Milestone();
        milestone.setJob(job);
        milestone.setTitle(request.getTitle());
        milestone.setDescription(request.getDescription());
        milestone.setAmount(request.getAmount());
        milestone.setStatus("PENDING");
        milestone.setDueDate(request.getDueDate());
        milestone.setCreatedBy(creator.getFullName());
        milestone.setCreatedByEmail(creatorEmail);

        return milestoneRepository.save(milestone);
    }

    @PutMapping("/{id}/status")
    public Milestone updateStatus(@PathVariable @NonNull Long id, @RequestParam String status) {
        Milestone milestone = milestoneRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Milestone not found"));
        requireJobParticipant(milestone.getJob());
        milestone.setStatus(status);
        return milestoneRepository.save(milestone);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteMilestone(@PathVariable @NonNull Long id) {
        Milestone milestone = milestoneRepository.findById(id).orElse(null);
        if (milestone == null) {
            return ResponseEntity.notFound().build();
        }
        requireJobParticipant(milestone.getJob());
        milestoneRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }

    // DTO
    @lombok.Data
    public static class MilestoneRequest {
        private Long jobId;
        private String title;
        private String description;
        private Double amount;
        private java.time.LocalDate dueDate;
    }
}

package com.nutzycraft.backend.controller;

import com.nutzycraft.backend.entity.Job;
import com.nutzycraft.backend.entity.Milestone;
import com.nutzycraft.backend.entity.User;
import com.nutzycraft.backend.repository.JobRepository;
import com.nutzycraft.backend.repository.MilestoneRepository;
import com.nutzycraft.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/milestones")
@CrossOrigin(origins = "*")
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

    @PostMapping
    public Milestone createMilestone(@RequestBody MilestoneRequest request) {
        Job job = jobRepository.findById(request.getJobId())
                .orElseThrow(() -> new RuntimeException("Job not found"));

        User creator = userRepository.findByEmail(request.getCreatedByEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Milestone milestone = new Milestone();
        milestone.setJob(job);
        milestone.setTitle(request.getTitle());
        milestone.setDescription(request.getDescription());
        milestone.setAmount(request.getAmount());
        milestone.setStatus("PENDING");
        milestone.setDueDate(request.getDueDate());
        milestone.setCreatedBy(creator.getFullName());
        milestone.setCreatedByEmail(request.getCreatedByEmail());

        return milestoneRepository.save(milestone);
    }

    @PutMapping("/{id}/status")
    public Milestone updateStatus(@PathVariable Long id, @RequestParam String status) {
        Milestone milestone = milestoneRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Milestone not found"));
        milestone.setStatus(status);
        return milestoneRepository.save(milestone);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteMilestone(@PathVariable Long id) {
        if (!milestoneRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
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
        private String createdByEmail;
    }
}

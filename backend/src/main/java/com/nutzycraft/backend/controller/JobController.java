package com.nutzycraft.backend.controller;

import com.nutzycraft.backend.entity.Job;
import com.nutzycraft.backend.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/jobs")
@CrossOrigin(origins = "*")
public class JobController {

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private com.nutzycraft.backend.repository.UserRepository userRepository;

    @GetMapping
    public List<Job> getAllJobs() {
        return jobRepository.findAll();
    }

    @Autowired
    private com.nutzycraft.backend.repository.ProposalRepository proposalRepository;

    @GetMapping("/my-jobs")
    public List<JobWithProposalsDTO> getMyJobs(@RequestParam String email) {
        List<Job> jobs = jobRepository.findByClient_Email(email);
        return jobs.stream().map(job -> {
            long proposalCount = proposalRepository.countByJobId(job.getId());
            return new JobWithProposalsDTO(job, proposalCount);
        }).toList();
    }

    @lombok.Data
    public static class JobWithProposalsDTO {
        private Long id;
        private String title;
        private String description;
        private String category;
        private Double budget;
        private String duration;
        private String status;
        private java.time.LocalDateTime postedAt;
        private long proposalCount;

        public JobWithProposalsDTO(Job job, long proposalCount) {
            this.id = job.getId();
            this.title = job.getTitle();
            this.description = job.getDescription();
            this.category = job.getCategory();
            this.budget = job.getBudget();
            this.duration = job.getDuration();
            this.status = job.getStatus();
            this.postedAt = job.getPostedAt();
            this.proposalCount = proposalCount;
        }
    }

    @PostMapping
    public Job createJob(@RequestBody Job job, @RequestParam(required = false) String clientEmail) {
        if (job == null) {
            throw new IllegalArgumentException("Job cannot be null");
        }
        if (clientEmail != null) {
            userRepository.findByEmail(clientEmail).ifPresent(job::setClient);
        }
        return jobRepository.save(job);
    }

    @Autowired
    private com.nutzycraft.backend.service.PaymentService paymentService;

    @PostMapping("/{id}/complete")
    public void completeJob(@PathVariable Long id) {
        paymentService.completeJob(id);
    }
}

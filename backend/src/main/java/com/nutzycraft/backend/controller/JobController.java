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
    public List<Job> getAllJobs(@RequestParam(required = false) String search) {
        if (search != null && !search.isEmpty()) {
            return jobRepository.findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCaseAndStatusNot(search,
                    search, "COMPLETED");
        }
        return jobRepository.findByStatusNot("COMPLETED");
    }

    @GetMapping("/{id}")
    public Job getJobById(@PathVariable Long id) {
        return jobRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Job not found"));
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

    @PutMapping("/{id}/step")
    public Job updateJobStep(@PathVariable Long id, @RequestParam Integer step) {
        Job job = jobRepository.findById(id).orElseThrow(() -> new RuntimeException("Job not found"));
        // 1=Started, 2=Concepts, 3=Revisions, 4=Delivery
        if (step < 1 || step > 4) {
            throw new IllegalArgumentException("Invalid step");
        }
        job.setCurrentStep(step);
        if (step == 4) {
            job.setStatus("COMPLETED");
        }
        return jobRepository.save(job);
    }

    @Autowired
    private com.nutzycraft.backend.repository.FreelancerRepository freelancerRepository;

    @PostMapping("/{id}/review")
    public Job addReview(
            @PathVariable Long id,
            @RequestParam String reviewerEmail,
            @RequestParam Integer rating,
            @RequestParam String reviewText,
            @RequestParam String role // "CLIENT" or "FREELANCER" indicating WHO IS REVIEWING
    ) {
        Job job = jobRepository.findById(id).orElseThrow(() -> new RuntimeException("Job not found"));

        if (!job.getStatus().equals("COMPLETED")) {
            throw new RuntimeException("Job must be COMPLETED to leave a review.");
        }

        if (role.equalsIgnoreCase("CLIENT")) {
            // Client is reviewing Freelancer
            if (!job.getClient().getEmail().equalsIgnoreCase(reviewerEmail)) {
                throw new RuntimeException("Unauthorized: Reviewer email does not match Job Client");
            }
            job.setRatingForFreelancer(rating);
            job.setReviewForFreelancer(reviewText);
            jobRepository.save(job);

            // Update Freelancer Aggregate Rating
            if (job.getFreelancer() != null) {
                Double avg = jobRepository.getAverageRatingForFreelancer(job.getFreelancer().getEmail());
                com.nutzycraft.backend.entity.Freelancer f = freelancerRepository
                        .findByUser_Id(job.getFreelancer().getId())
                        .orElse(null);
                if (f != null && avg != null) {
                    f.setRating(avg);
                    freelancerRepository.save(f);
                }
            }

        } else if (role.equalsIgnoreCase("FREELANCER")) {
            // Freelancer is reviewing Client
            if (!job.getFreelancer().getEmail().equalsIgnoreCase(reviewerEmail)) {
                throw new RuntimeException("Unauthorized: Reviewer email does not match Job Freelancer");
            }
            job.setRatingForClient(rating);
            job.setReviewForClient(reviewText);
            jobRepository.save(job);

            // Note: Client entity doesn't have a persisted rating field yet, calculating on
            // fly in DTO.
        } else {
            throw new IllegalArgumentException("Invalid Role");
        }

        return job;
    }
}

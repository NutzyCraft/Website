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

    @GetMapping("/my-jobs")
    public List<Job> getMyJobs(@RequestParam String email) {
        return jobRepository.findByClient_Email(email);
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

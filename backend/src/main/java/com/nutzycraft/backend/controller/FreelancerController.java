package com.nutzycraft.backend.controller;

import com.nutzycraft.backend.entity.Freelancer;
import com.nutzycraft.backend.repository.FreelancerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@RequestMapping("/api/freelancers")
@CrossOrigin(origins = "*")
public class FreelancerController {

    @Autowired
    private FreelancerRepository freelancerRepository;

    // List all or search
    @GetMapping
    public List<Freelancer> getFreelancers(@RequestParam(required = false) String query) {
        if (query != null && !query.isEmpty()) {
            List<Freelancer> byTitle = freelancerRepository.findByTitleContainingIgnoreCase(query);
            List<Freelancer> bySkill = freelancerRepository.findBySkillsContaining(query);
            // Merge and de-duplicate
            return Stream.concat(byTitle.stream(), bySkill.stream())
                    .distinct()
                    .collect(Collectors.toList());
        }
        return freelancerRepository.findAll();
    }

    // Get specific profile
    @GetMapping("/{id}")
    public Freelancer getFreelancer(@PathVariable Long id) {
        return freelancerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Freelancer not found"));
    }

    @GetMapping("/me")
    public Freelancer getMyProfile(@RequestParam String email) {
        return freelancerRepository.findByUser_Email(email)
                .orElseThrow(() -> new RuntimeException("Freelancer not found"));
    }

    @PutMapping("/me")
    public Freelancer updateMyProfile(@RequestParam String email, @RequestBody Freelancer updatedFreelancer) {
        Freelancer existing = freelancerRepository.findByUser_Email(email)
                .orElseThrow(() -> new RuntimeException("Freelancer not found"));

        if (updatedFreelancer.getTitle() != null)
            existing.setTitle(updatedFreelancer.getTitle());
        if (updatedFreelancer.getBio() != null)
            existing.setBio(updatedFreelancer.getBio());
        if (updatedFreelancer.getHourlyRate() != null)
            existing.setHourlyRate(updatedFreelancer.getHourlyRate());
        if (updatedFreelancer.getSkills() != null)
            existing.setSkills(updatedFreelancer.getSkills());

        // Update user fields if provided
        if (updatedFreelancer.getUser() != null) {
            com.nutzycraft.backend.entity.User user = existing.getUser();
            if (updatedFreelancer.getUser().getFullName() != null) {
                user.setFullName(updatedFreelancer.getUser().getFullName());
            }
        }

        return freelancerRepository.save(existing);
    }
}

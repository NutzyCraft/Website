package com.nutzycraft.backend.controller;

import com.nutzycraft.backend.entity.Freelancer;
import com.nutzycraft.backend.repository.FreelancerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import com.nutzycraft.backend.dto.FreelancerDTO;
import com.nutzycraft.backend.dto.UserSummaryDTO;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@RequestMapping("/api/freelancers")
@CrossOrigin(origins = "*") // Allow all origins explicitly
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

    @GetMapping("/user/{userId}")
    public Freelancer getFreelancerByUserId(@PathVariable Long userId) {
        return freelancerRepository.findByUser_Id(userId)
                .orElseThrow(() -> new RuntimeException("Freelancer profile not found for user id: " + userId));
    }

    @GetMapping("/me")
    public FreelancerDTO getMyProfile(@RequestParam String email) {
        Freelancer freelancer = freelancerRepository.findByUser_Email(email)
                .orElseThrow(() -> new RuntimeException("Freelancer not found"));
        return mapToDTO(freelancer);
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
        if (updatedFreelancer.getProfileImage() != null)
            existing.setProfileImage(updatedFreelancer.getProfileImage());
        if (updatedFreelancer.getBannerImage() != null)
            existing.setBannerImage(updatedFreelancer.getBannerImage());

        // Update user fields if provided
        if (updatedFreelancer.getUser() != null) {
            com.nutzycraft.backend.entity.User user = existing.getUser();
            if (updatedFreelancer.getUser().getFullName() != null) {
                user.setFullName(updatedFreelancer.getUser().getFullName());
            }
        }

        return freelancerRepository.save(existing);
    }

    private FreelancerDTO mapToDTO(Freelancer freelancer) {
        FreelancerDTO dto = new FreelancerDTO();
        dto.setId(freelancer.getId());
        dto.setTitle(freelancer.getTitle());
        dto.setBio(freelancer.getBio());
        dto.setHourlyRate(freelancer.getHourlyRate());
        dto.setSkills(freelancer.getSkills());
        dto.setRating(freelancer.getRating());
        
        if (freelancer.getUser() != null) {
            UserSummaryDTO userDto = new UserSummaryDTO();
            userDto.setId(freelancer.getUser().getId());
            userDto.setFullName(freelancer.getUser().getFullName());
            userDto.setEmail(freelancer.getUser().getEmail());
            // No profile picture here
            dto.setUser(userDto);
        }
        return dto;
    }
}

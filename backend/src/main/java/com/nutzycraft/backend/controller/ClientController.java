package com.nutzycraft.backend.controller;

import com.nutzycraft.backend.entity.Client;
import com.nutzycraft.backend.repository.ClientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/clients")
@CrossOrigin(origins = "*")
public class ClientController {

    @Autowired
    private ClientRepository clientRepository;

    @GetMapping("/me")
    public org.springframework.http.ResponseEntity<?> getMyProfile(@RequestParam String email) {
        try {
            System.out.println("Fetching profile for: " + email);
            Client client = clientRepository.findByUser_Email(email)
                    .orElseThrow(() -> new RuntimeException("Client profile not found"));
            return org.springframework.http.ResponseEntity.ok(client);
        } catch (Exception e) {
            e.printStackTrace();
            return org.springframework.http.ResponseEntity.status(500)
                    .body(java.util.Collections.singletonMap("message", "Error fetching profile: " + e.getMessage()));
        }
    }

    @PutMapping("/me")
    public Client updateMyProfile(@RequestParam String email, @RequestBody Client updatedClient) {
        Client existing = clientRepository.findByUser_Email(email)
                .orElseThrow(() -> new RuntimeException("Client profile not found"));

        // Update fields
        existing.setCompanyName(updatedClient.getCompanyName());
        // Add more fields to Client entity if needed (contactPerson, billingAddress
        // etc.)
        // For now MVP just Company Name and Description from entity definition
        existing.setDescription(updatedClient.getDescription());
        existing.setWebsite(updatedClient.getWebsite());
        existing.setIndustry(updatedClient.getIndustry());
        existing.setContactPerson(updatedClient.getContactPerson());
        existing.setBillingAddress(updatedClient.getBillingAddress());
        existing.setProfileImage(updatedClient.getProfileImage());
        existing.setBannerImage(updatedClient.getBannerImage());

        return clientRepository.save(existing);
    }
}

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
    public Client getMyProfile(@RequestParam String email) {
        return clientRepository.findByUser_Email(email)
                .orElseThrow(() -> new RuntimeException("Client profile not found"));
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

        return clientRepository.save(existing);
    }
}

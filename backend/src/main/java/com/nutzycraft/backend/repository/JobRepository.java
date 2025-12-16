package com.nutzycraft.backend.repository;

import com.nutzycraft.backend.entity.Job;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.List;

public interface JobRepository extends JpaRepository<Job, Long> {
    List<Job> findByCategory(String category);

    long countByStatus(String status);

    List<Job> findByClient_Email(String email);

    // Added method as per instruction
    List<Job> findByClientEmail(String email);

    @org.springframework.data.jpa.repository.Query("SELECT SUM(j.budget) FROM Job j WHERE j.status = :status")
    Double sumBudgetByStatus(@org.springframework.data.repository.query.Param("status") String status);
}

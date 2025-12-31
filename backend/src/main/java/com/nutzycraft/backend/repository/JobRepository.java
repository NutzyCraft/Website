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

    List<Job> findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(String title, String description);

    List<Job> findByStatusNot(String status);

    List<Job> findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCaseAndStatusNot(String title,
            String description, String status);

    List<Job> findByFreelancer_Email(String email);

    long countByClient_EmailAndFreelancerIsNotNull(String email);

    @org.springframework.data.jpa.repository.Query("SELECT AVG(j.ratingForClient) FROM Job j WHERE j.client.email = :email AND j.ratingForClient IS NOT NULL")
    Double getAverageRatingForClient(@org.springframework.data.repository.query.Param("email") String email);

    @org.springframework.data.jpa.repository.Query("SELECT AVG(j.ratingForFreelancer) FROM Job j WHERE j.freelancer.email = :email AND j.ratingForFreelancer IS NOT NULL")
    Double getAverageRatingForFreelancer(@org.springframework.data.repository.query.Param("email") String email);

    // Optimized queries with eager fetching to prevent N+1 queries
    @org.springframework.data.jpa.repository.Query("SELECT j FROM Job j LEFT JOIN FETCH j.client LEFT JOIN FETCH j.freelancer WHERE j.client.email = :email")
    List<Job> findByClientEmailWithUsers(@org.springframework.data.repository.query.Param("email") String email);

    @org.springframework.data.jpa.repository.Query("SELECT j FROM Job j LEFT JOIN FETCH j.client LEFT JOIN FETCH j.freelancer WHERE j.freelancer.email = :email")
    List<Job> findByFreelancerEmailWithUsers(@org.springframework.data.repository.query.Param("email") String email);

    List<Job> findTop5ByStatusIgnoreCase(String status);
}

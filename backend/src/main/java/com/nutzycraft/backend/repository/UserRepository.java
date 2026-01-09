package com.nutzycraft.backend.repository;

import com.nutzycraft.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    
    // Custom queries for soft delete functionality
    @Query("SELECT u FROM User u WHERE u.deleted = true")
    List<User> findAllDeleted();
    
    @Query("SELECT u FROM User u WHERE u.email = :email AND u.deleted = true")
    Optional<User> findDeletedByEmail(String email);
    
    @Query("SELECT u FROM User u WHERE u.id = :id AND u.deleted = false")
    Optional<User> findActiveById(Long id);
}

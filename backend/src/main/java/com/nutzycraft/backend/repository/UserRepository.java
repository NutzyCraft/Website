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

    Optional<User> findByProviderId(String providerId);

    // Bypasses the @Where(deleted = false) filter so callers can find a user
    // regardless of soft-delete state (e.g. the Clerk user.deleted webhook).
    @Query(value = "SELECT * FROM users WHERE provider_id = :providerId", nativeQuery = true)
    Optional<User> findByProviderIdIncludingDeleted(String providerId);

    // Native query needed because @Where(deleted = false) is applied by Hibernate
    // to ALL JPQL queries against User, including ones with an explicit
    // "deleted = true" predicate — making findDeletedByEmail() always empty.
    @Query(value = "SELECT * FROM users WHERE email = :email", nativeQuery = true)
    Optional<User> findByEmailIncludingDeleted(String email);
}

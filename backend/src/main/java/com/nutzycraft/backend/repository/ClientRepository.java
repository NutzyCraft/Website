package com.nutzycraft.backend.repository;

import com.nutzycraft.backend.entity.Client;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClientRepository extends JpaRepository<Client, Long> {
    java.util.Optional<Client> findByUser(com.nutzycraft.backend.entity.User user);

    java.util.Optional<Client> findByUser_Email(String email);
}

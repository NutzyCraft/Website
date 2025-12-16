package com.nutzycraft.backend.repository;

import com.nutzycraft.backend.entity.Dispute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DisputeRepository extends JpaRepository<Dispute, Long> {
    long countByStatus(Dispute.DisputeStatus status);
}

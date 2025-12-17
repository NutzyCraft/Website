package com.nutzycraft.backend.repository;

import com.nutzycraft.backend.entity.Milestone;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MilestoneRepository extends JpaRepository<Milestone, Long> {
    List<Milestone> findByJobId(Long jobId);
}

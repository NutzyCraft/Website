package com.nutzycraft.backend.repository;

import com.nutzycraft.backend.entity.Proposal;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProposalRepository extends JpaRepository<Proposal, Long> {
    List<Proposal> findByFreelancerEmail(String email);

    List<Proposal> findByJobId(Long jobId);
}

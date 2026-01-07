package com.nutzycraft.backend.repository;

import com.nutzycraft.backend.entity.SharedFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SharedFileRepository extends JpaRepository<SharedFile, Long> {
    List<SharedFile> findByJobId(Long jobId);
    void deleteByJobId(Long jobId);
}

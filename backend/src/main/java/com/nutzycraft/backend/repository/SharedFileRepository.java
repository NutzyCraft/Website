package com.nutzycraft.backend.repository;

import com.nutzycraft.backend.entity.SharedFile;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SharedFileRepository extends JpaRepository<SharedFile, Long> {
    List<SharedFile> findByJobIdOrderByUploadedAtDesc(Long jobId);
}

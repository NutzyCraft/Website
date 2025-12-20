package com.nutzycraft.backend.controller;

import com.nutzycraft.backend.entity.Job;
import com.nutzycraft.backend.entity.SharedFile;
import com.nutzycraft.backend.entity.User;
import com.nutzycraft.backend.repository.JobRepository;
import com.nutzycraft.backend.repository.SharedFileRepository;
import com.nutzycraft.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/shared-files")
@CrossOrigin(origins = "*")
public class SharedFileController {

    @Autowired
    private SharedFileRepository sharedFileRepository;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private UserRepository userRepository;

    // Use a fixed upload directory for simplicity
    private final Path uploadDir = Paths.get("uploads");

    public SharedFileController() {
        try {
            Files.createDirectories(uploadDir);
        } catch (IOException e) {
            throw new RuntimeException("Could not create upload directory", e);
        }
    }

    @GetMapping("/job/{jobId}")
    public List<SharedFileDTO> getFilesByJob(@PathVariable Long jobId) {
        return sharedFileRepository.findByJobIdOrderByUploadedAtDesc(jobId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @PostMapping("/upload")
    public SharedFileDTO uploadFile(
            @RequestParam("jobId") Long jobId,
            @RequestParam("uploaderEmail") String uploaderEmail,
            @RequestParam("file") MultipartFile file) throws IOException {

        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found"));

        User uploader = userRepository.findByEmail(uploaderEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String originalFilename = file.getOriginalFilename();
        String uniqueFilename = UUID.randomUUID().toString() + "_" + originalFilename;
        Path filePath = uploadDir.resolve(uniqueFilename);

        Files.copy(file.getInputStream(), filePath);

        SharedFile sharedFile = new SharedFile();
        sharedFile.setJob(job);
        sharedFile.setUploader(uploader);
        sharedFile.setFileName(originalFilename);
        sharedFile.setFileType(file.getContentType());
        sharedFile.setFilePath(uniqueFilename); // Store distinct filename
        sharedFile.setSize(file.getSize());

        SharedFile saved = sharedFileRepository.save(sharedFile);
        return convertToDTO(saved);
    }

    // Since we are storing files locally, we need an endpoint to serve them
    // For now, let's assume a simplified GET link or use a static resource handler
    // if mapped.
    // However, storing in 'uploads' outside static resources might require a
    // controller to serve bytes.
    @GetMapping("/download/{id}")
    public ResponseEntity<org.springframework.core.io.Resource> downloadFile(@PathVariable Long id) throws IOException {
        SharedFile file = sharedFileRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("File not found"));

        Path path = uploadDir.resolve(file.getFilePath());
        org.springframework.core.io.Resource resource = new org.springframework.core.io.UrlResource(path.toUri());

        return ResponseEntity.ok()
                .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + file.getFileName() + "\"")
                .body(resource);
    }

    private SharedFileDTO convertToDTO(SharedFile file) {
        SharedFileDTO dto = new SharedFileDTO();
        dto.setId(file.getId());
        dto.setFileName(file.getFileName());
        dto.setFileType(file.getFileType());
        dto.setSize(file.getSize());
        dto.setUploadedAt(file.getUploadedAt());
        dto.setUploaderName(file.getUploader().getFullName());
        dto.setDownloadUrl("/api/shared-files/download/" + file.getId());
        return dto;
    }

    @lombok.Data
    public static class SharedFileDTO {
        private Long id;
        private String fileName;
        private String fileType;
        private long size;
        private java.time.LocalDateTime uploadedAt;
        private String uploaderName;
        private String downloadUrl;
    }
}

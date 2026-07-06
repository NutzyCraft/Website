package com.nutzycraft.backend.controller;

import com.nutzycraft.backend.entity.Job;
import com.nutzycraft.backend.entity.SharedFile;
import com.nutzycraft.backend.entity.User;
import com.nutzycraft.backend.repository.JobRepository;
import com.nutzycraft.backend.repository.SharedFileRepository;
import com.nutzycraft.backend.repository.UserRepository;
import com.nutzycraft.backend.service.FileUploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    @Autowired
    private FileUploadService fileUploadService;

    private void requireJobParticipant(Job job) {
        String email = com.nutzycraft.backend.security.CurrentUser.email();
        boolean isClient = job.getClient() != null && job.getClient().getEmail().equalsIgnoreCase(email);
        boolean isFreelancer = job.getFreelancer() != null && job.getFreelancer().getEmail().equalsIgnoreCase(email);
        if (!isClient && !isFreelancer) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.FORBIDDEN, "You are not a participant on this job");
        }
    }

    /**
     * Get all shared files for a job
     */
    @GetMapping("/job/{jobId}")
    public ResponseEntity<List<Map<String, Object>>> getFilesByJob(@PathVariable Long jobId) {
        List<SharedFile> files = sharedFileRepository.findByJobId(jobId);
        List<Map<String, Object>> response = new ArrayList<>();

        for (SharedFile file : files) {
            Map<String, Object> fileData = new HashMap<>();
            fileData.put("id", file.getId());
            fileData.put("fileName", file.getFileName());
            fileData.put("downloadUrl", file.getDownloadUrl());
            fileData.put("uploaderName", file.getUploaderName());
            fileData.put("size", file.getSize());
            fileData.put("uploadedAt", file.getUploadedAt().toString());
            response.add(fileData);
        }

        return ResponseEntity.ok(response);
    }

    /**
     * Upload a shared file
     * Cloudinary free tier: 10MB max file size
     */
    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(
            @RequestParam("jobId") @NonNull Long jobId,
            @RequestParam("file") MultipartFile file) {

        try {
            String uploaderEmail = com.nutzycraft.backend.security.CurrentUser.email();
            // Validate file size (10MB = 10 * 1024 * 1024 bytes for Cloudinary free tier)
            long maxSize = 10 * 1024 * 1024;
            if (file.getSize() > maxSize) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "File size exceeds 10MB limit (Cloudinary free tier)"));
            }

            // Validate file type
            String contentType = file.getContentType();
            if (contentType == null || !isValidFileType(contentType)) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Invalid file type. Allowed: images, PDF, PPTX"));
            }

            Job job = jobRepository.findById(jobId)
                    .orElseThrow(() -> new RuntimeException("Job not found"));
            requireJobParticipant(job);

            User uploader = userRepository.findByEmail(uploaderEmail)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Upload to Cloudinary
            Map<String, Object> uploadResult = fileUploadService.uploadFileWithDetails(file);

            // Save file metadata
            SharedFile sharedFile = new SharedFile();
            sharedFile.setJob(job);
            sharedFile.setFileName(file.getOriginalFilename());
            sharedFile.setDownloadUrl((String) uploadResult.get("secure_url"));
            sharedFile.setPublicId((String) uploadResult.get("public_id"));
            sharedFile.setUploaderName(uploader.getFullName());
            sharedFile.setUploaderEmail(uploaderEmail);
            sharedFile.setUploaderId(uploader.getId());  // REQUIRED: Fix NOT NULL constraint
            sharedFile.setSize(file.getSize());

            SharedFile saved = sharedFileRepository.save(sharedFile);

            Map<String, Object> response = new HashMap<>();
            response.put("id", saved.getId());
            response.put("fileName", saved.getFileName());
            response.put("downloadUrl", saved.getDownloadUrl());
            response.put("uploaderName", saved.getUploaderName());
            response.put("size", saved.getSize());

            return ResponseEntity.ok(response);

        } catch (IOException e) {
            return ResponseEntity.status(500)
                    .body(Map.of("error", "Upload failed: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Delete a shared file
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteFile(@PathVariable @NonNull Long id) {
        try {
            SharedFile file = sharedFileRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("File not found"));
            requireJobParticipant(file.getJob());

            // Delete from Cloudinary
            fileUploadService.deleteFile(file.getPublicId());

            // Delete from database
            sharedFileRepository.deleteById(id);

            return ResponseEntity.ok(Map.of("message", "File deleted successfully"));

        } catch (IOException e) {
            return ResponseEntity.status(500)
                    .body(Map.of("error", "Failed to delete file from storage: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Delete all shared files for a job (called when job is completed)
     */
    @DeleteMapping("/job/{jobId}")
    @Transactional
    public ResponseEntity<?> deleteAllFilesForJob(@PathVariable Long jobId) {
        try {
            Job job = jobRepository.findById(jobId)
                    .orElseThrow(() -> new RuntimeException("Job not found"));
            requireJobParticipant(job);

            List<SharedFile> files = sharedFileRepository.findByJobId(jobId);

            // Delete each file from Cloudinary
            for (SharedFile file : files) {
                try {
                    fileUploadService.deleteFile(file.getPublicId());
                } catch (IOException e) {
                    // Log error but continue with other files
                    System.err.println("Failed to delete file from Cloudinary: " + file.getPublicId());
                }
            }

            // Delete all from database
            sharedFileRepository.deleteByJobId(jobId);

            return ResponseEntity.ok(Map.of("message", "All files deleted successfully", "count", files.size()));

        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Validate file type
     */
    private boolean isValidFileType(String contentType) {
        return contentType.equals("image/jpeg") ||
               contentType.equals("image/png") ||
               contentType.equals("image/gif") ||
               contentType.equals("image/jpg") ||
               contentType.equals("application/pdf") ||
               contentType.equals("application/vnd.ms-powerpoint") ||
               contentType.equals("application/vnd.openxmlformats-officedocument.presentationml.presentation");
    }
}

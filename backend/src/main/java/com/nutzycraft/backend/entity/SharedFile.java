package com.nutzycraft.backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "shared_files")
public class SharedFile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id", nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private Job job;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(name = "file_path")
    private String filePath;  // Existing column - legacy file path

    @Column(name = "download_url")
    private String downloadUrl;  // Cloudinary URL (new column)

    @Column(name = "public_id")
    private String publicId;  // Cloudinary public ID for deletion (new column)

    @Column(name = "file_type")
    private String fileType;  // Existing column

    @Column(name = "uploader_name")
    private String uploaderName;  // New column for display

    @Column(name = "uploader_email")
    private String uploaderEmail;  // New column for display

    @Column(name = "uploader_id")
    private Long uploaderId;  // Existing column - keep for backward compatibility

    @Column(nullable = false)
    private Long size;  // File size in bytes

    @Column(name = "uploaded_at", nullable = false)
    private LocalDateTime uploadedAt;

    @PrePersist
    protected void onCreate() {
        uploadedAt = LocalDateTime.now();
    }
}

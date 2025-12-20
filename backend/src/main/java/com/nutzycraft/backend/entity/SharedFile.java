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
    private Job job;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploader_id", nullable = false)
    private User uploader;

    private String fileName;
    private String fileType; // e.g., "application/pdf"

    // We'll store the file content as Base64 for simplicity as requested/implied by
    // previous work,
    // or we could store a path. Given the "attachments" field in Proposal is likely
    // just names,
    // and we want real uploads, let's assume we might need to store content or
    // handle minimal uploads.
    // For a robust app, we'd use S3. For this local setup, let's just store
    // metadata and assume a separate upload handling
    // or store small files in DB (byte array). Let's use byte array for simplicity
    // in this demo context.

    // @Lob
    // @Column(columnDefinition = "LONGBLOB")
    // private byte[] data;
    // Actually, checking Proposal again, it just says "attachments" (String).
    // If the user wants "drag and drop", we probably need a real upload endpoint.
    // Let's stick to a simple metadata approach first, but let's assume we SAVE the
    // file to disk in `uploads/` folder and store the path here.

    private String filePath;

    private long size; // bytes

    private LocalDateTime uploadedAt = LocalDateTime.now();
}

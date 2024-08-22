package com.kcs.security_sample.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "file_info")
public class FileInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false, unique = true)
    private Long id;

    @Column(name = "original_name", nullable = false)
    private String originalName;

    @Column(name = "renamed_name", nullable = false)
    private String renamedName;

    @Column(name = "path", nullable = false)
    private String path;

    @Column(name = "size", nullable = false)
    private long size;

    @Column(name = "content_type", nullable = false)
    private String contentType;

    @Column(name = "upload_date_time", nullable = false)
    private LocalDateTime uploadDateTime;

    @PrePersist
    protected void onCreate() {
        uploadDateTime = LocalDateTime.now();
    }

    @Builder
    public FileInfo(String originalName, String renamedName, String path, long size, String contentType) {
        this.originalName = originalName;
        this.renamedName = renamedName;
        this.path = path;
        this.size = size;
        this.contentType = contentType;
    }


}

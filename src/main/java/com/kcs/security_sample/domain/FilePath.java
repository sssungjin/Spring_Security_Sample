package com.kcs.security_sample.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "file_paths")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FilePath {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String pathType;

    @Column(nullable = false)
    private String path;
}
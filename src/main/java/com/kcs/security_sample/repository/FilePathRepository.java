package com.kcs.security_sample.repository;

import com.kcs.security_sample.domain.FilePath;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FilePathRepository extends JpaRepository<FilePath, Long> {
    Optional<FilePath> findByPathType(String pathType);
}

package com.kcs.security_sample.dto.response;

public record FileUploadResponseDto(
        Long id,
        String originalName,
        String renamedName,
        String path,
        long size,
        String contentType
) { }

package com.kcs.security_sample.dto.response;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record TotalResponseDto(
        String title,
        String text,
        LocalDate date,
        LocalTime time,
        List<FileUploadResponseDto> uploadedFiles,
        String processingResult
) {
}
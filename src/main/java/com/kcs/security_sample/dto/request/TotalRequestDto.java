package com.kcs.security_sample.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record TotalRequestDto(
        @NotNull @Valid PostData post,

        @NotNull
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate date,

        @NotNull
        @JsonFormat(pattern = "HH:mm:ss")
        LocalTime hour,

        @NotEmpty @Valid List<FileData> file
) {
    public record PostData(
            @NotBlank
            @Size(max = 20, message = "Title must not exceed 20 characters")
            String title,

            @NotBlank
            String text
    ) {}

    public record FileData(
            @NotBlank String file_name,
            @Positive Long file_size,
            @NotBlank String file_type,
            @NotBlank String file_data,
            @NotBlank String path_type
    ) {}
}
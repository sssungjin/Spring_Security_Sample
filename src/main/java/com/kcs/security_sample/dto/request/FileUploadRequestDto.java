package com.kcs.security_sample.dto.request;

import org.springframework.web.multipart.MultipartFile;

public record FileUploadRequestDto(MultipartFile file) {
}

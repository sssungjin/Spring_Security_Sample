package com.kcs.security_sample.controller;

import com.kcs.security_sample.dto.common.ResponseDto;
import com.kcs.security_sample.dto.response.FileUploadResponseDto;
import com.kcs.security_sample.exception.CommonException;
import com.kcs.security_sample.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class FileController {
    @PostMapping("/upload/file")
    public ResponseDto<?> uploadFile(HttpServletRequest request) {
        FileUploadResponseDto result = (FileUploadResponseDto) request.getAttribute("fileUploadResult");
        if (result != null) {
            return ResponseDto.ok(result);
        } else {
            return ResponseDto.fail(new CommonException(ErrorCode.INTERNAL_SERVER_ERROR));
        }
    }

    @PostMapping("/upload/jsonfile")
    public ResponseDto<?> uploadJsonFile(HttpServletRequest request) {
        FileUploadResponseDto result = (FileUploadResponseDto) request.getAttribute("fileUploadResult");
        if (result != null) {
            return ResponseDto.ok(result);
        } else {
            return ResponseDto.fail(new CommonException(ErrorCode.INTERNAL_SERVER_ERROR));
        }
    }
}

package com.kcs.security_sample.controller;

import com.kcs.security_sample.dto.common.ResponseDto;
import com.kcs.security_sample.service.SubmitService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
@Slf4j
public class FileController {
    private final SubmitService submitService;
    @PostMapping("/multipart/upload")
    public ResponseDto<?> uploadFile(HttpServletRequest request) {
        return submitService.processFileUpload(request);
    }

    @PostMapping("/jsonfile/upload")
    public ResponseDto<?> uploadJsonFile(HttpServletRequest request) {
        return submitService.processFileUpload(request);
    }
}
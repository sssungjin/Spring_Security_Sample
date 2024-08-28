package com.kcs.security_sample.controller;

import com.kcs.security_sample.dto.common.ResponseDto;
import com.kcs.security_sample.exception.CommonException;
import com.kcs.security_sample.exception.ErrorCode;
import com.kcs.security_sample.service.FileService;
import com.kcs.security_sample.service.SubmitService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
@Slf4j
public class FileController {
    private final SubmitService submitService;
    private final FileService fileService;
    @PostMapping("/multipart/upload")
    public ResponseDto<?> uploadFile(HttpServletRequest request) {
        return submitService.processFileUpload(request);
    }

    @PostMapping("/jsonfile/upload")
    public ResponseDto<?> uploadJsonFile(HttpServletRequest request) {
        return submitService.processFileUpload(request);
    }
}
package com.kcs.security_sample.controller;

import com.kcs.security_sample.dto.request.SubmitRequestDto;
import com.kcs.security_sample.dto.response.SubmitResponseDto;
import com.kcs.security_sample.dto.common.ResponseDto;
import com.kcs.security_sample.service.SubmitService;
import com.kcs.security_sample.exception.CommonException;
import com.kcs.security_sample.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v1")
public class SubmitController {

    private final SubmitService submitService;

    @PostMapping("/danger-submit")
    public ResponseDto<?> dangerSubmit(@RequestBody SubmitRequestDto submitRequest) {
        log.info("Danger submit attempt with input: {}", submitRequest.inputText());
        try {
            SubmitResponseDto response = submitService.processDangerSubmit(submitRequest);
            return ResponseDto.ok(response);
        } catch (Exception e) {
            log.error("Error processing danger submit", e);
            return ResponseDto.fail(new CommonException(ErrorCode.INTERNAL_SERVER_ERROR));
        }
    }
}
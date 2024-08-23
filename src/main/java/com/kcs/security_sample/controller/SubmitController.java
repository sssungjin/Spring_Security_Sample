package com.kcs.security_sample.controller;

import com.kcs.security_sample.dto.request.FormDataSubmitRequestDto;
import com.kcs.security_sample.dto.request.SubmitRequestDto;
import com.kcs.security_sample.dto.response.FileUploadResponseDto;
import com.kcs.security_sample.dto.response.SubmitResponseDto;
import com.kcs.security_sample.dto.common.ResponseDto;
import com.kcs.security_sample.security.service.CustomUserDetailService;
import com.kcs.security_sample.security.service.FileService;
import com.kcs.security_sample.service.SubmitService;
import com.kcs.security_sample.exception.CommonException;
import com.kcs.security_sample.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v1")
public class SubmitController {

    private final SubmitService submitService;
    private final FileService fileService;
    private final CustomUserDetailService customUserDetailService;

    @PostMapping("/submit/danger")
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

    @PostMapping("/submit/formdata")
    public ResponseDto<?> formDataSubmit(@ModelAttribute FormDataSubmitRequestDto formDataSubmitDto, Authentication authentication) {
        log.info("Form data submit attempt with input: {}", formDataSubmitDto.text());
        try {
            SubmitResponseDto response = submitService.processFormDataSubmit(formDataSubmitDto);
            return ResponseDto.ok(response);
        } catch (Exception e) {
            log.error("Error processing form data submit", e);
            return ResponseDto.fail(new CommonException(ErrorCode.INTERNAL_SERVER_ERROR));
        }
    }



    @PostMapping("/submit/create")
    @PreAuthorize("hasAuthority('apiv1submitcreate_CREATE')")
    public ResponseDto<?> createSettlement(Authentication authentication) {
        try {
            return ResponseDto.ok("Settlement created successfully");
        } catch (Exception e) {
            log.error("Error creating settlement", e);
            return ResponseDto.fail(new CommonException(ErrorCode.INTERNAL_SERVER_ERROR));
        }
    }

    @DeleteMapping("/submit/delete")
    @PreAuthorize("hasAuthority('apiv1submitcreate_DELETE')")
    public ResponseDto<?> deleteSettlement(Authentication authentication) {
        try {
            return ResponseDto.ok("Settlement deleted successfully");
        } catch (Exception e) {
            log.error("Error deleting settlement", e);
            return ResponseDto.fail(new CommonException(ErrorCode.INTERNAL_SERVER_ERROR));
        }
    }


}
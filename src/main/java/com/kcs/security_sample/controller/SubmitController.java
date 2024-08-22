package com.kcs.security_sample.controller;

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



    @PostMapping("/submit/create")
    @PreAuthorize("hasAuthority('apiv1submitcreate_CREATE')")
    public ResponseDto<?> createSettlement(Authentication authentication) {

//        if (!customUserDetailService.hasPermission("apiv1submitcreate_CREATE")) {
//            return ResponseDto.fail(new CommonException(ErrorCode.UNAUTHORIZED));
//        }
        return ResponseDto.ok("Settlement created successfully");
    }

    @DeleteMapping("/submit/delete")
    public ResponseDto<?> deleteSettlement(Authentication authentication) {
        log.info("Entered deleteSettlement method");

        // 권한 검사
        if (!customUserDetailService.hasPermission("apiv1submitcreate_DELETE")) {
            log.warn("User {} does not have 'settlement_DELETE' authority", authentication.getName());
            return ResponseDto.fail(new CommonException(ErrorCode.UNAUTHORIZED));
        }

        log.info("Attempting to delete settlement. User: {}, Authorities: {}",
                authentication.getName(),
                authentication.getAuthorities());

        return ResponseDto.ok("Settlement deleted successfully");
    }
}
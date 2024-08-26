package com.kcs.security_sample.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.kcs.security_sample.dto.request.FormDataSubmitRequestDto;
import com.kcs.security_sample.dto.request.TotalRequestDto;
import com.kcs.security_sample.dto.response.FileUploadResponseDto;
import com.kcs.security_sample.dto.response.SubmitResponseDto;
import com.kcs.security_sample.dto.common.ResponseDto;
import com.kcs.security_sample.dto.response.TotalResponseDto;
import com.kcs.security_sample.service.SubmitService;
import com.kcs.security_sample.exception.CommonException;
import com.kcs.security_sample.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v1")
public class SubmitController {

    private final SubmitService submitService;
    private final ObjectMapper objectMapper;
    private final Validator validator;

    @PostMapping("/submit/danger")
    public ResponseDto<?> dangerSubmit(@RequestAttribute("input_text") String inputText) {
        log.info("Danger submit attempt with input: {}", inputText);
        try {
            SubmitResponseDto response = submitService.processDangerSubmit(inputText);
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

    @GetMapping("/submit/create")
    public ResponseDto<?> createSettlement() {
        try {
            return ResponseDto.ok("Settlement created successfully");
        } catch (Exception e) {
            log.error("Error creating settlement", e);
            return ResponseDto.fail(new CommonException(ErrorCode.INTERNAL_SERVER_ERROR));
        }
    }

    @DeleteMapping("/submit/delete")
    public ResponseDto<?> deleteSettlement() {
        try {
            return ResponseDto.ok("Settlement deleted successfully");
        } catch (Exception e) {
            log.error("Error deleting settlement", e);
            return ResponseDto.fail(new CommonException(ErrorCode.INTERNAL_SERVER_ERROR));
        }
    }

    @PostMapping("/submit/total")
    public ResponseDto<?> totalSubmit(HttpServletRequest request) {
        try {
            TotalRequestDto.PostData postData = objectMapper.convertValue(request.getAttribute("post"), TotalRequestDto.PostData.class);
            LocalDate date = LocalDate.parse((String) request.getAttribute("date"));
            LocalTime hour = LocalTime.parse((String) request.getAttribute("hour"));
            List<TotalRequestDto.FileData> fileData = objectMapper.convertValue(request.getAttribute("file"),
                    new TypeReference<List<TotalRequestDto.FileData>>() {});

            TotalRequestDto totalRequestDto = new TotalRequestDto(postData, date, hour, fileData);

            Set<ConstraintViolation<TotalRequestDto>> violations = validator.validate(totalRequestDto);
            if (!violations.isEmpty()) {
                List<String> errorMessages = violations.stream()
                        .map(ConstraintViolation::getMessage)
                        .collect(Collectors.toList());
                log.error("Validation errors: {}", errorMessages);
                return ResponseDto.fail(new CommonException(ErrorCode.INVALID_INPUT));
            }

            log.info("Total submit attempt with input: {}", totalRequestDto);
            List<FileUploadResponseDto> fileUploadResults = (List<FileUploadResponseDto>) request.getAttribute("fileUploadResults");
            TotalResponseDto response = submitService.processTotalSubmit(totalRequestDto, fileUploadResults);
            return ResponseDto.ok(response);
        } catch (Exception e) {
            log.error("Error processing total submit", e);
            return ResponseDto.fail(new CommonException(ErrorCode.INTERNAL_SERVER_ERROR));
        }
    }
}
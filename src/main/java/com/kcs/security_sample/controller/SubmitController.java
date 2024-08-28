package com.kcs.security_sample.controller;

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
    private final Validator validator;

    /**
     * Submits a danger form
     * @param inputText the input text from the form (json)
     * @return a response containing the submit result
     */
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

    /**
     * Submits a form data
     * @param formDataSubmitDto the form data submit request DTO (form-data)
     * @param authentication the authentication object
     * @return a response containing the submit result
     */
    @PostMapping("/submit/formdata")
    public ResponseDto<?> formDataSubmit(@ModelAttribute FormDataSubmitRequestDto formDataSubmitDto, Authentication authentication) {
        log.info("formDataSubmitDto {}", formDataSubmitDto);

        log.info("Form data submit attempt with input: {}", formDataSubmitDto.text());
        try {
            SubmitResponseDto response = submitService.processFormDataSubmit(formDataSubmitDto);
            return ResponseDto.ok(response);
        } catch (Exception e) {
            log.error("Error processing form data submit", e);
            return ResponseDto.fail(new CommonException(ErrorCode.INTERNAL_SERVER_ERROR));
        }
    }


    /**
     * Creates a settlement (dummy method just to test the filter, permission)
     * @return
     */
    @GetMapping("/submit/create")
    public ResponseDto<?> createSettlement() {
        try {
            return ResponseDto.ok("Settlement created successfully");
        } catch (Exception e) {
            log.error("Error creating settlement", e);
            return ResponseDto.fail(new CommonException(ErrorCode.INTERNAL_SERVER_ERROR));
        }
    }

    /**
     * Deletes a settlement (dummy method just to test the filter, permission)
     * @return
     */
    @DeleteMapping("/submit/delete")
    public ResponseDto<?> deleteSettlement() {
        try {
            return ResponseDto.ok("Settlement deleted successfully");
        } catch (Exception e) {
            log.error("Error deleting settlement", e);
            return ResponseDto.fail(new CommonException(ErrorCode.INTERNAL_SERVER_ERROR));
        }
    }

    /**
     * Submits a total form
     * @param totalRequestDtoBody the total request DTO (cached json)
     * @param request the request object
     * @return a response containing the submit result
     */
    @PostMapping("/submit/total")
    public ResponseDto<?> totalSubmit(@RequestBody TotalRequestDto totalRequestDtoBody, HttpServletRequest request) {
        try {
            TotalRequestDto totalRequestDto = submitService.buildTotalRequestDto(request);

            Set<ConstraintViolation<TotalRequestDto>> violations = validator.validate(totalRequestDto);
            if (!violations.isEmpty()) {
                List<String> errorMessages = violations.stream()
                        .map(ConstraintViolation::getMessage)
                        .collect(Collectors.toList());
                log.error("Validation errors: {}", errorMessages);
                return ResponseDto.fail(new CommonException(ErrorCode.INVALID_INPUT));
            }

            List<FileUploadResponseDto> fileUploadResults = (List<FileUploadResponseDto>) request.getAttribute("fileUploadResults");
            TotalResponseDto response = submitService.processTotalSubmit(totalRequestDto, fileUploadResults);
            return ResponseDto.ok(response);
        } catch (Exception e) {
            log.error("Error processing total submit", e);
            return ResponseDto.fail(new CommonException(ErrorCode.INVALID_INPUT));
        }
    }

    /**
     * Test Xss attack, by parameter input
     * @param request
     * @return
     * example: http://localhost:8080/api/v1/example?param1=<script>alert('XSS')</script>&param2=value1&param2=<img src=x onerror=alert('XSS')>
     */
    @GetMapping("/example")
    public String exampleMethod(HttpServletRequest request) {
        String param1 = (String) request.getAttribute("param1");
        String[] param2Array = (String[]) request.getAttribute("param2");
        String param1Original = (String) request.getAttribute("param1_original");
        String[] param2OriginalArray = (String[]) request.getAttribute("param2_original");
        return " param1: " + param1 + "\n param2: " + String.join("\n", param2Array) + "\n param1_original: " + param1Original + "\n param2_original: " + String.join(", ", param2OriginalArray);
    }
}
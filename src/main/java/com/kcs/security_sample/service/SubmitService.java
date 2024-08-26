package com.kcs.security_sample.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kcs.security_sample.dto.common.ResponseDto;
import com.kcs.security_sample.dto.request.FormDataSubmitRequestDto;
import com.kcs.security_sample.dto.request.TotalRequestDto;
import com.kcs.security_sample.dto.response.FileUploadResponseDto;
import com.kcs.security_sample.dto.response.SubmitResponseDto;
import com.kcs.security_sample.dto.response.TotalResponseDto;
import com.kcs.security_sample.exception.CommonException;
import com.kcs.security_sample.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;


@Service
@RequiredArgsConstructor
@Slf4j
public class SubmitService {
    private final ObjectMapper objectMapper;

    public SubmitResponseDto processDangerSubmit(String inputText) {
        return new SubmitResponseDto(inputText);
    }

    public SubmitResponseDto processFormDataSubmit(FormDataSubmitRequestDto formDataSubmitDto) {
        return new SubmitResponseDto(formDataSubmitDto.text());
    }

    public TotalResponseDto processTotalSubmit(TotalRequestDto totalRequestDto, List<FileUploadResponseDto> uploadResults) {
        // Process the totalRequestDto and uploadResults
        // This is where you would implement your business logic
        return new TotalResponseDto(
                totalRequestDto.post().title(),
                totalRequestDto.post().text(),
                totalRequestDto.date(),
                totalRequestDto.hour(),
                uploadResults,
                "Request processed successfully"
        );
    }

    public ResponseDto<?> processFileUpload(HttpServletRequest request) {
        FileUploadResponseDto result = (FileUploadResponseDto) request.getAttribute("fileUploadResult");
        log.info("Processing file upload request. Result: {}", result);
        if (result != null) {
            return ResponseDto.ok(result);
        } else {
            log.error("File upload result not found in request attributes");
            return ResponseDto.fail(new CommonException(ErrorCode.INTERNAL_SERVER_ERROR));
        }
    }

    public TotalRequestDto buildTotalRequestDto(HttpServletRequest request) {
        try {
            TotalRequestDto.PostData postData = objectMapper.convertValue(request.getAttribute("post"), TotalRequestDto.PostData.class);
            LocalDate date = LocalDate.parse((String) request.getAttribute("date"));
            LocalTime hour = LocalTime.parse((String) request.getAttribute("hour"));
            List<TotalRequestDto.FileData> fileData = objectMapper.convertValue(request.getAttribute("file"),
                    new TypeReference<List<TotalRequestDto.FileData>>() {});

            return new TotalRequestDto(postData, date, hour, fileData);
        } catch (Exception e) {
            log.error("Error building TotalRequestDto", e);
            throw new CommonException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }
}
package com.kcs.security_sample.service;

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

import java.util.List;


@Service
@RequiredArgsConstructor
@Slf4j
public class SubmitService {
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
}
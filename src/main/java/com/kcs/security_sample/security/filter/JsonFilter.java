package com.kcs.security_sample.security.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kcs.security_sample.dto.response.FileUploadResponseDto;
import com.kcs.security_sample.exception.CommonException;
import com.kcs.security_sample.exception.ErrorCode;
import com.kcs.security_sample.security.service.FileService;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.Base64;

@Component
@RequiredArgsConstructor
@Slf4j
@Order(1)
public class JsonFilter extends OncePerRequestFilter {

    private final ObjectMapper objectMapper;
    private final FileService fileService;


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String uri = request.getRequestURI();

        if ("application/json".equalsIgnoreCase(request.getContentType())) {
            try {
                String jsonBody = new String(request.getInputStream().readAllBytes());
                Map<String, Object> jsonMap = objectMapper.readValue(jsonBody, Map.class);

                if ("/api/v1/jsonfile/upload".equals(uri)) {
                    FileUploadResponseDto responseDto = processJsonFileUpload(jsonMap);
                    request.setAttribute("fileUploadResult", responseDto);
                } else {
                    log.info("Processing other JSON request: {}", uri);
                    for (Map.Entry<String, Object> entry : jsonMap.entrySet()) {
                        request.setAttribute(entry.getKey(), entry.getValue());
                    }
                }

            } catch (Exception e) {
                log.error("Error processing JSON request: {}", e.getMessage(), e);
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("Invalid JSON or processing error");
                return;
            }
        }

        // Continue the filter chain
        filterChain.doFilter(request, response);
    }

    private FileUploadResponseDto processJsonFileUpload(Map<String, Object> jsonMap) throws IOException {
        String fileName = (String) jsonMap.get("file_name");
        String fileType = (String) jsonMap.get("file_type");
        Integer fileSize = (Integer) jsonMap.get("file_size");
        String fileData = (String) jsonMap.get("file_data");

        if (fileName != null && fileType != null && fileSize != null && fileData != null) {
            byte[] decodedFile = Base64.getDecoder().decode(fileData);
            MultipartFile multipartFile = new MockMultipartFile(fileName, fileName, fileType, decodedFile);

            FileUploadResponseDto responseDto = fileService.uploadFile(multipartFile);
            log.info("JSON File upload successful: {}", responseDto);
            return responseDto;
        } else {
            log.error("Invalid file data received");
            throw new CommonException(ErrorCode.FILE_UPLOAD_FAILED);
        }
    }

}


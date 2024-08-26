package com.kcs.security_sample.security.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kcs.security_sample.dto.response.FileUploadResponseDto;
import com.kcs.security_sample.exception.CommonException;
import com.kcs.security_sample.exception.ErrorCode;
import com.kcs.security_sample.service.FileService;
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
import java.util.ArrayList;
import java.util.List;
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

                if ("/api/v1/submit/total".equals(uri)) {
                    processJsonFileUpload(jsonMap, request);
                } else if ("/api/v1/jsonfile/upload".equals(uri)) {
                    FileUploadResponseDto responseDto = fileService.uploadFile((MultipartFile) jsonMap.get("file"));
                    request.setAttribute("fileUploadResult", responseDto);
                }

                for (Map.Entry<String, Object> entry : jsonMap.entrySet()) {
                    request.setAttribute(entry.getKey(), entry.getValue());
                }
            } catch (Exception e) {
                log.error("Error processing JSON request: {}", e.getMessage(), e);
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("Invalid JSON or processing error");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private void processJsonFileUpload(Map<String, Object> jsonMap, HttpServletRequest request) throws IOException {
        List<Map<String, Object>> files = (List<Map<String, Object>>) jsonMap.get("file");
        List<FileUploadResponseDto> uploadResults = new ArrayList<>();

        for (Map<String, Object> fileData : files) {
            String fileName = (String) fileData.get("file_name");
            String fileType = (String) fileData.get("file_type");
            Long fileSize = Long.valueOf(fileData.get("file_size").toString());
            String fileDataStr = (String) fileData.get("file_data");
            String pathType = (String) fileData.get("path_type");

            byte[] decodedFile = Base64.getDecoder().decode(fileDataStr);
            MultipartFile multipartFile = new MockMultipartFile(fileName, fileName, fileType, decodedFile);

            FileUploadResponseDto responseDto = fileService.uploadFileWithPathType(multipartFile, pathType);
            uploadResults.add(responseDto);
        }

        request.setAttribute("fileUploadResults", uploadResults);
    }
}


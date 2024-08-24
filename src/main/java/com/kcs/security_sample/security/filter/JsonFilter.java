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

        // Check if the request is a JSON request
        if ("application/json".equalsIgnoreCase(request.getContentType())) {
            try {
                String jsonBody = new String(request.getInputStream().readAllBytes());
                Map<String, Object> jsonMap = objectMapper.readValue(jsonBody, Map.class);

                // Process upload file request, need to add a new endpoint for this
                if ("/api/v1/jsonfile/upload".equals(uri)) {
                    FileUploadResponseDto responseDto = processJsonFileUpload(jsonMap);         // If file upload uri, process JSON file upload
                    request.setAttribute("fileUploadResult", responseDto);                // Set file upload result as request attribute
                } else {
                    for (Map.Entry<String, Object> entry : jsonMap.entrySet()) {
                        request.setAttribute(entry.getKey(), entry.getValue());                 // Set JSON attributes as request attributes
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

    // Process JSON file upload
    private FileUploadResponseDto processJsonFileUpload(Map<String, Object> jsonMap) throws IOException {
        String fileName = (String) jsonMap.get("file_name");
        String fileType = (String) jsonMap.get("file_type");
        Integer fileSize = (Integer) jsonMap.get("file_size");
        String fileData = (String) jsonMap.get("file_data");

        // Decode base64 file data and create MultipartFile to save at path
        if (fileName != null && fileType != null && fileSize != null && fileData != null) {
            byte[] decodedFile = Base64.getDecoder().decode(fileData);
            MultipartFile multipartFile = new MockMultipartFile(fileName, fileName, fileType, decodedFile);

            // Upload file and return response
            FileUploadResponseDto responseDto = fileService.uploadFile(multipartFile);                      // Upload file at Path
            return responseDto;
        } else {
            log.error("Invalid file data received");
            throw new CommonException(ErrorCode.FILE_UPLOAD_FAILED);
        }
    }

}


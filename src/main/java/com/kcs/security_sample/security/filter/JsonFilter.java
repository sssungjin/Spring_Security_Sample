package com.kcs.security_sample.security.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kcs.security_sample.dto.response.FileUploadResponseDto;
import com.kcs.security_sample.security.details.CachedBodyHttpServletRequest;
import com.kcs.security_sample.security.details.CustomMultipartFile;
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
import org.apache.commons.text.StringEscapeUtils;

import java.io.IOException;
import java.util.*;

@Component
@RequiredArgsConstructor
@Slf4j
@Order(1)
public class JsonFilter extends OncePerRequestFilter {

    private final ObjectMapper objectMapper;
    private final FileService fileService;


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String uri = request.getRequestURI();
        if ("application/json".equalsIgnoreCase(request.getContentType())) {
            CachedBodyHttpServletRequest cachedBodyHttpServletRequest = new CachedBodyHttpServletRequest(request);
            String jsonBody = new String(cachedBodyHttpServletRequest.getInputStream().readAllBytes());

            Map<String, Object> jsonMap = objectMapper.readValue(jsonBody, Map.class);
            Map<String, Object> sanitizedJsonMap = new HashMap<>(jsonMap);

            sanitizeJson(sanitizedJsonMap);
            String escapedJsonBody = objectMapper.writeValueAsString(sanitizedJsonMap);
            cachedBodyHttpServletRequest = new CachedBodyHttpServletRequest(request, escapedJsonBody.getBytes());

            if ("/api/v1/submit/total".equals(uri)) {
                processJsonFileUpload(jsonMap, request);
            } else if ("/api/v1/jsonfile/upload".equals(uri)) {
                Map<String, Object> fileData = (Map<String, Object>) jsonMap.get("file");
                if (fileData != null) {
                    String fileName = (String) fileData.get("file_name");
                    String fileType = (String) fileData.get("file_type");
                    String fileDataStr = (String) fileData.get("file_data");

                    if (fileName != null && fileDataStr != null) {
                        byte[] decodedFile = Base64.getDecoder().decode(fileDataStr);
                        MultipartFile multipartFile = new CustomMultipartFile(fileName, fileName, fileType, decodedFile);

                        FileUploadResponseDto responseDto = fileService.uploadFile(multipartFile);
                        request.setAttribute("fileUploadResult", responseDto);
                    } else {
                        throw new IllegalArgumentException("File name or file data is missing");
                    }
                } else {
                    throw new IllegalArgumentException("File data is missing in the request");
                }
            }

            for (Map.Entry<String, Object> entry : sanitizedJsonMap.entrySet()) {
                request.setAttribute(entry.getKey(), entry.getValue());
                request.setAttribute(entry.getKey() + "_original", jsonMap.get(entry.getKey()));
            }
            filterChain.doFilter(cachedBodyHttpServletRequest, response);
        } else {
            filterChain.doFilter(request, response);
        }
    }

    private void sanitizeJson(Map<String, Object> json) {
        for (Map.Entry<String, Object> entry : json.entrySet()) {
            if (entry.getValue() instanceof String) {
                entry.setValue(StringEscapeUtils.escapeHtml4((String) entry.getValue()));
            } else if (entry.getValue() instanceof Map) {
                sanitizeJson((Map<String, Object>) entry.getValue());
            } else if (entry.getValue() instanceof List) {
                sanitizeList((List<Object>) entry.getValue());
            }
        }
    }

    private void sanitizeList(List<Object> list) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i) instanceof String) {
                list.set(i, StringEscapeUtils.escapeHtml4((String) list.get(i)));
            } else if (list.get(i) instanceof Map) {
                sanitizeJson((Map<String, Object>) list.get(i));
            } else if (list.get(i) instanceof List) {
                sanitizeList((List<Object>) list.get(i));
            }
        }
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

            MultipartFile multipartFile = new CustomMultipartFile(fileName, fileName, fileType, decodedFile);


            FileUploadResponseDto responseDto = fileService.uploadFileWithPathType(multipartFile, pathType);
            uploadResults.add(responseDto);
        }

        request.setAttribute("fileUploadResults", uploadResults);
    }
}


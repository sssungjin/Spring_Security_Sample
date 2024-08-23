package com.kcs.security_sample.security.filter;

import com.kcs.security_sample.dto.response.FileUploadResponseDto;
import com.kcs.security_sample.exception.CommonException;
import com.kcs.security_sample.exception.ErrorCode;
import com.kcs.security_sample.security.service.FileService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.StringEscapeUtils;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
@Order(2)
public class MultipartFilter extends OncePerRequestFilter {

    private final MultipartResolver multipartResolver;
    private final FileService fileService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String uri = request.getRequestURI();

        if (multipartResolver.isMultipart(request) && !(request instanceof ContentCachingRequestWrapper)) {
            MultipartHttpServletRequest multipartRequest = multipartResolver.resolveMultipart(request);
            try {
                processMultipartRequest(multipartRequest);
                filterChain.doFilter(multipartRequest, response);
            } catch (Exception e) {
                log.error("Error in MultipartFilter", e);
                throw new ServletException(e);
            }
        } else {
            log.info("Skipping multipart processing for request: {}", uri);
            filterChain.doFilter(request, response);
        }
    }

    private void processMultipartRequest(MultipartHttpServletRequest multipartRequest) throws IOException {
        // Store all request parameters as attributes
        Map<String, String[]> parameterMap = multipartRequest.getParameterMap();
        for (Map.Entry<String, String[]> entry : parameterMap.entrySet()) {
            String[] sanitizedValues = new String[entry.getValue().length];
            for (int i = 0; i < entry.getValue().length; i++) {
                sanitizedValues[i] = StringEscapeUtils.escapeHtml4(entry.getValue()[i]);
            }
            multipartRequest.setAttribute(entry.getKey(), sanitizedValues);
        }

        MultipartFile file = multipartRequest.getFile("file");
        if (file != null) {
            log.info("Processing file upload: {}", file.getOriginalFilename());
            try {
                FileUploadResponseDto responseDto = fileService.uploadFile(file);
                multipartRequest.setAttribute("fileUploadResult", responseDto);
                log.info("File upload successful: {}", responseDto);
            } catch (Exception e) {
                log.error("Error during file upload: {}", e.getMessage(), e);
                throw new CommonException(ErrorCode.FILE_UPLOAD_FAILED);
            }
        } else {
            log.warn("No file found in the multipart request");
        }
    }

    private static class XssRequestWrapper extends HttpServletRequestWrapper {

        private Map<String, String[]> sanitizedParameterMap;

        public XssRequestWrapper(HttpServletRequest request) {
            super(request);
            this.sanitizedParameterMap = sanitizeParameterMap(request.getParameterMap());
        }

        @Override
        public String getParameter(String name) {
            String[] values = getParameterValues(name);
            if (values != null && values.length > 0) {
                return values[0];
            }
            return null;
        }

        @Override
        public String[] getParameterValues(String name) {
            return sanitizedParameterMap.get(name);
        }

        @Override
        public Enumeration<String> getParameterNames() {
            return Collections.enumeration(sanitizedParameterMap.keySet());
        }

        @Override
        public Map<String, String[]> getParameterMap() {
            return sanitizedParameterMap;
        }

        private Map<String, String[]> sanitizeParameterMap(Map<String, String[]> parameterMap) {
            Map<String, String[]> sanitizedMap = new HashMap<>();

            for (Map.Entry<String, String[]> entry : parameterMap.entrySet()) {
                String[] rawValues = entry.getValue();
                String[] sanitizedValues = new String[rawValues.length];
                for (int i = 0; i < rawValues.length; i++) {
                    sanitizedValues[i] = StringEscapeUtils.escapeHtml4(rawValues[i]);
                }
                sanitizedMap.put(entry.getKey(), sanitizedValues);
            }

            return sanitizedMap;
        }
    }
}

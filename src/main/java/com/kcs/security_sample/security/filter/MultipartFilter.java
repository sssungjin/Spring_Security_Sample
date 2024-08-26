package com.kcs.security_sample.security.filter;

import com.kcs.security_sample.dto.response.FileUploadResponseDto;
import com.kcs.security_sample.exception.CommonException;
import com.kcs.security_sample.exception.ErrorCode;
import com.kcs.security_sample.service.FileService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
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
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
@Order(2) // After Excute JSONFilter, which is Order(1)
public class MultipartFilter extends OncePerRequestFilter {

    private final MultipartResolver multipartResolver;
    private final FileService fileService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String uri = request.getRequestURI();

        //  Check if the request is a multipart request
        if (multipartResolver.isMultipart(request) && !(request instanceof ContentCachingRequestWrapper)) {
            MultipartHttpServletRequest multipartRequest = multipartResolver.resolveMultipart(request);
            try {
                processMultipartRequest(multipartRequest);                                      //  Process the multipart request
                filterChain.doFilter(multipartRequest, response);                               // Continue the filter chain with the multipart request
            } catch (Exception e) {
                log.error("Error in MultipartFilter", e);
                throw new ServletException(e);
            }
        } else {
            filterChain.doFilter(request, response);                                            // Continue the filter chain with the not multipart request
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

        // Process file upload
        MultipartFile file = multipartRequest.getFile("file");
        if (file != null) {
            try {
                FileUploadResponseDto responseDto = fileService.uploadFile(file);                // Upload the file
                multipartRequest.setAttribute("fileUploadResult", responseDto);           // Set the file upload result as a request attribute
            } catch (Exception e) {
                log.error("Error during file upload: {}", e.getMessage(), e);
                throw new CommonException(ErrorCode.FILE_UPLOAD_FAILED);
            }
        } else {
            log.warn("No file found in the multipart request");
        }
    }
}

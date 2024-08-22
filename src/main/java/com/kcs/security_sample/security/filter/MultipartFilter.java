package com.kcs.security_sample.security.filter;

import com.kcs.security_sample.dto.response.FileUploadResponseDto;
import com.kcs.security_sample.exception.CommonException;
import com.kcs.security_sample.exception.ErrorCode;
import com.kcs.security_sample.security.service.FileService;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.MultipartResolver;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class MultipartFilter implements Filter {

    private final MultipartResolver multipartResolver;
    private final FileService fileService;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;

        String uri = ((HttpServletRequest) request).getRequestURI();


        try {

            log.info("Processing request: {}", uri);

            //if (multipartResolver.isMultipart(httpRequest)) {
            log.info("Processing multipart request: {}", httpRequest.getRequestURI());

            MultipartHttpServletRequest multipartRequest = multipartResolver.resolveMultipart(httpRequest);
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

            chain.doFilter(multipartRequest, response);
            //} else {
            //    log.info("Non-multipart request detected");
            //    chain.doFilter(request, response);
            //}
        }
        catch(Exception e){
            chain.doFilter(request, response);
        }
    }
}

package com.kcs.security_sample.controller;

import com.kcs.security_sample.dto.common.ResponseDto;
import com.kcs.security_sample.exception.CommonException;
import com.kcs.security_sample.exception.ErrorCode;
import com.kcs.security_sample.service.FileService;
import com.kcs.security_sample.service.SubmitService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
@Slf4j
public class FileController {
    private final SubmitService submitService;
    private final FileService fileService;

    /**
     * Uploads a file to the server
     * @param request the request containing the file
     * @return a response containing the file upload result
     */
    @PostMapping("/multipart/upload")
    public ResponseDto<?> uploadFile(HttpServletRequest request) {
        return submitService.processFileUpload(request);
    }

    /**
     * Uploads a JSON file to the server
     * @param request the request containing the JSON file
     * @return a response containing the file upload result
     */
    @PostMapping("/jsonfile/upload")
    public ResponseDto<?> uploadJsonFile(HttpServletRequest request) {
        return submitService.processFileUpload(request);
    }

    /**
     * Deletes a file from the server
     * @param fileId the ID of the file to delete
     * @return a response indicating whether the file was deleted successfully
     */
    @DeleteMapping("/file/{fileId}")
    public ResponseDto<?> deleteFile(@PathVariable Long fileId) {
        try {
            fileService.deleteFile(fileId);
            return ResponseDto.ok("File deleted successfully");
        } catch (IOException e) {
            log.error("Error deleting file", e);
            return ResponseDto.fail(new CommonException(ErrorCode.FILE_DELETE_FAILED));
        }
    }

    /**
     * Deletes a file from the server
     * @param fileId the ID of the file to delete
     * @return a response indicating whether the file was deleted successfully
     */
    @DeleteMapping("/file/forceDelete/{fileId}")
    public ResponseDto<?> deleteFileForce(@PathVariable Long fileId) {
        try {
            fileService.deleteFileForce(fileId);
            return ResponseDto.ok("File deleted successfully");
        } catch (IOException e) {
            log.error("Error deleting file", e);
            return ResponseDto.fail(new CommonException(ErrorCode.FILE_DELETE_FAILED));
        }
    }
}
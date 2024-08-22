package com.kcs.security_sample.security.service;

import com.kcs.security_sample.domain.FileInfo;
import com.kcs.security_sample.dto.response.FileUploadResponseDto;
import com.kcs.security_sample.exception.CommonException;
import com.kcs.security_sample.exception.ErrorCode;
import com.kcs.security_sample.repository.FileInfoRepository;
import com.kcs.security_sample.security.config.FileUploadProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileService {


    private final FileInfoRepository fileInfoRepository;

    private final FileUploadProperties fileUploadProperties;


    @Transactional
    public FileUploadResponseDto uploadFile(MultipartFile file) throws IOException {
        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename);

        if (!isAllowedExtension(extension)) {
            throw new CommonException(ErrorCode.FILE_EXTENSION_NOT_ALLOWED);
        }

        String renamedFilename = generateUniqueFilename(originalFilename);
        Path filePath = Paths.get(fileUploadProperties.getPath(), renamedFilename);

        Files.copy(file.getInputStream(), filePath);
        setFilePermissions(filePath);

        FileInfo fileInfo = FileInfo.builder()
                .originalName(originalFilename)
                .renamedName(renamedFilename)
                .path(filePath.toString())
                .size(file.getSize())
                .contentType(file.getContentType())
                .build();

        FileInfo savedFileInfo = fileInfoRepository.save(fileInfo);

        return new FileUploadResponseDto(
                savedFileInfo.getId(),
                savedFileInfo.getOriginalName(),
                savedFileInfo.getRenamedName(),
                savedFileInfo.getPath(),
                savedFileInfo.getSize(),
                savedFileInfo.getContentType()
        );
    }

    private String getFileExtension(String filename) {
        return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
    }

    private boolean isAllowedExtension(String extension) {
        log.info("Allowed extensions: {}", fileUploadProperties.getAllowedExtensions());
        return fileUploadProperties.getAllowedExtensions().contains(extension);
    }

    private String generateUniqueFilename(String originalFilename) {
        String extension = getFileExtension(originalFilename);
        return UUID.randomUUID().toString() + extension;
    }

    private void setFilePermissions(Path filePath) throws IOException {
        File file = filePath.toFile();
        if (!file.setReadable(true, false)) {
            throw new CommonException(ErrorCode.FILE_PERMISSION_SETTING_FAILED);
        }
        if (!file.setWritable(true, true)) {
            throw new CommonException(ErrorCode.FILE_PERMISSION_SETTING_FAILED);
        }
//        if (!file.setExecutable(false, false)) {
//            throw new IOException("Failed to set file non-executable");
//        }
    }

//    private void setFilePermissions(Path filePath) throws IOException {
//        try {
//            Set<PosixFilePermission> permissions = PosixFilePermissions.fromString("rw-r--r--");
//            Files.setPosixFilePermissions(filePath, permissions);
//        } catch (UnsupportedOperationException e) {
//            // Fallback for non-POSIX systems
//            File file = filePath.toFile();
//            if (!file.setReadable(true, false) || !file.setWritable(true, true) || !file.setExecutable(false, false)) {
//                throw new IOException("Failed to set file permissions");
//            }
//        }
//        log.info("File permissions set successfully for: {}", filePath);
//    }
}
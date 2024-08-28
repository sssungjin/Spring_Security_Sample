package com.kcs.security_sample.service;

import com.kcs.security_sample.domain.FileInfo;
import com.kcs.security_sample.domain.FilePath;
import com.kcs.security_sample.dto.response.FileUploadResponseDto;
import com.kcs.security_sample.exception.CommonException;
import com.kcs.security_sample.exception.ErrorCode;
import com.kcs.security_sample.repository.FileInfoRepository;
import com.kcs.security_sample.repository.FilePathRepository;
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
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileService {
    private final FileInfoRepository fileInfoRepository;
    private final FilePathRepository filePathRepository;
    private final FileUploadProperties fileUploadProperties;

    @Transactional
    public FileUploadResponseDto uploadFileWithPathType(MultipartFile file, String pathType) throws IOException {
        if (file.getSize() > fileUploadProperties.getMaxFileSize()) {
            throw new CommonException(ErrorCode.FILE_SIZE_EXCEEDED);
        }

        FilePath filePath = filePathRepository.findByPathType(pathType)
                .orElseThrow(() -> new CommonException(ErrorCode.INVALID_PATH_TYPE));

        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename);

        if (!isAllowedExtension(extension)) {
            throw new CommonException(ErrorCode.FILE_EXTENSION_NOT_ALLOWED);
        }

        String renamedFilename = generateUniqueFilename(originalFilename);
        Path fullPath = Paths.get(filePath.getPath(), renamedFilename);

        Files.createDirectories(fullPath.getParent());
        Files.copy(file.getInputStream(), fullPath);
        setFilePermissions(fullPath);

        FileInfo fileInfo = FileInfo.builder()
                .originalName(originalFilename)
                .renamedName(renamedFilename)
                .path(fullPath.toString())
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


    @Transactional
    public FileUploadResponseDto uploadFile(MultipartFile file) throws IOException {
        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename);

        if (!isAllowedExtension(extension)) {
            throw new CommonException(ErrorCode.FILE_EXTENSION_NOT_ALLOWED);
        }

        // Generate unique filename
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

    @Transactional
    public void deleteFile(Long fileId) throws IOException {
        FileInfo fileInfo = fileInfoRepository.findById(fileId)
                .orElseThrow(() -> new CommonException(ErrorCode.FILE_NOT_FOUND));

        Path filePath = Paths.get(fileInfo.getPath());

        // 파일 권한 확인
        if (!Files.isWritable(filePath)) {
            throw new CommonException(ErrorCode.FILE_DELETE_PERMISSION_DENIED);
        }

        Files.deleteIfExists(filePath);

        fileInfoRepository.delete(fileInfo);
    }

    @Transactional
    public void deleteFileForce(Long fileId) throws IOException {
        FileInfo fileInfo = fileInfoRepository.findById(fileId)
                .orElseThrow(() -> new CommonException(ErrorCode.FILE_NOT_FOUND));

        Path filePath = Paths.get(fileInfo.getPath());

        // 파일 권한 확인
        if (!Files.isWritable(filePath)) {
            // 쓰기 권한이 없다면 권한 부여
            Files.setAttribute(filePath, "dos:readonly", false);
        }

        Files.deleteIfExists(filePath);

        fileInfoRepository.delete(fileInfo);
    }

    private String getFileExtension(String filename) {
        return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
    }

    private boolean isAllowedExtension(String extension) {
        log.info("Allowed extensions: {}", fileUploadProperties.getAllowedExtensions());
        return fileUploadProperties.getAllowedExtensions().contains(extension);
    }

    // Generate unique filename using UUID
    private String generateUniqueFilename(String originalFilename) {
        String extension = getFileExtension(originalFilename);
        return UUID.randomUUID().toString() + extension;
    }

    private void setFilePermissions(Path filePath) throws IOException {
        File file = filePath.toFile();
        if (!file.setReadable(true, false)) {
            throw new CommonException(ErrorCode.FILE_PERMISSION_SETTING_FAILED);
        }
        if (!file.setWritable(false, false)) {
            throw new CommonException(ErrorCode.FILE_PERMISSION_SETTING_FAILED);
        }
        // Window os does not support setExecutable
//        if (!file.setExecutable(false, false)) {
//            throw new CommonException(ErrorCode.FILE_PERMISSION_SETTING_FAILED);
//        }
    }
}
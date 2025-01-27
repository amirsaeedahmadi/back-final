package com.kalado.reporting.application.service;

import com.kalado.common.exception.CustomException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.net.MalformedURLException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import com.kalado.common.enums.ErrorCode;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;


@Service
@Slf4j
public class EvidenceService {
    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    @Value("${app.gateway-url:http://kaladoshop.com:8083}")
    private String gatewayUrl;

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final int MAX_FILES = 3;

    public List<String> storeEvidence(List<MultipartFile> files) {
        validateFiles(files);

        List<String> fileUrls = new ArrayList<>();
        Path uploadPath = Paths.get(uploadDir);

        try {
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            for (MultipartFile file : files) {
                String filename = generateUniqueFilename(file);
                Path targetLocation = uploadPath.resolve(filename);
                Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

                String fileUrl = gatewayUrl + "/v1/evidence/" + filename;
                fileUrls.add(fileUrl);
                log.debug("Stored evidence file: {} -> {}", file.getOriginalFilename(), fileUrl);
            }

            return fileUrls;
        } catch (IOException e) {
            log.error("Failed to store evidence files", e);
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "Failed to store evidence files");
        }
    }

    private void validateFiles(List<MultipartFile> files) {
        if (files == null) return;

        if (files.size() > MAX_FILES) {
            throw new CustomException(ErrorCode.BAD_REQUEST,
                    "Maximum " + MAX_FILES + " files allowed");
        }

        for (MultipartFile file : files) {
            if (file.getSize() > MAX_FILE_SIZE) {
                throw new CustomException(ErrorCode.BAD_REQUEST,
                        "File size must be less than 5MB: " + file.getOriginalFilename());
            }

            String contentType = file.getContentType();
            if (!isValidContentType(contentType)) {
                throw new CustomException(ErrorCode.BAD_REQUEST,
                        "Invalid file type: " + file.getOriginalFilename());
            }
        }
    }

    public Resource getEvidence(String filename) {
        try {
            Path filePath = Paths.get(uploadDir).resolve(filename).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists()) {
                return resource;
            } else {
                throw new CustomException(ErrorCode.NOT_FOUND, "Evidence file not found");
            }
        } catch (MalformedURLException e) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "Error retrieving evidence file");
        }
    }

    private boolean isValidContentType(String contentType) {
        if (contentType == null) return false;
        return contentType.startsWith("image/") ||
                contentType.equals("application/pdf") ||
                contentType.equals("text/plain");
    }

    private String generateUniqueFilename(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null ?
                originalFilename.substring(originalFilename.lastIndexOf(".")) : ".bin";
        return UUID.randomUUID().toString() + extension;
    }
}
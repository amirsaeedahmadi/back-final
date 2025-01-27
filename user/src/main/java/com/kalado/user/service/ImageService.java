package com.kalado.user.service;

import com.kalado.common.enums.ErrorCode;
import com.kalado.common.exception.CustomException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
@Slf4j
public class ImageService {
    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    @Value("${app.gateway-url:http://kaladoshop.com:8083}")
    private String gatewayUrl;

    private static final long MAX_IMAGE_SIZE = 1024 * 1024; // 1MB

    public String storeProfileImage(MultipartFile file) {
        validateImage(file);

        try {
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String filename = generateUniqueFilename(file);
            Path targetLocation = uploadPath.resolve(filename);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            String imageUrl = gatewayUrl + "/v1/images/" + filename;
            log.debug("Stored profile image: {} -> {}", file.getOriginalFilename(), imageUrl);

            return imageUrl;
        } catch (IOException e) {
            log.error("Failed to store profile image", e);
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "Failed to store profile image");
        }
    }

    public Resource getProfileImage(String filename) {
        try {
            Path filePath = Paths.get(uploadDir).resolve(filename).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists()) {
                return resource;
            } else {
                throw new CustomException(ErrorCode.NOT_FOUND, "Profile image not found");
            }
        } catch (MalformedURLException e) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "Error retrieving profile image");
        }
    }

    private void validateImage(MultipartFile file) {
        if (file.getSize() > MAX_IMAGE_SIZE) {
            throw new CustomException(ErrorCode.BAD_REQUEST, "Image size must be less than 1MB");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new CustomException(ErrorCode.BAD_REQUEST, "File must be an image");
        }
    }

    private String generateUniqueFilename(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null ?
                originalFilename.substring(originalFilename.lastIndexOf(".")) : ".jpg";
        return UUID.randomUUID().toString() + extension;
    }
}
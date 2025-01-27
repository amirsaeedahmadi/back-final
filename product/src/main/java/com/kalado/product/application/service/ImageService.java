package com.kalado.product.application.service;

import com.kalado.common.enums.ErrorCode;
import com.kalado.common.exception.CustomException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


@Service
@Slf4j
public class ImageService {
    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    @Value("${app.gateway-url:http://kaladoshop.com:8083}")
    private String gatewayUrl;

    private static final long MAX_IMAGE_SIZE = 1024 * 1024;
    private static final int MAX_IMAGES = 3;

    public List<String> storeImages(List<MultipartFile> images) {
        validateImages(images);

        List<String> imageUrls = new ArrayList<>();
        Path uploadPath = Paths.get(uploadDir);

        try {
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            for (MultipartFile image : images) {
                String filename = generateUniqueFilename(image);
                Path targetLocation = uploadPath.resolve(filename);
                Files.copy(image.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

                String imageUrl = gatewayUrl + "/v1/images/" + filename;
                imageUrls.add(imageUrl);
                log.debug("Stored image: {} -> {}", image.getOriginalFilename(), imageUrl);
            }

            return imageUrls;
        } catch (IOException e) {
            log.error("Failed to store images", e);
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "Failed to store images");
        }
    }


    private void validateImages(List<MultipartFile> images) {
        if (images.size() > MAX_IMAGES) {
            throw new CustomException(ErrorCode.BAD_REQUEST,
                    "Maximum " + MAX_IMAGES + " images allowed");
        }

        for (MultipartFile image : images) {
            if (image.getSize() > MAX_IMAGE_SIZE) {
                throw new CustomException(ErrorCode.BAD_REQUEST,
                        "Image size must be less than 1MB: " + image.getOriginalFilename());
            }

            String contentType = image.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                throw new CustomException(ErrorCode.BAD_REQUEST,
                        "Invalid file type: " + image.getOriginalFilename());
            }
        }
    }

    private String generateUniqueFilename(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null ?
                originalFilename.substring(originalFilename.lastIndexOf(".")) : ".jpg";
        return UUID.randomUUID().toString() + extension;
    }
}
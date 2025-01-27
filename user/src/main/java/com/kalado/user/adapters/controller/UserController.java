package com.kalado.user.adapters.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kalado.common.dto.AdminDto;
import com.kalado.common.dto.UserDto;
import com.kalado.common.dto.UserProfileUpdateDto;
import com.kalado.common.enums.ErrorCode;
import com.kalado.common.exception.CustomException;
import com.kalado.common.feign.user.UserApi;
import com.kalado.user.service.ImageService;
import com.kalado.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequiredArgsConstructor
@Slf4j
public class UserController implements UserApi {
  @Value("${app.upload.dir:uploads/profile}")
  private String uploadDir;
  private final UserService userService;
  private final ImageService imageService;
  private final ObjectMapper objectMapper;

  @Override
  @PostMapping(value = "/user/modifyProfile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public Boolean modifyUserProfile(
          @RequestPart("profile") String profileJson,
          @RequestPart(value = "profileImage", required = false) MultipartFile profileImage) {
    try {
      UserProfileUpdateDto profileDto = objectMapper.readValue(profileJson, UserProfileUpdateDto.class);
      return userService.modifyUserProfile(profileDto, profileImage);
    } catch (Exception e) {
      log.error("Error processing profile update: {}", e.getMessage());
      throw new CustomException(ErrorCode.BAD_REQUEST,
              "Error processing profile update: " + e.getMessage());
    }
  }

  @Override
  @GetMapping("/user/getProfile")
  public UserDto getUserProfile(@RequestParam("userId") Long userId) {
    return userService.getUserProfile(userId);
  }

  @Override
  @PostMapping("/user")
  public void createUser(@RequestBody UserDto userDto) {
    userService.createUser(userDto);
  }

  @Override
  @PostMapping("/user/admin")
  public void createAdmin(@RequestBody AdminDto adminDto) {
    userService.createAdmin(adminDto);
  }

  @Override
  @PostMapping("/user/block/{userId}")
  public boolean blockUser(@PathVariable Long userId) {
    return userService.blockUser(userId);
  }

  @Override
  @GetMapping(value = "/images/{filename}", produces = MediaType.IMAGE_JPEG_VALUE)
  public Resource getImage(@PathVariable String filename) {
    try {
      Path filePath = Paths.get(uploadDir).resolve(filename).normalize();
      Resource resource = new UrlResource(filePath.toUri());

      if (resource.exists()) {
        return resource;
      } else {
        throw new CustomException(ErrorCode.NOT_FOUND, "Image not found: " + filename);
      }
    } catch (MalformedURLException e) {
      throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "Error retrieving image");
    }
  }

}
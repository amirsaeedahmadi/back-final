package com.kalado.gateway.adapters;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kalado.common.dto.ProfileUpdateResponseDto;
import com.kalado.common.dto.UserDto;
import com.kalado.common.dto.UserProfileUpdateDto;
import com.kalado.common.enums.ErrorCode;
import com.kalado.common.exception.CustomException;
import com.kalado.common.feign.user.UserApi;
import com.kalado.gateway.annotation.Authentication;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/v1/user")
@RequiredArgsConstructor
@Slf4j
public class UserController {
  private final UserApi userApi;
  private final ObjectMapper objectMapper;

  @PostMapping(value = "/modifyProfile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @Authentication(userId = "#userId")
  public ProfileUpdateResponseDto modifyUserProfile(
          Long userId,
          @RequestParam("profile") String profileJson,
          @RequestParam(value = "profileImage", required = false) MultipartFile profileImage) {
    try {
      UserProfileUpdateDto profileDto = objectMapper.readValue(profileJson, UserProfileUpdateDto.class);

      profileDto.setId(userId);

      String updatedProfileJson = objectMapper.writeValueAsString(profileDto);

      log.debug("Modifying profile with data: {} and image present: {}",
              profileDto, profileImage != null);

      return userApi.modifyUserProfile(updatedProfileJson, profileImage);
    } catch (JsonProcessingException e) {
      log.error("Error parsing profile JSON: {}", e.getMessage());
      throw new CustomException(ErrorCode.BAD_REQUEST,
              "Invalid profile data format: " + e.getMessage());
    } catch (Exception e) {
      log.error("Error modifying user profile: {}", e.getMessage(), e);
      throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR,
              "Error processing profile update: " + e.getMessage());
    }
  }

  @GetMapping("/profile")
  @Authentication(userId = "#userId")
  public ResponseEntity<UserDto> getUserProfile(Long userId) {
    try {
      log.debug("Fetching user profile for userId: {}", userId);
      UserDto userProfile = userApi.getUserProfile(userId);

      if (userProfile == null) {
        log.warn("No profile found for userId: {}", userId);
        return ResponseEntity.notFound().build();
      }

      return ResponseEntity.ok(userProfile);

    } catch (Exception e) {
      log.error("Error fetching user profile for userId {}: {}", userId, e.getMessage());
      throw new CustomException(
              ErrorCode.INTERNAL_SERVER_ERROR,
              "Error retrieving user profile: " + e.getMessage()
      );
    }
  }

  @PostMapping("/user/block/{userId}")
  @Authentication(userId = "#userId")
  boolean blockUser(Long userId) {
    return userApi.blockUser(userId);
  }

  @GetMapping("/all")
  @Authentication(userId = "#userId")
  public ResponseEntity<List<UserDto>> getAllUsers(Long userId) {
    try {
      log.debug("Fetching all users. Request made by user ID: {}", userId);
      List<UserDto> users = userApi.getAllUsers();
      return ResponseEntity.ok(users);
    } catch (Exception e) {
      log.error("Error fetching all users: {}", e.getMessage());
      throw new CustomException(
              ErrorCode.INTERNAL_SERVER_ERROR,
              "Error retrieving users: " + e.getMessage()
      );
    }
  }
}
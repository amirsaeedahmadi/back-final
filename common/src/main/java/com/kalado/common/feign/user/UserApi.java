package com.kalado.common.feign.user;

import com.kalado.common.dto.AdminDto;
import com.kalado.common.dto.ProfileUpdateResponseDto;
import com.kalado.common.dto.UserDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@FeignClient(name = "user-service")
public interface UserApi {
  @PostMapping(value = "/user/modifyProfile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  ProfileUpdateResponseDto modifyUserProfile(
          @RequestPart("profile") String profileJson,
          @RequestPart(value = "profileImage", required = false) MultipartFile profileImage
  );

  @GetMapping("/user/getProfile")
  UserDto getUserProfile(@RequestParam("userId") Long userId);

  @PostMapping("/user")
  void createUser(@RequestBody UserDto userDto);

  @PostMapping("/user/admin")
  void createAdmin(@RequestBody AdminDto adminDto);

  @PostMapping("/user/block/{userId}")
  boolean blockUser(@PathVariable Long userId);

  @GetMapping(value = "/images/{filename}", produces = MediaType.IMAGE_JPEG_VALUE)
  Resource getImage(@PathVariable String filename);

  @GetMapping("/user/all")
  List<UserDto> getAllUsers();
}
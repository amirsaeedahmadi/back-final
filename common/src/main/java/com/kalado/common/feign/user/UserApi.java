package com.kalado.common.feign.user;

import com.kalado.common.configuration.FeignMultipartConfig;
import com.kalado.common.dto.AdminDto;
import com.kalado.common.dto.UserDto;
import feign.Headers;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@FeignClient(name = "user-service")
public interface UserApi {
  @PostMapping(value = "/user/modifyProfile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  Boolean modifyUserProfile(
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
}
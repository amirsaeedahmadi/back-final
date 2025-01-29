package com.kalado.common.feign.authentication;

import com.kalado.common.dto.*;
import com.kalado.common.enums.Role;
import com.kalado.common.response.LoginResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "authentication-service")
public interface AuthenticationApi {
  @PostMapping("auth/login")
  LoginResponse login(@RequestParam String username, @RequestParam String password);

  @GetMapping("auth/validate")
  AuthDto validate(@RequestParam String token);

  @GetMapping("auth/info")
  String getUsername(@RequestParam Long userId);

  @PostMapping("auth/logout")
  void logout(@RequestParam String token);

  @PostMapping("auth/register")
  void register(@RequestBody RegistrationRequestDto registrationRequest);

  @PostMapping("/auth/verify")
  String verifyEmail(@RequestParam String token);

  @PostMapping("/auth/forgot-password")
  void forgotPassword(@RequestBody ForgotPasswordRequestDto request);

  @PostMapping("/auth/reset-password")
  ResetPasswordResponseDto resetPassword(@RequestBody ResetPasswordRequestDto request);

  @PostMapping("/auth/update-password")
  void updatePassword(
          @RequestParam Long userId,
          @RequestParam String currentPassword,
          @RequestParam String newPassword
  );

  @PutMapping("/auth/roles/{userId}")
  void updateUserRole(
          @PathVariable Long userId,
          @RequestParam Role newRole,
          @RequestParam Long requestingUserId
  );
}
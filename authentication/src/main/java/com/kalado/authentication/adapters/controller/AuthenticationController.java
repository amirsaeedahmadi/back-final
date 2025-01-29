package com.kalado.authentication.adapters.controller;

import com.kalado.authentication.application.service.AuthenticationService;
import com.kalado.authentication.application.service.PasswordResetService;
import com.kalado.authentication.application.service.VerificationService;
import com.kalado.common.dto.*;
import com.kalado.common.enums.ErrorCode;
import com.kalado.common.enums.Role;
import com.kalado.common.exception.CustomException;
import com.kalado.common.feign.authentication.AuthenticationApi;
import com.kalado.common.response.LoginResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class AuthenticationController implements AuthenticationApi {

  private final AuthenticationService authService;
  private final VerificationService verificationService;
  private final PasswordResetService passwordResetService;

  @Override
  public LoginResponse login(String username, String password) {
    var user = authService.findByUsername(username);
    if (user != null && !verificationService.isEmailVerified(user)) {
      throw new CustomException(ErrorCode.UNAUTHORIZED, "Email not verified");
    }
    return authService.login(username, password);
  }

  @PostMapping("/auth/verify")
  public String verifyEmail(@RequestParam String token) {
    boolean verified = verificationService.verifyEmail(token);
    if (verified) {
      return "Email verified successfully";
    }
    return "Invalid or expired token";
  }

  @PostMapping("/auth/resend-verification")
  public String resendVerificationToken(@RequestParam String username) {
    var user = authService.findByUsername(username);
    if (user != null && !verificationService.isEmailVerified(user)) {
      verificationService.resendVerificationToken(user);
      return "Verification code sent";
    }
    return "Invalid request or email already verified";
  }

  @Override
  public AuthDto validate(String token) {
    return authService.validateToken(token);
  }

  @Override
  public String getUsername(Long userId) {
    return authService.getUsername(userId);
  }

  @Override
  public void logout(String token) {
    authService.invalidateToken(token);
  }

  @Override
  public void register(@RequestBody RegistrationRequestDto registrationRequest) {
    authService.register(registrationRequest);
  }

  @Override
  @PostMapping("/auth/forgot-password")
  public void forgotPassword(@RequestBody ForgotPasswordRequestDto request) {
    passwordResetService.createPasswordResetTokenForUser(request.getEmail());
  }

  @Override
  @PostMapping("/auth/reset-password")
  public ResetPasswordResponseDto resetPassword(@RequestBody ResetPasswordRequestDto request) {
    return passwordResetService.resetPassword(request);
  }

  @Override
  public void updatePassword(Long userId, String currentPassword, String newPassword) {
    var user = authService.findUserById(userId)
            .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "User not found"));

    if (!authService.verifyPassword(user, currentPassword)) {
      throw new CustomException(ErrorCode.INVALID_CREDENTIALS, "Current password is incorrect");
    }

    authService.updateUserPassword(userId, newPassword);
  }

  @Override
  @PutMapping("/auth/roles/{userId}")
  public void updateUserRole(
          @PathVariable Long userId,
          @RequestParam Role newRole,
          @RequestParam Long requestingUserId) {

    var requestingUser = authService.findUserById(requestingUserId)
            .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "Requesting user not found"));

    var targetUser = authService.findUserById(userId)
            .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "Target user not found"));

    validateRoleChange(requestingUser.getRole(), targetUser.getRole(), newRole);

    authService.updateUserRole(userId, newRole, requestingUserId);
  }


  private void validateRoleChange(Role requestingRole, Role currentRole, Role newRole) {
    if (requestingRole != Role.GOD) {
      throw new CustomException(
              ErrorCode.INSUFFICIENT_PRIVILEGES,
              "Only GOD role can modify user roles"
      );
    }

    if (currentRole == Role.GOD) {
      throw new CustomException(
              ErrorCode.GOD_ROLE_MODIFICATION_FORBIDDEN,
              "Cannot modify GOD role"
      );
    }

    boolean isValidTransition = switch (currentRole) {
      case USER -> newRole == Role.ADMIN;
      case ADMIN -> newRole == Role.USER;
      default -> false;
    };

    if (!isValidTransition) {
      throw new CustomException(
              ErrorCode.INVALID_ROLE_TRANSITION,
              "Invalid role transition from " + currentRole + " to " + newRole
      );
    }
  }
}
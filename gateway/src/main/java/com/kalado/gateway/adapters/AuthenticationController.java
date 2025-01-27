package com.kalado.gateway.adapters;

import com.kalado.common.dto.*;
import com.kalado.common.feign.authentication.AuthenticationApi;
import com.kalado.common.response.LoginResponse;
import com.kalado.gateway.annotation.Authentication;
import com.kalado.gateway.request.LoginRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
public class AuthenticationController {
  private final AuthenticationApi authenticationApi;

  @PostMapping("/login")
  public LoginResponse login(@RequestBody LoginRequest loginRequest) {
    return authenticationApi.login(loginRequest.getEmail(), loginRequest.getPassword());
  }

  @PostMapping("/register")
  public void register(@RequestBody RegistrationRequestDto registrationRequest) {
    authenticationApi.register(registrationRequest);
  }

  @GetMapping("/validate")
  @Authentication(token = "#token")
  boolean validate(String token) {
    return authenticationApi.validate(token).isValid();
  }

  @PostMapping("/logout")
  @Authentication(token = "#token")
  public void logout(String token) {
    authenticationApi.logout(token);
  }

  @PostMapping("/verify")
  public void verifyEmail(String token) {
    authenticationApi.verifyEmail(token);
  }

  @PostMapping("/forgot-password")
  public void forgotPassword(@RequestBody ForgotPasswordRequestDto request) {
    authenticationApi.forgotPassword(request);
  }

  @PostMapping("/reset-password")
  public ResetPasswordResponseDto resetPassword(@RequestBody ResetPasswordRequestDto request) {
    return authenticationApi.resetPassword(request);
  }
}
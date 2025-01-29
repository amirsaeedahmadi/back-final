package com.kalado.authentication.application.service;

import com.kalado.authentication.configuration.AdminConfiguration;
import com.kalado.authentication.domain.model.AuthenticationInfo;
import com.kalado.common.dto.AuthDto;
import com.kalado.common.dto.AdminDto;
import com.kalado.common.dto.RegistrationRequestDto;
import com.kalado.common.dto.UserDto;
import com.kalado.common.enums.ErrorCode;
import com.kalado.common.enums.Role;
import com.kalado.common.exception.CustomException;
import com.kalado.common.feign.user.UserApi;
import com.kalado.common.response.LoginResponse;
import com.kalado.authentication.infrastructure.repository.AuthenticationRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.util.*;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationService {

  private final AuthenticationRepository authRepository;
  private final BCryptPasswordEncoder passwordEncoder;
  private final RedisTemplate<String, Long> redisTemplate;
  private final UserApi userApi;
  private final VerificationService verificationService;
  private final AdminConfiguration adminConfig;

  private static final String SECRET_KEY =
      "X71wHJEhg1LQE5DzWcdc/BRAgIvnqHYiZHBbqgrBOZLzwlHlHh/W1ScQGwd1XM8V1c5vtgGlDS8lb64zjZEZXg==";
  private static final long TOKEN_EXPIRATION_TIME = 24 * 60 * 60 * 1000;

  public AuthenticationInfo findByUsername(String username) {
    return authRepository.findByUsername(username);
  }

  public LoginResponse login(String username, String password) {
    validateLoginInput(username, password);

    AuthenticationInfo authInfo = authRepository.findByUsername(username);
    if (authInfo == null || !passwordEncoder.matches(password, authInfo.getPassword())) {
      log.warn("Invalid login attempt for username: {}", username);
      throw new CustomException(ErrorCode.INVALID_CREDENTIALS, "Invalid username or password");
    }

    if (!verificationService.isEmailVerified(authInfo)) {
      log.warn("Email not verified for username: {}", username);
      throw new CustomException(ErrorCode.EMAIL_NOT_VERIFIED, "Email not verified");
    }

    if (authInfo.getRole() == Role.USER) {
      try {
        UserDto userDto = userApi.getUserProfile(authInfo.getUserId());
        if (userDto != null && userDto.isBlocked()) {
          log.warn("Blocked user attempted to login: {}", username);
          throw new CustomException(ErrorCode.UNAUTHORIZED, "Your account has been blocked");
        }
      } catch (Exception e) {
        log.error("Error checking user blocked status: {}", e.getMessage());
        if (e instanceof CustomException) {
          throw e;
        }
        throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "Error validating user status");
      }
    }

    String token = generateToken(authInfo.getUserId());
    return LoginResponse.builder().token(token).role(authInfo.getRole()).build();
  }

  public AuthDto validateToken(String tokenValue) {
    Optional<Claims> claims = extractAllClaims(tokenValue);

    if (claims.isEmpty()) {
      return AuthDto.builder().isValid(false).build();
    }

    String userId = claims.get().getSubject();
    if (!isTokenValid(tokenValue, userId, claims.get().getExpiration())) {
      return AuthDto.builder().isValid(false).build();
    }

    return authRepository
        .findById(Long.valueOf(userId))
        .map(
            authInfo ->
                AuthDto.builder()
                    .isValid(true)
                    .userId(authInfo.getUserId())
                    .role(authInfo.getRole())
                    .build())
        .orElseGet(() -> AuthDto.builder().isValid(false).build());
  }

  public void invalidateToken(String token) {
    if (Boolean.TRUE.equals(redisTemplate.hasKey(token))) {
      redisTemplate.delete(token);
      log.info("Token invalidated: {}", token);
    }
  }

  private String generateToken(long userId) {
    long nowMillis = System.currentTimeMillis();
    long expMillis = nowMillis + TOKEN_EXPIRATION_TIME;

    String token = generateTokenValue(userId, expMillis, nowMillis);
    redisTemplate.opsForValue().set(token, userId, TOKEN_EXPIRATION_TIME, TimeUnit.MILLISECONDS);
    return token;
  }

  private String generateTokenValue(long userId, long expMillis, long nowMillis) {
    return Jwts.builder()
        .setSubject(String.valueOf(userId))
        .setIssuedAt(new Date(nowMillis))
        .setExpiration(new Date(expMillis))
        .setId(UUID.randomUUID().toString())
        .signWith(getSignInKey(), SignatureAlgorithm.HS256)
        .compact();
  }

  private Key getSignInKey() {
    byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
    return Keys.hmacShaKeyFor(keyBytes);
  }

  private Optional<Claims> extractAllClaims(String token) {
    try {
      return Optional.ofNullable(
          Jwts.parserBuilder()
              .setSigningKey(getSignInKey())
              .build()
              .parseClaimsJws(token)
              .getBody());
    } catch (Exception e) {
      log.error("Failed to parse token: {}", e.getMessage());
      return Optional.empty();
    }
  }

  private boolean isTokenValid(String token, String userId, Date expiration) {
    return Boolean.TRUE.equals(redisTemplate.hasKey(token))
        && Objects.nonNull(userId)
        && new Date().before(expiration);
  }


  @Transactional
  public AuthenticationInfo register(RegistrationRequestDto request) {
    validateRegistrationInput(request);

    if (request.getRole() == Role.GOD || request.getRole() == Role.ADMIN) {
      validatePrivilegedRegistration(request.getEmail(), request.getRole());
    }

    AuthenticationInfo existingUser = authRepository.findByUsername(request.getEmail());
    if (existingUser != null) {
      log.info("User already exists: {}", existingUser);
      throw new CustomException(ErrorCode.USER_ALREADY_EXISTS, "User already exists");
    }

    String encodedPassword = passwordEncoder.encode(request.getPassword());

    AuthenticationInfo authenticationInfo = authRepository.save(
            AuthenticationInfo.builder()
                    .username(request.getEmail())
                    .password(encodedPassword)
                    .role(request.getRole())
                    .build());

    UserDto userDto = UserDto.builder()
            .id(authenticationInfo.getUserId())
            .username(request.getEmail())
            .firstName(request.getFirstName())
            .lastName(request.getLastName())
            .phoneNumber(request.getPhoneNumber())
            .build();

    switch (request.getRole()) {
      case GOD, ADMIN -> userApi.createAdmin(AdminDto.builder()
              .id(authenticationInfo.getUserId())
              .firstName(request.getFirstName())
              .lastName(request.getLastName())
              .phoneNumber(request.getPhoneNumber())
              .build());
      case USER -> userApi.createUser(userDto);
    }

    verificationService.createVerificationToken(authenticationInfo);

    return authenticationInfo;
  }

  private void validatePrivilegedRegistration(String email, Role role) {
    if (role == Role.GOD) {
      if (!adminConfig.isEmailAuthorizedForGod(email)) {
        log.warn("Unauthorized attempt to register as GOD: {}", email);
        throw new CustomException(
                ErrorCode.FORBIDDEN,
                "GOD registration is restricted to authorized emails only"
        );
      }
    } else if (role == Role.ADMIN) {
      if (!adminConfig.isEmailAuthorizedForAdmin(email)) {
        log.warn("Unauthorized attempt to register as admin: {}", email);
        throw new CustomException(
                ErrorCode.FORBIDDEN,
                "Admin registration is restricted to authorized emails only"
        );
      }
    }
  }

  @Transactional
  public void updateUserRole(Long userId, Role newRole, Long requestingUserId) {
    AuthenticationInfo requestingUser = findUserById(requestingUserId)
            .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "Requesting user not found"));

    if (requestingUser.getRole() != Role.GOD) {
      throw new CustomException(ErrorCode.FORBIDDEN, "Only GOD role can modify user roles");
    }

    AuthenticationInfo targetUser = findUserById(userId)
            .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "Target user not found"));

    if (targetUser.getRole() == Role.GOD) {
      throw new CustomException(ErrorCode.FORBIDDEN, "Cannot modify GOD role");
    }

    targetUser.setRole(newRole);
    authRepository.save(targetUser);

    if (newRole == Role.ADMIN) {
      UserDto userProfile = userApi.getUserProfile(userId);
      userApi.createAdmin(AdminDto.builder()
              .id(userId)
              .firstName(userProfile.getFirstName())
              .lastName(userProfile.getLastName())
              .phoneNumber(userProfile.getPhoneNumber())
              .build());
    }
  }

  private void validateAdminRegistration(String email) {
      if (!adminConfig.isEmailAuthorizedForAdmin(email)) {
        log.warn("Unauthorized attempt to register as admin: {}", email);
        throw new CustomException(
                ErrorCode.FORBIDDEN,
                "Initial admin registration is restricted to authorized emails only"
        );
      }
  }

  private void validateRegistrationInput(RegistrationRequestDto request) {
    if (request.getEmail() == null || request.getEmail().isEmpty()) {
      throw new CustomException(ErrorCode.INVALID_CREDENTIALS, "Username cannot be empty");
    }
    if (request.getPassword() == null || request.getPassword().isEmpty()) {
      throw new CustomException(ErrorCode.INVALID_CREDENTIALS, "Password cannot be empty");
    }
    if (request.getFirstName() == null || request.getFirstName().isEmpty()) {
      throw new CustomException(ErrorCode.INVALID_CREDENTIALS, "First name cannot be empty");
    }
    if (request.getLastName() == null || request.getLastName().isEmpty()) {
      throw new CustomException(ErrorCode.INVALID_CREDENTIALS, "Last name cannot be empty");
    }
    if (request.getPhoneNumber() == null || request.getPhoneNumber().isEmpty()) {
      throw new CustomException(ErrorCode.INVALID_CREDENTIALS, "Phone number cannot be empty");
    }
    if (request.getRole() == null) {
      throw new CustomException(ErrorCode.INVALID_CREDENTIALS, "Role cannot be null");
    }
  }

  private void validateLoginInput(String username, String password) {
    if (username == null || username.isEmpty()) {
      throw new CustomException(ErrorCode.INVALID_CREDENTIALS, "Username cannot be empty");
    }
    if (password == null || password.isEmpty()) {
      throw new CustomException(ErrorCode.INVALID_CREDENTIALS, "Password cannot be empty");
    }
  }

  private void validateRegistrationInput(String username, String password, Role role) {
    if (username == null || username.isEmpty()) {
      throw new CustomException(ErrorCode.INVALID_CREDENTIALS, "Username cannot be empty");
    }
    if (password == null || password.isEmpty()) {
      throw new CustomException(ErrorCode.INVALID_CREDENTIALS, "Password cannot be empty");
    }
    if (role == null) {
      throw new CustomException(ErrorCode.INVALID_CREDENTIALS, "Role cannot be null");
    }
  }

  public String getUsername(Long userId) {
    return authRepository.findById(userId).map(AuthenticationInfo::getUsername).orElse(null);
  }

  @Transactional
  public void updatePassword(Long userId, String currentPassword, String newPassword) {
    AuthenticationInfo authInfo = authRepository.findById(userId)
            .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "User not found"));

    if (!passwordEncoder.matches(currentPassword, authInfo.getPassword())) {
      throw new CustomException(ErrorCode.INVALID_CREDENTIALS, "Current password is incorrect");
    }

    authInfo.setPassword(passwordEncoder.encode(newPassword));
    authRepository.save(authInfo);

    redisTemplate.keys("*").stream()
            .filter(key -> redisTemplate.opsForValue().get(key).equals(userId))
            .forEach(redisTemplate::delete);

    log.info("Password updated successfully for user ID: {}", userId);
  }

  public boolean verifyPassword(AuthenticationInfo user, String password) {
    return passwordEncoder.matches(password, user.getPassword());
  }

  @Transactional
  public void updateUserPassword(Long userId, String newPassword) {
    AuthenticationInfo user = authRepository.findById(userId)
            .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "User not found"));

    user.setPassword(passwordEncoder.encode(newPassword));
    authRepository.save(user);

    invalidateUserTokens(userId);
  }

  private void invalidateUserTokens(Long userId) {
    Set<String> keys = redisTemplate.keys("*");
    if (keys != null) {
      keys.stream()
              .filter(key -> redisTemplate.opsForValue().get(key) != null)
              .filter(key -> redisTemplate.opsForValue().get(key).equals(userId))
              .forEach(redisTemplate::delete);
    }
  }

  public Optional<AuthenticationInfo> findUserById(Long userId) {
    return authRepository.findById(userId);
  }
}

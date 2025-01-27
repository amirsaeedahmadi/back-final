package com.kalado.authentication;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;

import com.kalado.common.dto.AuthDto;
import com.kalado.common.dto.RegistrationRequestDto;
import com.kalado.common.dto.UserDto;
import com.kalado.common.enums.ErrorCode;
import com.kalado.common.enums.Role;
import com.kalado.common.exception.CustomException;
import com.kalado.common.feign.user.UserApi;
import com.kalado.authentication.application.service.AuthenticationService;
import com.kalado.authentication.application.service.EmailService;
import com.kalado.authentication.application.service.VerificationService;
import com.kalado.authentication.domain.model.AuthenticationInfo;
import com.kalado.authentication.infrastructure.repository.AuthenticationRepository;
import org.junit.jupiter.api.*;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AuthenticationServiceIntegrationTest {

  @Autowired
  private AuthenticationService authenticationService;

  @Autowired
  private AuthenticationRepository authRepository;

  @Autowired
  private RedisTemplate<String, Long> redisTemplate;

  @Autowired
  private PasswordEncoder passwordEncoder;

  @MockBean
  private UserApi userApi;

  @MockBean
  private EmailService emailService;

  @MockBean
  private VerificationService verificationService;

  @BeforeEach
  void setUp() {
    authRepository.deleteAll();
    authRepository.flush();
    // Mock verification service to return true for tests
    Mockito.when(verificationService.isEmailVerified(any())).thenReturn(true);
  }

  @Test
  void login_ShouldThrowException_WhenCredentialsAreInvalid() {
    AuthenticationInfo authInfo = AuthenticationInfo.builder()
            .username("invaliduser")
            .password(passwordEncoder.encode("password"))
            .role(Role.USER)
            .build();
    authRepository.save(authInfo);

    CustomException exception = assertThrows(
            CustomException.class,
            () -> authenticationService.login("invaliduser", "wrongpassword"));

    assertEquals(
            "Invalid username or password",
            exception.getMessage(),
            "Exception message should indicate invalid credentials");
  }

  @Test
  void validateToken_ShouldReturnInvalidAuthDto_WhenTokenIsInvalid() {
    String invalidToken = "invalid.token.value";

    AuthDto authDto = authenticationService.validateToken(invalidToken);

    assertNotNull(authDto, "AuthDto should not be null");
    assertFalse(authDto.isValid(), "AuthDto should be invalid");
  }

  @Test
  void register_ShouldCreateNewUser_WhenDataIsValid() {
    // Arrange
    RegistrationRequestDto request = RegistrationRequestDto.builder()
            .password("newpassword")
            .firstName("John")
            .lastName("Doe")
            .phoneNumber("1234567890")
            .email("john.doe@example.com")
            .role(Role.USER)
            .build();

    Mockito.doNothing().when(userApi).createUser(any());
    Mockito.doNothing().when(verificationService).createVerificationToken(any());

    // Act
    AuthenticationInfo savedUser = authenticationService.register(request);

    // Assert
    assertNotNull(savedUser, "Saved user should not be null");
    assertEquals(request.getEmail(), savedUser.getUsername(), "Username should match");
    assertTrue(
            passwordEncoder.matches(request.getPassword(), savedUser.getPassword()),
            "Password should be properly encoded"
    );
    assertEquals(request.getRole(), savedUser.getRole(), "Role should match");

    // Verify profile creation
    ArgumentCaptor<UserDto> userDtoCaptor = ArgumentCaptor.forClass(UserDto.class);
    Mockito.verify(userApi).createUser(userDtoCaptor.capture());

    UserDto capturedUserDto = userDtoCaptor.getValue();
    assertEquals(request.getFirstName(), capturedUserDto.getFirstName());
    assertEquals(request.getLastName(), capturedUserDto.getLastName());
    assertEquals(request.getPhoneNumber(), capturedUserDto.getPhoneNumber());

    // Verify email verification
    Mockito.verify(verificationService).createVerificationToken(any());
  }

  @Test
  void register_ShouldThrowException_WhenUserAlreadyExists() {
    RegistrationRequestDto request = RegistrationRequestDto.builder()
            .password("newpassword")
            .firstName("John")
            .lastName("Doe")
            .phoneNumber("1234567890")
            .email("john.doe@example.com")
            .role(Role.USER)
            .build();

    AuthenticationInfo existingUser = AuthenticationInfo.builder()
            .username(request.getEmail())
            .password(passwordEncoder.encode("oldpassword"))
            .role(Role.USER)
            .build();
    authRepository.save(existingUser);

    CustomException exception = assertThrows(
            CustomException.class,
            () -> authenticationService.register(request)
    );

    assertEquals(ErrorCode.USER_ALREADY_EXISTS, exception.getErrorCode());
    assertEquals("User already exists", exception.getMessage());

    Mockito.verify(userApi, never()).createUser(any());
    Mockito.verify(verificationService, never()).createVerificationToken(any());
  }

  @Test
  void login_ShouldThrowException_WhenEmailNotVerified() {
    AuthenticationInfo authInfo = AuthenticationInfo.builder()
            .username("unverifieduser")
            .password(passwordEncoder.encode("password"))
            .role(Role.USER)
            .build();
    authRepository.save(authInfo);

    // Mock verification service to return false
    Mockito.when(verificationService.isEmailVerified(any())).thenReturn(false);

    CustomException exception = assertThrows(
            CustomException.class,
            () -> authenticationService.login("unverifieduser", "password"));

    assertEquals(
            "Email not verified",
            exception.getMessage(),
            "Exception message should indicate email not verified");
  }
}
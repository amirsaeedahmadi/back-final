//package com.kalado.authentication;
//
//import com.kalado.authentication.application.service.AuthenticationService;
//import com.kalado.authentication.application.service.PasswordResetService;
//import com.kalado.authentication.application.service.VerificationService;
//import com.kalado.authentication.domain.model.AuthenticationInfo;
//import com.kalado.authentication.domain.model.PasswordResetToken;
//import com.kalado.authentication.domain.model.VerificationToken;
//import com.kalado.authentication.infrastructure.repository.AuthenticationRepository;
//import com.kalado.authentication.infrastructure.repository.PasswordResetTokenRepository;
//import com.kalado.authentication.infrastructure.repository.VerificationTokenRepository;
//import com.kalado.common.dto.RegistrationRequestDto;
//import com.kalado.common.dto.ResetPasswordRequestDto;
//import com.kalado.common.dto.UserDto;
//import com.kalado.common.enums.Role;
//import com.kalado.common.exception.CustomException;
//import com.kalado.common.feign.user.UserApi;
//import com.kalado.common.response.LoginResponse;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.mail.SimpleMailMessage;
//import org.springframework.mail.javamail.JavaMailSender;
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
//
//import javax.persistence.EntityManager;
//import javax.persistence.PersistenceContext;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.doNothing;
//import static org.mockito.Mockito.when;
//
//@SpringBootTest
//class AuthenticationFlowIntegrationTest extends BaseIntegrationTest {
//    @PersistenceContext
//    private EntityManager entityManager;
//
//    @Autowired
//    private AuthenticationService authenticationService;
//
//    @Autowired
//    private AuthenticationRepository authRepository;
//
//    @MockBean
//    private JavaMailSender mailSender;
//
//    @Autowired
//    private VerificationService verificationService;
//
//    @Autowired
//    private PasswordResetService passwordResetService;
//
//    @Autowired
//    private VerificationTokenRepository verificationTokenRepository;
//
//    @Autowired
//    private PasswordResetTokenRepository passwordResetTokenRepository;
//
//    @Autowired
//    private BCryptPasswordEncoder passwordEncoder;
//
//    @MockBean
//    private UserApi userApi;
//
//    private static final String TEST_EMAIL = "test@example.com";
//    private static final String TEST_PASSWORD = "TestPassword123";
//
//    @BeforeEach
//    void setUp() {
//        clearDatabase(entityManager);
//        // Clean all repositories
//        authRepository.deleteAll();
//        verificationTokenRepository.deleteAll();
//        passwordResetTokenRepository.deleteAll();
//        doNothing().when(mailSender).send(any(SimpleMailMessage.class));
//
//
//        // Mock user API responses - Fix for void method
//        doNothing().when(userApi).createUser(any(UserDto.class));
//        when(userApi.getUserProfile(any())).thenReturn(UserDto.builder().blocked(false).build());
//    }
//
//    @Test
//    void completeAuthenticationFlow() {
//        // Step 1: Register a new user
//        RegistrationRequestDto registrationRequest = RegistrationRequestDto.builder()
//                .email(TEST_EMAIL)
//                .password(TEST_PASSWORD)
//                .firstName("Test")
//                .lastName("User")
//                .phoneNumber("1234567890")
//                .role(Role.USER)
//                .build();
//
//        AuthenticationInfo registeredUser = authenticationService.register(registrationRequest);
//        assertNotNull(registeredUser);
//        assertEquals(TEST_EMAIL, registeredUser.getUsername());
//
//        // Step 2: Verify email fails before verification
//        assertThrows(CustomException.class,
//                () -> authenticationService.login(TEST_EMAIL, TEST_PASSWORD));
//
//        // Step 3: Get and use verification token
//        VerificationToken verificationToken = verificationTokenRepository
//                .findByUser_UserId(registeredUser.getUserId())
//                .orElseThrow();
//        boolean verificationResult = verificationService.verifyEmail(verificationToken.getToken());
//        assertTrue(verificationResult);
//
//        // Step 4: Login successfully after verification
//        LoginResponse loginResponse = authenticationService.login(TEST_EMAIL, TEST_PASSWORD);
//        assertNotNull(loginResponse);
//        assertNotNull(loginResponse.getToken());
//        assertEquals(Role.USER, loginResponse.getRole());
//
//        // Step 5: Request password reset
//        passwordResetService.createPasswordResetTokenForUser(TEST_EMAIL);
//        PasswordResetToken resetToken = passwordResetTokenRepository.findAll().get(0);
//        assertNotNull(resetToken);
//
//        // Step 6: Reset password
//        String newPassword = "NewPassword456";
//        ResetPasswordRequestDto resetRequest = ResetPasswordRequestDto.builder()
//                .token(resetToken.getToken())
//                .newPassword(newPassword)
//                .build();
//
//        var resetResponse = passwordResetService.resetPassword(resetRequest);
//        assertTrue(resetResponse.isSuccess());
//
//        // Step 7: Verify old password fails
//        assertThrows(CustomException.class,
//                () -> authenticationService.login(TEST_EMAIL, TEST_PASSWORD));
//
//        // Step 8: Login with new password succeeds
//        LoginResponse newLoginResponse = authenticationService.login(TEST_EMAIL, newPassword);
//        assertNotNull(newLoginResponse);
//        assertNotNull(newLoginResponse.getToken());
//    }
//
//    @Test
//    void userBlockingFlow() {
//        // Step 1: Register and verify user
//        RegistrationRequestDto registrationRequest = RegistrationRequestDto.builder()
//                .email(TEST_EMAIL)
//                .password(TEST_PASSWORD)
//                .firstName("Test")
//                .lastName("User")
//                .phoneNumber("1234567890")
//                .role(Role.USER)
//                .build();
//
//        AuthenticationInfo registeredUser = authenticationService.register(registrationRequest);
//        VerificationToken verificationToken = verificationTokenRepository
//                .findByUser_UserId(registeredUser.getUserId())
//                .orElseThrow();
//        verificationService.verifyEmail(verificationToken.getToken());
//
//        // Step 2: Login succeeds when user is not blocked
//        when(userApi.getUserProfile(any())).thenReturn(UserDto.builder().blocked(false).build());
//        assertDoesNotThrow(() ->
//                authenticationService.login(TEST_EMAIL, TEST_PASSWORD)
//        );
//
//        // Step 3: Login fails when user is blocked
//        when(userApi.getUserProfile(any())).thenReturn(UserDto.builder().blocked(true).build());
//        assertThrows(CustomException.class,
//                () -> authenticationService.login(TEST_EMAIL, TEST_PASSWORD));
//    }
//
//    @Test
//    void tokenValidationFlow() {
//        // Step 1: Register and login user
//        RegistrationRequestDto registrationRequest = RegistrationRequestDto.builder()
//                .email(TEST_EMAIL)
//                .password(TEST_PASSWORD)
//                .firstName("Test")
//                .lastName("User")
//                .phoneNumber("1234567890")
//                .role(Role.USER)
//                .build();
//
//        authenticationService.register(registrationRequest);
//        VerificationToken verificationToken = verificationTokenRepository
//                .findByUser_UserId(authRepository.findByUsername(TEST_EMAIL).getUserId())
//                .orElseThrow();
//        verificationService.verifyEmail(verificationToken.getToken());
//
//        LoginResponse loginResponse = authenticationService.login(TEST_EMAIL, TEST_PASSWORD);
//        String token = loginResponse.getToken();
//
//        // Step 2: Validate token
//        var authDto = authenticationService.validateToken(token);
//        assertTrue(authDto.isValid());
//        assertEquals(Role.USER, authDto.getRole());
//
//        // Step 3: Invalidate token
//        authenticationService.invalidateToken(token);
//        var invalidatedAuthDto = authenticationService.validateToken(token);
//        assertFalse(invalidatedAuthDto.isValid());
//    }
//}
//package com.kalado.authentication;
//
//import com.kalado.authentication.application.service.PasswordResetService;
//import com.kalado.authentication.domain.model.AuthenticationInfo;
//import com.kalado.authentication.domain.model.PasswordResetToken;
//import com.kalado.authentication.infrastructure.repository.AuthenticationRepository;
//import com.kalado.authentication.infrastructure.repository.PasswordResetTokenRepository;
//import com.kalado.common.dto.ResetPasswordRequestDto;
//import com.kalado.common.dto.ResetPasswordResponseDto;
//import com.kalado.common.enums.ErrorCode;
//import com.kalado.common.enums.Role;
//import com.kalado.common.exception.CustomException;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.mail.SimpleMailMessage;
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
//import org.springframework.mail.javamail.JavaMailSender;
//import org.springframework.test.context.TestPropertySource;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.time.LocalDateTime;
//import java.util.List;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.doNothing;
//
//@SpringBootTest
//@TestPropertySource(properties = {
//        "spring.mail.host=smtp.gmail.com",
//        "spring.mail.port=587",
//        "spring.mail.username=test@example.com",
//        "spring.mail.password=test"
//})
//class PasswordResetServiceIntegrationTest {
//
//    @Autowired
//    private PasswordResetService passwordResetService;
//
//    @Autowired
//    private AuthenticationRepository authRepository;
//
//    @Autowired
//    private PasswordResetTokenRepository tokenRepository;
//
//    @Autowired
//    private BCryptPasswordEncoder passwordEncoder;
//
//    @MockBean
//    private JavaMailSender mailSender;
//
//    private static final String TEST_EMAIL = "test@example.com";
//    private static final String TEST_PASSWORD = "TestPassword123";
//    private AuthenticationInfo testUser;
//
//    @BeforeEach
//    void setUp() {
//        // First delete tokens to maintain referential integrity
//        tokenRepository.deleteAllInBatch();
//        // Then delete users
//        authRepository.deleteAllInBatch();
//
//        // Mock email sending to avoid actual SMTP calls
//        doNothing().when(mailSender).send(any(SimpleMailMessage.class));
//
//        // Create test user
//        testUser = AuthenticationInfo.builder()
//                .username(TEST_EMAIL)
//                .password(passwordEncoder.encode(TEST_PASSWORD))
//                .role(Role.USER)
//                .build();
//        testUser = authRepository.save(testUser);
//    }
//
//    @Test
//    @Transactional
//    void resetPassword_ShouldThrowException_WhenTokenExpired() {
//        // Arrange
//        passwordResetService.createPasswordResetTokenForUser(TEST_EMAIL);
//
//        // Get the token and modify its expiry date
//        List<PasswordResetToken> tokens = tokenRepository.findAll();
//        assertEquals(1, tokens.size(), "Should have exactly one token");
//
//        PasswordResetToken token = tokens.get(0);
//        token.setExpiryDate(LocalDateTime.now().minusHours(1));
//        tokenRepository.saveAndFlush(token);
//
//        ResetPasswordRequestDto request = ResetPasswordRequestDto.builder()
//                .token(token.getToken())
//                .newPassword("newPassword123")
//                .build();
//
//        // Act & Assert
//        CustomException exception = assertThrows(CustomException.class,
//                () -> passwordResetService.resetPassword(request));
//
//        assertEquals(ErrorCode.INVALID_TOKEN, exception.getErrorCode());
//
//        // Force a flush and clear of the persistence context
//        tokenRepository.flush();
//
//        // Verify token deletion
//        assertTrue(tokenRepository.findByToken(token.getToken()).isEmpty(),
//                "Token should be deleted after expiration");
//    }
//
//    @Test
//    @Transactional
//    void resetPassword_ShouldHandleNullPassword() {
//        // Arrange
//        passwordResetService.createPasswordResetTokenForUser(TEST_EMAIL);
//        PasswordResetToken token = tokenRepository.findAll().get(0);
//
//        ResetPasswordRequestDto request = ResetPasswordRequestDto.builder()
//                .token(token.getToken())
//                .newPassword(null)
//                .build();
//
//        // Act & Assert
//        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
//                () -> passwordResetService.resetPassword(request),
//                "Should throw IllegalArgumentException for null password");
//
//        assertEquals("rawPassword cannot be null", exception.getMessage());
//    }
//
//    @Test
//    @Transactional
//    void resetPassword_ShouldSucceed_WithValidToken() {
//        // Arrange
//        passwordResetService.createPasswordResetTokenForUser(TEST_EMAIL);
//        PasswordResetToken token = tokenRepository.findAll().get(0);
//        String newPassword = "newValidPassword123";
//
//        ResetPasswordRequestDto request = ResetPasswordRequestDto.builder()
//                .token(token.getToken())
//                .newPassword(newPassword)
//                .build();
//
//        // Act
//        ResetPasswordResponseDto response = passwordResetService.resetPassword(request);
//
//        // Assert
//        assertTrue(response.isSuccess(), "Password reset should be successful");
//
//        // Force a flush and clear
//        tokenRepository.flush();
//
//        assertTrue(tokenRepository.findByToken(token.getToken()).isEmpty(),
//                "Token should be deleted after successful reset");
//
//        AuthenticationInfo updatedUser = authRepository.findById(testUser.getUserId()).orElseThrow();
//        assertTrue(passwordEncoder.matches(newPassword, updatedUser.getPassword()),
//                "Password should be updated and correctly encoded");
//    }
//
//    @Test
//    @Transactional
//    void createPasswordResetToken_ShouldGenerateNewToken() {
//        // Act
//        passwordResetService.createPasswordResetTokenForUser(TEST_EMAIL);
//
//        // Assert
//        List<PasswordResetToken> tokens = tokenRepository.findAll();
//        assertEquals(1, tokens.size(), "Should create exactly one token");
//
//        PasswordResetToken token = tokens.get(0);
//        assertNotNull(token.getToken(), "Token should not be null");
//        assertEquals(testUser.getUserId(), token.getUser().getUserId(),
//                "Token should be associated with correct user");
//        assertTrue(token.getExpiryDate().isAfter(LocalDateTime.now()),
//                "Token should have future expiry date");
//    }
//
//    @Test
//    @Transactional
//    void createPasswordResetToken_ShouldHandleNonExistentEmail() {
//        // Act & Assert
//        assertDoesNotThrow(() -> passwordResetService.createPasswordResetTokenForUser("nonexistent@example.com"),
//                "Should not throw exception for non-existent email");
//        assertEquals(0, tokenRepository.count(), "Should not create token for non-existent user");
//    }
//}
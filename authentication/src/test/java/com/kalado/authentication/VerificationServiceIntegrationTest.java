//package com.kalado.authentication;
//
//import com.kalado.authentication.application.service.EmailService;
//import com.kalado.authentication.application.service.VerificationService;
//import com.kalado.authentication.domain.model.AuthenticationInfo;
//import com.kalado.authentication.domain.model.VerificationToken;
//import com.kalado.authentication.infrastructure.repository.AuthenticationRepository;
//import com.kalado.authentication.infrastructure.repository.VerificationTokenRepository;
//import com.kalado.common.enums.Role;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.mail.SimpleMailMessage;
//import org.springframework.mail.javamail.JavaMailSender;
//
//import javax.persistence.EntityManager;
//import javax.persistence.PersistenceContext;
//import java.time.LocalDateTime;
//import java.util.Optional;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.ArgumentMatchers.*;
//import static org.mockito.Mockito.doNothing;
//import static org.mockito.Mockito.verify;
//
//@SpringBootTest
//class VerificationServiceIntegrationTest extends BaseIntegrationTest{
//    @PersistenceContext
//    private EntityManager entityManager;
//
//    @Autowired
//    private VerificationService verificationService;
//
//    @Autowired
//    private AuthenticationRepository authRepository;
//
//    @Autowired
//    private VerificationTokenRepository tokenRepository;
//
//    @MockBean
//    private JavaMailSender mailSender;
//
//    @MockBean
//    private EmailService emailService;
//
//    private static final String TEST_EMAIL = "test@example.com";
//    private AuthenticationInfo testUser;
//
//    @BeforeEach
//    void setUp() {
//        clearDatabase(entityManager);
//        // Clean up repositories
//        tokenRepository.deleteAll();
//        authRepository.deleteAll();
//
//        // Create test user
//        testUser = AuthenticationInfo.builder()
//                .username(TEST_EMAIL)
//                .password("password")
//                .role(Role.USER)
//                .build();
//        testUser = authRepository.save(testUser);
//        doNothing().when(mailSender).send(any(SimpleMailMessage.class));
//    }
//
//    @Test
//    void isEmailVerified_ShouldReturnTrue_WhenTokenVerified() {
//        // First create and verify a token
//        verificationService.createVerificationToken(testUser);
//        String token = tokenRepository.findByUser_UserId(testUser.getUserId()).get().getToken();
//        verificationService.verifyEmail(token);
//
//        // Act
//        boolean result = verificationService.isEmailVerified(testUser);
//
//        // Assert
//        assertTrue(result);
//    }
//
//    @Test
//    void isEmailVerified_ShouldReturnFalse_WhenTokenUnverified() {
//        // Create token but don't verify it
//        verificationService.createVerificationToken(testUser);
//
//        // Act
//        boolean result = verificationService.isEmailVerified(testUser);
//
//        // Assert
//        assertFalse(result);
//    }
//
//    @Test
//    void isEmailVerified_ShouldReturnFalse_WhenNoToken() {
//        // Act
//        boolean result = verificationService.isEmailVerified(testUser);
//
//        // Assert
//        assertFalse(result);
//    }
//
//    @Test
//    void resendVerificationToken_ShouldCreateNewToken() {
//        // First create a token
//        verificationService.createVerificationToken(testUser);
//        String originalToken = tokenRepository.findByUser_UserId(testUser.getUserId()).get().getToken();
//
//        // Act - Resend the token
//        verificationService.resendVerificationToken(testUser);
//
//        // Assert
//        String newToken = tokenRepository.findByUser_UserId(testUser.getUserId()).get().getToken();
//        assertNotEquals(originalToken, newToken);
//        verify(emailService).sendVerificationToken(eq(TEST_EMAIL), eq(newToken));
//    }
//
//    void createVerificationToken_ShouldCreateTokenAndSendEmail() {
//        // Act
//        verificationService.createVerificationToken(testUser);
//
//        // Assert
//        Optional<VerificationToken> token = tokenRepository.findByUser_UserId(testUser.getUserId());
//
//        assertTrue(token.isPresent());
//        assertNotNull(token.get().getToken());
//        assertEquals(6, token.get().getToken().length());
//        assertTrue(token.get().getToken().matches("\\d{6}"));
//        assertFalse(token.get().isVerified());
//        assertTrue(token.get().getExpiryDate().isAfter(LocalDateTime.now()));
//
//        verify(emailService).sendVerificationToken(eq(TEST_EMAIL), anyString());
//    }
//
//    @Test
//    void createVerificationToken_ShouldReplaceExistingToken() {
//        // Act - Create first token
//        verificationService.createVerificationToken(testUser);
//        String firstToken = tokenRepository.findByUser_UserId(testUser.getUserId()).get().getToken();
//
//        // Act - Create second token
//        verificationService.createVerificationToken(testUser);
//        String secondToken = tokenRepository.findByUser_UserId(testUser.getUserId()).get().getToken();
//
//        // Assert
//        assertNotEquals(firstToken, secondToken);
//        assertEquals(1, tokenRepository.findAll().size());
//    }
//
//    @Test
//    void verifyEmail_ShouldReturnTrue_WhenTokenValid() {
//        // Arrange
//        verificationService.createVerificationToken(testUser);
//        String token = tokenRepository.findByUser_UserId(testUser.getUserId()).get().getToken();
//
//        // Act
//        boolean result = verificationService.verifyEmail(token);
//
//        // Assert
//        assertTrue(result);
//        assertTrue(tokenRepository.findByToken(token).get().isVerified());
//    }
//
//    @Test
//    void verifyEmail_ShouldReturnFalse_WhenTokenExpired() {
//        // Arrange
//        verificationService.createVerificationToken(testUser);
//        VerificationToken token = tokenRepository.findByUser_UserId(testUser.getUserId()).get();
//        token.setExpiryDate(LocalDateTime.now().minusHours(1));
//        tokenRepository.save(token);
//
//        // Act
//        boolean result = verificationService.verifyEmail(token.getToken());
//
//        // Assert
//        assertFalse(result);
//        assertFalse(token.isVerified());
//    }
//
//    @Test
//    void verifyEmail_ShouldReturnFalse_WhenTokenInvalid() {
//        // Act
//        boolean result = verificationService.verifyEmail("invalid-token");
//
//        // Assert
//        assertFalse(result);
//    }
//}
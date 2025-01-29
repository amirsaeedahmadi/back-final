package com.kalado.authentication;

import com.kalado.authentication.application.service.EmailService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailService emailService;

    @Captor
    private ArgumentCaptor<SimpleMailMessage> emailCaptor;

    private static final String TEST_EMAIL = "test@example.com";
    private static final String FROM_EMAIL = "noreply@kalado.com";

    @Test
    void sendVerificationToken_ShouldSendCorrectEmail() {
        String token = "123456";

        emailService.sendVerificationToken(TEST_EMAIL, token);

        verify(mailSender).send(emailCaptor.capture());
        SimpleMailMessage capturedEmail = emailCaptor.getValue();

        assertEquals(FROM_EMAIL, capturedEmail.getFrom());
        assertEquals(TEST_EMAIL, capturedEmail.getTo()[0]);
        assertEquals("Your Email Verification Code", capturedEmail.getSubject());
        assertTrue(capturedEmail.getText().contains(token));
        assertTrue(capturedEmail.getText().contains("24 hours"));
    }

    @Test
    void sendPasswordResetToken_ShouldSendCorrectEmail() {
        String token = "reset-token-123";

        emailService.sendPasswordResetToken(TEST_EMAIL, token);

        verify(mailSender).send(emailCaptor.capture());
        SimpleMailMessage capturedEmail = emailCaptor.getValue();

        assertEquals(FROM_EMAIL, capturedEmail.getFrom());
        assertEquals(TEST_EMAIL, capturedEmail.getTo()[0]);
        assertEquals("Password Reset Request", capturedEmail.getSubject());
        assertTrue(capturedEmail.getText().contains(token));
        assertTrue(capturedEmail.getText().contains("24 hours"));
        assertTrue(capturedEmail.getText().contains("If you did not request this password reset"));
    }

    @Test
    void sendVerificationToken_ShouldHandleNullToken() {
        assertDoesNotThrow(() -> emailService.sendVerificationToken(TEST_EMAIL, null));

        verify(mailSender).send(emailCaptor.capture());
        SimpleMailMessage capturedEmail = emailCaptor.getValue();
        assertTrue(capturedEmail.getText().contains("null"));
    }

    @Test
    void sendPasswordResetToken_ShouldHandleNullToken() {
        assertDoesNotThrow(() -> emailService.sendPasswordResetToken(TEST_EMAIL, null));

        verify(mailSender).send(emailCaptor.capture());
        SimpleMailMessage capturedEmail = emailCaptor.getValue();
        assertTrue(capturedEmail.getText().contains("null"));
    }
}
package com.kalado.authentication.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender mailSender;
    private static final String FROM_EMAIL = "noreply@kalado.com";

    public void sendVerificationToken(String to, String token) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(FROM_EMAIL);
        message.setTo(to);
        message.setSubject("Your Email Verification Code");
        message.setText("Your verification code is: " + token + "\n\n" +
                "Please enter this code in the verification page to complete your registration.\n" +
                "This code will expire in 24 hours.");

        mailSender.send(message);
    }

    public void sendPasswordResetToken(String to, String token) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(FROM_EMAIL);
        message.setTo(to);
        message.setSubject("Password Reset Request");
        message.setText("You have requested to reset your password.\n\n" +
                "Please use the following token to reset your password: " + token + "\n\n" +
                "If you did not request this password reset, please ignore this email.\n" +
                "This token will expire in 24 hours.");

        mailSender.send(message);
    }
}
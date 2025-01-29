package com.kalado.authentication;

import com.kalado.authentication.application.service.EmailService;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

@TestConfiguration
public class TestConfig {

    @Bean
    @Primary
    public JavaMailSender javaMailSender() {
        return new JavaMailSenderImpl();
    }

    @Bean
    @Primary
    public EmailService emailService(JavaMailSender javaMailSender) {
        return new EmailService(javaMailSender) {
            @Override
            public void sendVerificationToken(String to, String token) {
            }

            @Override
            public void sendPasswordResetToken(String to, String token) {
            }
        };
    }
}
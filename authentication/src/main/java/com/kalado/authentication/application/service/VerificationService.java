package com.kalado.authentication.application.service;

import com.kalado.authentication.domain.model.AuthenticationInfo;
import com.kalado.authentication.domain.model.VerificationToken;
import com.kalado.authentication.infrastructure.repository.VerificationTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.security.SecureRandom;

@Service
@RequiredArgsConstructor
public class VerificationService {
    private final VerificationTokenRepository tokenRepository;
    private final EmailService emailService;
    private static final int EXPIRATION_HOURS = 24;
    private static final int TOKEN_LENGTH = 6;

    private String generateToken() {
        SecureRandom random = new SecureRandom();
        StringBuilder token = new StringBuilder();
        for (int i = 0; i < TOKEN_LENGTH; i++) {
            token.append(random.nextInt(10));
        }
        return token.toString();
    }

    public void createVerificationToken(AuthenticationInfo user) {
        tokenRepository.findByUser_UserId(user.getUserId())
                .ifPresent(tokenRepository::delete);

        String token = generateToken();
        VerificationToken verificationToken = VerificationToken.builder()
                .token(token)
                .user(user)
                .expiryDate(LocalDateTime.now().plusHours(EXPIRATION_HOURS))
                .verified(false)
                .build();

        tokenRepository.save(verificationToken);
        emailService.sendVerificationToken(user.getUsername(), token);
    }

    public boolean verifyEmail(String token) {
        return tokenRepository.findByToken(token)
                .filter(verificationToken -> !verificationToken.isExpired())
                .map(verificationToken -> {
                    verificationToken.setVerified(true);
                    tokenRepository.save(verificationToken);
                    return true;
                })
                .orElse(false);
    }

    public boolean isEmailVerified(AuthenticationInfo user) {
        return tokenRepository.findByUser_UserId(user.getUserId())
                .map(VerificationToken::isVerified)
                .orElse(false);
    }

    public void resendVerificationToken(AuthenticationInfo user) {
        createVerificationToken(user);
    }
}
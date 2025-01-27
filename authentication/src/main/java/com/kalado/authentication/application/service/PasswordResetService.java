package com.kalado.authentication.application.service;

import com.kalado.authentication.domain.model.AuthenticationInfo;
import com.kalado.authentication.domain.model.PasswordResetToken;
import com.kalado.authentication.infrastructure.repository.AuthenticationRepository;
import com.kalado.authentication.infrastructure.repository.PasswordResetTokenRepository;
import com.kalado.common.dto.ResetPasswordRequestDto;
import com.kalado.common.dto.ResetPasswordResponseDto;
import com.kalado.common.enums.ErrorCode;
import com.kalado.common.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordResetService {
    private final AuthenticationRepository authRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final EmailService emailService;
    private final BCryptPasswordEncoder passwordEncoder;

    private static final int EXPIRATION_HOURS = 24;

    @Transactional
    public void createPasswordResetTokenForUser(String email) {
        AuthenticationInfo user = authRepository.findByUsername(email);
        if (user == null) {
            // For security reasons, we still return success even if the email doesn't exist
            log.warn("Password reset requested for non-existent user: {}", email);
            return;
        }

        String token = generateToken();

        // Delete any existing tokens for this user
        tokenRepository.findAll().stream()
                .filter(t -> t.getUser().getUserId() == user.getUserId())
                .forEach(tokenRepository::delete);

        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token(token)
                .user(user)
                .expiryDate(LocalDateTime.now().plusHours(EXPIRATION_HOURS))
                .build();

        tokenRepository.save(resetToken);
        emailService.sendPasswordResetToken(email, token);

        log.info("Password reset token created for user: {}", email);
    }

    @Transactional
    public ResetPasswordResponseDto resetPassword(ResetPasswordRequestDto request) {
        PasswordResetToken resetToken = tokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_TOKEN, "Invalid or expired token"));

        if (resetToken.isExpired()) {
            tokenRepository.delete(resetToken);
            throw new CustomException(ErrorCode.INVALID_TOKEN, "Token has expired");
        }

        AuthenticationInfo user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        authRepository.save(user);

        tokenRepository.delete(resetToken);

        log.info("Password successfully reset for user ID: {}", user.getUserId());

        return ResetPasswordResponseDto.builder()
                .success(true)
                .message("Password has been reset successfully")
                .build();
    }

    private String generateToken() {
        return UUID.randomUUID().toString();
    }
}
package com.kalado.authentication.domain.model;

import lombok.*;
import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "verification_tokens")
public class VerificationToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String token;

    @OneToOne(targetEntity = AuthenticationInfo.class, fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private AuthenticationInfo user;

    private LocalDateTime expiryDate;

    @Column(name = "verified")
    private boolean verified;

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expiryDate);
    }
}
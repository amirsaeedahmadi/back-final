package com.kalado.authentication.infrastructure.repository;

import com.kalado.authentication.domain.model.AuthenticationInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuthenticationRepository extends JpaRepository<AuthenticationInfo, Long> {
  AuthenticationInfo findByUsername(String username);
}

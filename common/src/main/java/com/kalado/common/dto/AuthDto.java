package com.kalado.common.dto;

import com.kalado.common.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthDto {
  private boolean isValid;
  private long userId;
  private Role role;
}

package com.kalado.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserDto {
  private Long id;
  private String username;
  private String firstName;
  private String lastName;
  private String address;
  private String phoneNumber;
  private String profileImageUrl;
  private boolean blocked;
}
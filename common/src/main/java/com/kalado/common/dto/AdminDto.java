package com.kalado.common.dto;

import com.kalado.common.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AdminDto {
  private Long id;
  private String firstName;
  private String lastName;
  private String phoneNumber;
  private String address;
  private Role role;
  private List<String> permissions;
}
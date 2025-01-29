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
public class RoleUpdateResponseDto {
    private boolean success;
    private String message;
    private Long userId;
    private Role oldRole;
    private Role newRole;
    private Long updatedBy;
    private String timestamp;
}
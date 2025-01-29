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
public class RoleUpdateRequestDto {
    private Long userId;
    private Role newRole;
    private String reason;
}

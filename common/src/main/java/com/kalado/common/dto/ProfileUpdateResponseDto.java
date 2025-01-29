package com.kalado.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProfileUpdateResponseDto {
    private boolean success;
    private String message;
    private UserDto updatedProfile;
    private boolean passwordChanged;
}
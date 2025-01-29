package com.kalado.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class UserProfileUpdateDto {
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String address;
    private Long id;

    private String currentPassword;
    private String newPassword;
    private String confirmPassword;
}
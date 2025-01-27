package com.kalado.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserProfileUpdateDto {
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String address;
    private Long id;
}
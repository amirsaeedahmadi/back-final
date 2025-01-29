package com.kalado.gateway.adapters;

import com.kalado.common.enums.Role;
import com.kalado.common.feign.authentication.AuthenticationApi;
import com.kalado.gateway.annotation.Authentication;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/roles")
@RequiredArgsConstructor
public class RoleManagementController {
    private final AuthenticationApi authenticationApi;

    @PutMapping("/update/{targetUserId}")
    @Authentication(userId = "#userId")
    public void updateUserRole(
                                                  Long userId,
                                                  @PathVariable Long targetUserId,
                                                  @RequestParam Role newRole) {
        authenticationApi.updateUserRole(targetUserId, newRole, userId);
    }
}
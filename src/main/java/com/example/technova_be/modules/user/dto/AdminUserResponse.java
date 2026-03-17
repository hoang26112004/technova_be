package com.example.technova_be.modules.user.dto;

import com.example.technova_be.comom.constants.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;
import java.util.List;

@Getter
@AllArgsConstructor
public class AdminUserResponse {
    private Long id;
    private String email;
    private String fullName;
    private String phoneNumber;
    private UserStatus status;
    private List<String> roles;
    private Instant createdAt;
    private Instant updatedAt;
}

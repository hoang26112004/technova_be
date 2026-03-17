package com.example.technova_be.modules.user.controller;

import com.example.technova_be.comom.constants.UserStatus;
import com.example.technova_be.modules.user.dto.AdminResetPasswordRequest;
import com.example.technova_be.modules.user.dto.AdminUserResponse;
import com.example.technova_be.modules.user.dto.UpdateUserStatusRequest;
import com.example.technova_be.modules.user.entity.User;
import com.example.technova_be.modules.user.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/users")
public class UserAdminController {
    private final UserService userService;

    public UserAdminController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/create-admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdminUserResponse> createAdmin(@Valid @RequestBody CreateAdminRequest request) {
        return ResponseEntity.ok(
            userService.createAdmin(request.getEmail(), request.getPassword(), request.getFullName())
        );
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<AdminUserResponse>> listUsers(
        @RequestParam(name = "query", required = false) String query,
        @RequestParam(name = "status", required = false) UserStatus status,
        @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(userService.getAllUsers(query, status, pageable));
    }

    @GetMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdminUserResponse> getUser(@PathVariable Long userId) {
        return ResponseEntity.ok(userService.getUserById(userId));
    }

    @PatchMapping("/{userId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdminUserResponse> updateStatus(
        @PathVariable Long userId,
        @Valid @RequestBody UpdateUserStatusRequest request
    ) {
        return ResponseEntity.ok(userService.updateUserStatus(userId, request));
    }

    @PostMapping("/{userId}/reset-password")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdminUserResponse> resetPassword(
        @PathVariable Long userId,
        @Valid @RequestBody AdminResetPasswordRequest request
    ) {
        return ResponseEntity.ok(userService.resetPassword(userId, request));
    }

    @Getter
    @Setter
    public static class CreateAdminRequest {
        @Email
        @NotBlank
        private String email;
        @NotBlank
        private String password;
        @NotBlank
        private String fullName;
    }
}

package com.example.bookingapp.controller;

import com.example.bookingapp.dto.user.RoleUpdateRequestDto;
import com.example.bookingapp.dto.user.UserResponseDto;
import com.example.bookingapp.dto.user.UserResponseDtoWithRoles;
import com.example.bookingapp.dto.user.UserUpdateRequestDto;
import com.example.bookingapp.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping(value = "/{id}/role")
    @Operation(summary = "Update a role", description = "Update a role of user by user id")
    @ResponseStatus(HttpStatus.OK)
    public UserResponseDtoWithRoles updateRoleById(@PathVariable Long id,
                                                   @RequestBody @Valid RoleUpdateRequestDto
                                                           requestDto) {
        return userService.updateRoleById(id, requestDto);
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping(value = "/me")
    @Operation(summary = "Get a profile", description = "Get a current user's profile")
    @ResponseStatus(HttpStatus.OK)
    public UserResponseDto getCurrentUserProfile() {
        return userService.getProfile();
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @PutMapping(value = "/me")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Update a profile", description = "Update a current user's profile")
    public UserResponseDto updateCurrentUserProfile(@RequestBody UserUpdateRequestDto requestDto) {
        return userService.updateProfile(requestDto);
    }
}

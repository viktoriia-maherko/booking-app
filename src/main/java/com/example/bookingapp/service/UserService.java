package com.example.bookingapp.service;

import com.example.bookingapp.dto.user.RoleUpdateRequestDto;
import com.example.bookingapp.dto.user.UserRegistrationRequestDto;
import com.example.bookingapp.dto.user.UserResponseDto;
import com.example.bookingapp.dto.user.UserUpdateRequestDto;
import com.example.bookingapp.exception.RegistrationException;
import com.example.bookingapp.model.User;

public interface UserService {
    UserResponseDto register(UserRegistrationRequestDto request) throws RegistrationException;

    User findByEmail(String email);

    UserResponseDto updateRoleById(Long id, RoleUpdateRequestDto requestDto);

    UserResponseDto getProfile();

    UserResponseDto updateProfile(UserUpdateRequestDto requestDto);

    boolean existsById(Long id);

    User getAuthenticatedUserIfExists();
}

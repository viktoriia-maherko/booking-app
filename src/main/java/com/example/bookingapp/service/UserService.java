package com.example.bookingapp.service;

import com.example.bookingapp.dto.user.UserRegistrationRequestDto;
import com.example.bookingapp.dto.user.UserResponseDto;
import com.example.bookingapp.exception.RegistrationException;
import com.example.bookingapp.model.User;

public interface UserService {
    UserResponseDto register(UserRegistrationRequestDto request) throws RegistrationException;

    User findByEmail(String email);

}

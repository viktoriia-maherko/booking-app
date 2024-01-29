package com.example.bookingapp.controller;

import com.example.bookingapp.dto.user.UserLoginRequestDto;
import com.example.bookingapp.dto.user.UserLoginResponseDto;
import com.example.bookingapp.dto.user.UserRegistrationRequestDto;
import com.example.bookingapp.dto.user.UserResponseDto;
import com.example.bookingapp.exception.RegistrationException;
import com.example.bookingapp.security.AuthenticationService;
import com.example.bookingapp.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/auth")
public class AuthenticationController {
    private final UserService userService;
    private final AuthenticationService authenticationService;

    @PostMapping("/login")
    public UserLoginResponseDto login(@RequestBody @Valid UserLoginRequestDto requestDto) {
        return authenticationService.authenticate(requestDto);
    }

    @PostMapping("/register")
    public UserResponseDto register(@RequestBody @Valid UserRegistrationRequestDto request)
            throws RegistrationException {
        return userService.register(request);
    }
}

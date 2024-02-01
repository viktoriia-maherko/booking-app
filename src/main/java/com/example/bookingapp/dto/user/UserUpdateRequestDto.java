package com.example.bookingapp.dto.user;

import lombok.Data;

@Data
public class UserUpdateRequestDto {
    private String email;
    private String firstName;
    private String lastName;
    private String password;
}

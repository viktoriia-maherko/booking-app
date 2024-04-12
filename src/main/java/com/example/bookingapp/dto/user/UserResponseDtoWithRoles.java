package com.example.bookingapp.dto.user;

import java.util.Set;
import lombok.Data;

@Data
public class UserResponseDtoWithRoles {
    private String email;
    private String firstName;
    private String lastName;
    private Set<String> roleNames;
}

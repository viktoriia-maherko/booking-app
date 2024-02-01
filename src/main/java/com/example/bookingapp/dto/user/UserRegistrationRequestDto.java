package com.example.bookingapp.dto.user;

import com.example.bookingapp.validation.FieldsValueMatch;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@FieldsValueMatch(field = "password", fieldMatch = "repeatPassword",
        message = "Password and repeatPassword must be the same")
public class UserRegistrationRequestDto {
    @NotBlank
    @Size(min = 4, max = 50)
    private String email;

    @NotBlank
    @Size(min = 6, max = 100)
    private String password;

    private String repeatPassword;

    @NotBlank
    @Size(max = 50)
    private String firstName;
    @NotBlank
    @Size(max = 50)
    private String lastName;
}

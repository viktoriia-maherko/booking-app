package com.example.bookingapp.dto.user;

import jakarta.validation.constraints.NotNull;
import java.util.Set;
import lombok.Data;

@Data
public class RoleUpdateRequestDto {
    @NotNull
    private Set<String> roleNames;
}

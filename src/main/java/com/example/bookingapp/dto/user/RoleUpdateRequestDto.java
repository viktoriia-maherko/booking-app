package com.example.bookingapp.dto.user;

import com.example.bookingapp.model.Role;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RoleUpdateRequestDto {
    @NotNull
    private Role.RoleName roleNames;
}

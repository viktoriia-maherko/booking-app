package com.example.bookingapp.mapper;

import com.example.bookingapp.config.MapperConfig;
import com.example.bookingapp.dto.user.UserResponseDto;
import com.example.bookingapp.model.Role;
import com.example.bookingapp.model.User;
import java.util.Set;
import java.util.stream.Collectors;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(config = MapperConfig.class)
public interface UserMapper {
    @Mapping(source = "roles", target = "roleNames", qualifiedByName = "mapRolesToRoleNames")
    UserResponseDto toDto(User user);

    @Named("mapRolesToRoleNames")
    default Set<Role.RoleName> mapRolesToRoleNames(Set<Role> roles) {
        return roles.stream()
                .map(Role::getRoleName)
                .collect(Collectors.toSet());
    }
}

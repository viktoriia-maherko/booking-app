package com.example.bookingapp.mapper;

import com.example.bookingapp.config.MapperConfig;
import com.example.bookingapp.dto.user.UserResponseDto;
import com.example.bookingapp.model.User;
import org.mapstruct.Mapper;

@Mapper(config = MapperConfig.class)
public interface UserMapper {
    UserResponseDto toUserResponse(User user);
}

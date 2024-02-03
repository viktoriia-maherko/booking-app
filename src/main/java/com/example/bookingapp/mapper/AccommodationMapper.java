package com.example.bookingapp.mapper;

import com.example.bookingapp.config.MapperConfig;
import com.example.bookingapp.dto.accommodation.AccommodationResponseDto;
import com.example.bookingapp.dto.accommodation.CreateAccommodationRequestDto;
import com.example.bookingapp.model.Accommodation;
import org.mapstruct.Mapper;

@Mapper(config = MapperConfig.class)
public interface AccommodationMapper {
    AccommodationResponseDto toDto(Accommodation accommodation);

    Accommodation toModel(CreateAccommodationRequestDto requestDto);
}

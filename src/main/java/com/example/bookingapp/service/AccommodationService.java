package com.example.bookingapp.service;

import com.example.bookingapp.dto.accommodation.AccommodationResponseDto;
import com.example.bookingapp.dto.accommodation.AccommodationUpdateRequestDto;
import com.example.bookingapp.dto.accommodation.CreateAccommodationRequestDto;
import java.util.List;

public interface AccommodationService {
    AccommodationResponseDto save(CreateAccommodationRequestDto requestDto);

    List<AccommodationResponseDto> getAll();

    AccommodationResponseDto getById(Long id);

    AccommodationResponseDto updateById(Long id, AccommodationUpdateRequestDto requestDto);

    void deleteById(Long id);
}

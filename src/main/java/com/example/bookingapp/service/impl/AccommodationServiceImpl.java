package com.example.bookingapp.service.impl;

import com.example.bookingapp.dto.accommodation.AccommodationResponseDto;
import com.example.bookingapp.dto.accommodation.AccommodationUpdateRequestDto;
import com.example.bookingapp.dto.accommodation.CreateAccommodationRequestDto;
import com.example.bookingapp.exception.EntityNotFoundException;
import com.example.bookingapp.mapper.AccommodationMapper;
import com.example.bookingapp.model.Accommodation;
import com.example.bookingapp.model.Address;
import com.example.bookingapp.repository.AccommodationRepository;
import com.example.bookingapp.repository.AddressRepository;
import com.example.bookingapp.service.AccommodationService;
import com.example.bookingapp.service.NotificationService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AccommodationServiceImpl implements AccommodationService {
    private final AccommodationRepository accommodationRepository;
    private final AddressRepository addressRepository;
    private final AccommodationMapper accommodationMapper;
    private final NotificationService notificationService;

    @Override
    public AccommodationResponseDto save(CreateAccommodationRequestDto requestDto) {
        Accommodation accommodation = accommodationMapper.toModel(requestDto);
        Address savedAddress = addressRepository.save(requestDto.getAddress());
        accommodation.setAddress(savedAddress);
        Accommodation savedAccommodation = accommodationRepository.save(accommodation);
        AccommodationResponseDto responseDto = accommodationMapper.toDto(savedAccommodation);
        notificationService.notifyCreationNewAccommodation(responseDto);
        return responseDto;
    }

    @Override
    public List<AccommodationResponseDto> getAll(Pageable pageable) {
        return accommodationRepository.findAll(pageable)
                .stream()
                .filter(accommodation -> accommodation.getAvailability() > 0)
                .map(accommodationMapper::toDto)
                .toList();
    }

    @Override
    public AccommodationResponseDto getById(Long id) {
        Accommodation accommodation = accommodationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Can't find an accommodation by id " + id
                ));
        return accommodationMapper.toDto(accommodation);
    }

    @Override
    public AccommodationResponseDto updateById(Long id, AccommodationUpdateRequestDto requestDto) {
        Accommodation accommodation = accommodationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Can't find an accommodation by id " + id
                ));
        accommodation.setAvailability(requestDto.getAvailability());
        accommodation.setAmenities(requestDto.getAmenities());
        accommodation.setDailyRate(requestDto.getDailyRate());
        Accommodation updatedAccommodation = accommodationRepository.save(accommodation);
        return accommodationMapper.toDto(updatedAccommodation);
    }

    @Override
    public void deleteById(Long id) {
        accommodationRepository.deleteById(id);
        notificationService.notifyCancellationAccommodation(id);
    }
}

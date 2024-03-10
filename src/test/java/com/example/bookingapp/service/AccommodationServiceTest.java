package com.example.bookingapp.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.example.bookingapp.dto.accommodation.AccommodationResponseDto;
import com.example.bookingapp.dto.accommodation.AccommodationUpdateRequestDto;
import com.example.bookingapp.dto.accommodation.CreateAccommodationRequestDto;
import com.example.bookingapp.exception.EntityNotFoundException;
import com.example.bookingapp.mapper.AccommodationMapper;
import com.example.bookingapp.model.Accommodation;
import com.example.bookingapp.model.Address;
import com.example.bookingapp.repository.AccommodationRepository;
import com.example.bookingapp.repository.AddressRepository;
import com.example.bookingapp.service.impl.AccommodationServiceImpl;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class AccommodationServiceTest {
    @Mock
    private AccommodationRepository accommodationRepository;
    @Mock
    private AccommodationMapper accommodationMapper;
    @Mock
    private AddressRepository addressRepository;
    @Mock
    private NotificationService notificationService;
    @InjectMocks
    private AccommodationServiceImpl accommodationService;

    @Test
    @DisplayName("Verify saving accommodation to DB")
    void save_ValidCreateAccommodationRequestDto_AccommodationResponseDto() {
        CreateAccommodationRequestDto createAccommodationRequestDto
                = createAccommodationRequestDto();
        Accommodation accommodation = createAccommodation();
        AccommodationResponseDto expected = createAccommodationResponseDto();
        Address address = createAddress();

        when(accommodationMapper.toModel(createAccommodationRequestDto)).thenReturn(accommodation);
        when(addressRepository.save(address)).thenReturn(address);
        when(accommodationRepository.save(accommodation)).thenReturn(accommodation);
        when(accommodationMapper.toDto(accommodation)).thenReturn(expected);
        doAnswer(invocation -> {
            invocation.getArgument(0);
            return null;
        }).when(notificationService)
                .notifyCreationNewAccommodation(any(AccommodationResponseDto.class));

        AccommodationResponseDto actual = accommodationService.save(createAccommodationRequestDto);

        notificationService.notifyCreationNewAccommodation(expected);
        notificationService.notifyCreationNewAccommodation(actual);

        assertEquals(expected, actual);
        assertNotNull(actual);
        verifyNoMoreInteractions(accommodationRepository, accommodationMapper, notificationService);
    }

    @Test
    @DisplayName("Verify the correct list of accommodationsDto was returned")
    void getAll_ValidPageable_ReturnsAllAccommodations() {
        Accommodation accommodation1 = createAccommodation();
        Accommodation accommodation2 = createAccommodation();
        accommodation2.setSize("large");

        AccommodationResponseDto accommodationDto1 = createAccommodationResponseDto();
        AccommodationResponseDto accommodationDto2 = createAccommodationResponseDto();
        accommodationDto2.setSize("large");

        Pageable pageable = PageRequest.of(0, 10);
        List<Accommodation> accommodations = List.of(accommodation1, accommodation2);
        Page<Accommodation> accommodationPage = new PageImpl<>(accommodations, pageable,
                accommodations.size());

        when(accommodationRepository.findAll(pageable)).thenReturn(accommodationPage);
        when(accommodationMapper.toDto(accommodation1)).thenReturn(accommodationDto1);
        when(accommodationMapper.toDto(accommodation2)).thenReturn(accommodationDto2);

        List<AccommodationResponseDto> expected = List.of(accommodationDto1, accommodationDto2);
        List<AccommodationResponseDto> actual = accommodationService.getAll(pageable);

        assertEquals(expected.size(), actual.size());
        assertNotNull(actual);
        verifyNoMoreInteractions(accommodationRepository, accommodationMapper);
    }

    @Test
    @DisplayName("Verify the correct accommodationDto was returned by valid accommodationId")
    void getById_WithValidAccommodationId_ShouldReturnValidAccommodationDto() {
        Accommodation accommodation = createAccommodation();
        AccommodationResponseDto expected = createAccommodationResponseDto();

        when(accommodationRepository.findById(anyLong())).thenReturn(Optional.of(accommodation));
        when(accommodationMapper.toDto(accommodation)).thenReturn(expected);

        AccommodationResponseDto actual = accommodationService.getById(accommodation.getId());
        assertEquals(expected, actual);
        assertNotNull(actual);
        verifyNoMoreInteractions(accommodationRepository, accommodationMapper);
    }

    @Test
    @DisplayName("Verify the exception was thrown when accommodationId was incorrect")
    void getById_WithInvalidAccommodationId_ShouldThrowEntityException() {
        Long accommodationId = -10L;
        when(accommodationRepository.findById(accommodationId)).thenReturn(Optional.empty());

        Exception exception = assertThrows(
                EntityNotFoundException.class,
                () -> accommodationService.getById(accommodationId)
        );

        String expected = "Can't find an accommodation by id " + accommodationId;
        String actual = exception.getMessage();
        assertEquals(expected, actual);
        verifyNoMoreInteractions(accommodationRepository);
    }

    @Test
    @DisplayName("Verify the accommodationDto was updated correctly "
            + "by valid accommodationId and accommodationUpdateRequestDto")
    void updateById_WithValidAccommodationId_ReturnsAccommodationDto() {
        AccommodationUpdateRequestDto accommodationRequestDto = new AccommodationUpdateRequestDto();
        accommodationRequestDto.setAmenities(List.of("pool"));
        accommodationRequestDto.setAvailability(1);
        accommodationRequestDto.setDailyRate(BigDecimal.valueOf(150));
        AccommodationResponseDto expected = createAccommodationResponseDto();
        expected.setAmenities(List.of("pool"));
        expected.setAvailability(1);
        expected.setDailyRate(BigDecimal.valueOf(150));
        Accommodation accommodation = createAccommodation();

        when(accommodationRepository.findById(accommodation.getId()))
                .thenReturn(Optional.of(accommodation));
        when(accommodationRepository.save(accommodation)).thenReturn(accommodation);
        when(accommodationMapper.toDto(accommodation)).thenReturn(expected);

        AccommodationResponseDto actual = accommodationService.updateById(accommodation.getId(),
                accommodationRequestDto);

        assertEquals(expected, actual);
        assertNotNull(actual);
        verifyNoMoreInteractions(accommodationRepository, accommodationMapper);
    }

    @Test
    @DisplayName("Verify method delete was invoked")
    void deleteById_ValidAccommodationId_Success() {
        Long accommodationId = 1L;
        accommodationService.deleteById(accommodationId);
        verify(accommodationRepository, times(1)).deleteById(accommodationId);
        doAnswer(invocation -> {
            invocation.getArgument(0);
            return null;
        }).when(notificationService).notifyCancellationAccommodation(anyLong());

        notificationService.notifyCancellationAccommodation(accommodationId);
        verifyNoMoreInteractions(accommodationRepository);
    }

    private AccommodationResponseDto createAccommodationResponseDto() {
        AccommodationResponseDto responseDto = new AccommodationResponseDto();
        responseDto.setAddress(createAddress());
        responseDto.setAmenities(List.of("Bath", "Big kitchen", "Wi-fi"));
        responseDto.setType(Accommodation.Type.APARTMENT);
        responseDto.setDailyRate(BigDecimal.valueOf(100));
        responseDto.setSize("Small");
        responseDto.setAvailability(2);
        return responseDto;
    }

    private Accommodation createAccommodation() {
        Accommodation accommodation = new Accommodation();
        accommodation.setId(1L);
        accommodation.setAddress(createAddress());
        accommodation.setAmenities(List.of("Bath", "Big kitchen", "Wi-fi"));
        accommodation.setType(Accommodation.Type.APARTMENT);
        accommodation.setDailyRate(BigDecimal.valueOf(100));
        accommodation.setSize("Small");
        accommodation.setAvailability(2);
        return accommodation;
    }

    private CreateAccommodationRequestDto createAccommodationRequestDto() {
        CreateAccommodationRequestDto requestDto = new CreateAccommodationRequestDto();
        requestDto.setAddress(createAddress());
        requestDto.setAmenities(List.of("Bath", "Big kitchen", "Wi-fi"));
        requestDto.setType(Accommodation.Type.APARTMENT);
        requestDto.setDailyRate(BigDecimal.valueOf(100));
        requestDto.setSize("Small");
        requestDto.setAvailability(2);
        return requestDto;
    }

    private Address createAddress() {
        Address address = new Address();
        address.setId(1L);
        address.setRegion("New York");
        address.setCity("New York");
        address.setStreet("John Glen Blvd, 10");
        return address;
    }
}

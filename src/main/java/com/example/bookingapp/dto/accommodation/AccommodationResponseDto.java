package com.example.bookingapp.dto.accommodation;

import com.example.bookingapp.model.Accommodation;
import com.example.bookingapp.model.Address;
import java.math.BigDecimal;
import java.util.List;
import lombok.Data;

@Data
public class AccommodationResponseDto {
    private Long id;
    private Accommodation.Type type;
    private Address address;
    private String size;
    private List<String> amenities;
    private BigDecimal dailyRate;
    private Integer availability;
}

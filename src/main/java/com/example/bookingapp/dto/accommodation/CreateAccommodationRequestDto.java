package com.example.bookingapp.dto.accommodation;

import com.example.bookingapp.model.Accommodation;
import com.example.bookingapp.model.Address;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;
import lombok.Data;

@Data
public class CreateAccommodationRequestDto {
    @NotNull
    private Accommodation.Type type;
    @NotNull
    private Address address;
    @NotNull
    private String size;
    @NotNull
    private List<String> amenities;
    @NotNull
    @Min(0)
    private BigDecimal dailyRate;
    @NotNull
    @Min(0)
    private Integer availability;
}

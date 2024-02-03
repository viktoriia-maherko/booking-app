package com.example.bookingapp.dto.accommodation;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;
import lombok.Data;

@Data
public class AccommodationUpdateRequestDto {
    @NotNull
    private List<String> amenities;
    @NotNull
    @Min(0)
    private BigDecimal dailyRate;
    @NotNull
    @Min(0)
    private Integer availability;
}

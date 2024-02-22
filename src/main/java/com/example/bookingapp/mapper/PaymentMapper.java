package com.example.bookingapp.mapper;

import com.example.bookingapp.config.MapperConfig;
import com.example.bookingapp.dto.payment.PaymentResponseDto;
import com.example.bookingapp.model.Payment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapperConfig.class)
public interface PaymentMapper {
    @Mapping(source = "booking.id", target = "bookingId")
    @Mapping(target = "paymentUrl", ignore = true)
    PaymentResponseDto toDto(Payment payment);
}

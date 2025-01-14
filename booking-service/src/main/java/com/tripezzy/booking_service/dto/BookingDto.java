package com.tripezzy.booking_service.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class BookingDto {

    private String firstName;

    private String lastName;

    private String email;

    private String phoneNumber;

    private Long userId;

    private Long destinationId;

    private LocalDate travelDate;

    private BigDecimal totalPrice;

    private PaymentInformationDto paymentInformationDto;

}

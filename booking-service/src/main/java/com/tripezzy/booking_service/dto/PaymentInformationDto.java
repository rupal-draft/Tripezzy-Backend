package com.tripezzy.booking_service.dto;

import lombok.Data;

@Data
public class PaymentInformationDto {

    private String cardName;

    private String cardNumber;

    private String expirationDate;

    private Long cvv;

}

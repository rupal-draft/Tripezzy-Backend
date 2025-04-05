package com.tripezzy.payment_service.exceptions;

public class PaymentProcessingException extends RuntimeException {
  public PaymentProcessingException(String message) {
    super(message);
  }
}

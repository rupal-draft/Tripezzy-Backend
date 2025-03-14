package com.tripezzy.api_gateway.exceptions;

import io.jsonwebtoken.JwtException;

public class TokenValidationException extends JwtException {
  public TokenValidationException(String message) {
    super(message);
  }
}

package com.tripezzy.api_gateway.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Service
public class JwtService {

    @Value("${jwt.secretKey}")
    private String secretKey;

    private SecretKey getSecretKey() {
        if (secretKey == null || secretKey.length() < 32) {
            throw new IllegalStateException("Secret key must be at least 32 characters long for HS256:"+secretKey.length()+secretKey);
        }
        return Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    public Map<String, String> getUserDetails(String token){
        Claims claims = Jwts
                .parser()
                .verifyWith(getSecretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        Map<String, String> userDetails = new HashMap<>();

        userDetails.put("id", claims.getSubject());
        userDetails.put("role", claims.get("role", String.class));

        return userDetails;
    }
}

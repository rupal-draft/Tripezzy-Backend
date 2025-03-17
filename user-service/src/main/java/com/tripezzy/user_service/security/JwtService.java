package com.tripezzy.user_service.security;

import com.tripezzy.user_service.entity.User;
import com.tripezzy.user_service.exceptions.IllegalState;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtService {

    @Value("${jwt.secretKey}")
    private String secretKey;

    private SecretKey getSecretKey() {
        if (secretKey == null || secretKey.length() < 32) {
            throw new IllegalState("Secret key must be at least 32 characters long for HS256:"+secretKey.length()+secretKey);
        }
        return Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    public String getAccessJwtToken(User user){
        return Jwts
                .builder()
                .subject(String.valueOf(user.getId()))
                .claim("email",user.getEmail())
                .claim("role",user.getRole().toString())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 1000L * 60 * 60 * 24 * 1))
                .signWith(getSecretKey())
                .compact();
    }
}

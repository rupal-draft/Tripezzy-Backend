package com.tripezzy.user_service.controller;

import com.tripezzy.user_service.dto.*;
import com.tripezzy.user_service.exceptions.RuntimeConflict;
import com.tripezzy.user_service.service.AuthService;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping(path = "/signup")
    @RateLimiter(name = "signupLimiter", fallbackMethod = "rateLimitFallback")
    public ResponseEntity<UserDto> signup(@RequestBody UserRegisterDto signupDto){
        UserDto user = authService.signup(signupDto);
        return new ResponseEntity<>(user, HttpStatus.CREATED);
    }

    @PostMapping(path = "/login")
    @RateLimiter(name = "loginLimiter", fallbackMethod = "rateLimitFallback")
    public ResponseEntity<String> login(@RequestBody UserLoginDto loginDto){
        String token = authService.login(loginDto);
        return ResponseEntity.ok(token);
    }

    @PostMapping(path = "/onboard/seller")
    @RateLimiter(name = "sellerOnboardLimiter", fallbackMethod = "rateLimitFallback")
    public ResponseEntity<UserDto> onboardSeller(@RequestBody OnboardSellerDto onboardSellerDto){
        UserDto seller = authService.onboardSeller(onboardSellerDto);
        return new ResponseEntity<>(seller, HttpStatus.CREATED);
    }

    @PostMapping(path = "/onboard/guide")
    @RateLimiter(name = "guideOnboardLimiter", fallbackMethod = "rateLimitFallback")
    public ResponseEntity<UserDto> onboardGuide(@RequestBody OnboardGuideDto onboardGuideDto){
        UserDto guide = authService.onboardGuide(onboardGuideDto);
        return new ResponseEntity<>(guide, HttpStatus.CREATED);
    }

    public ResponseEntity<String> rateLimitFallback() {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .body("Too many requests. Please try again later.");
    }
}

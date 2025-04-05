package com.tripezzy.user_service.controller;

import com.tripezzy.user_service.annotations.RoleRequired;
import com.tripezzy.user_service.dto.*;
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

    @PostMapping(path = "/register")
    @RateLimiter(name = "signupLimiter", fallbackMethod = "rateLimitFallbackSignup")
    public ResponseEntity<UserDto> signup(@RequestBody UserRegisterDto signupDto){
        UserDto user = authService.signup(signupDto);
        return new ResponseEntity<>(user, HttpStatus.CREATED);
    }

    @PostMapping(path = "/login")
    @RateLimiter(name = "loginLimiter", fallbackMethod = "rateLimitFallbackLogin")
    public ResponseEntity<UserLoginResponseDto> login(@RequestBody UserLoginDto loginDto){
        UserLoginResponseDto loginResponse = authService.login(loginDto);
        return ResponseEntity.ok(loginResponse);
    }

    @PostMapping(path = "/onboard/seller")
    @RoleRequired("ADMIN")
    @RateLimiter(name = "sellerOnboardLimiter", fallbackMethod = "rateLimitFallbackSeller")
    public ResponseEntity<UserDto> onboardSeller(@RequestBody OnboardSellerDto onboardSellerDto){
        UserDto seller = authService.onboardSeller(onboardSellerDto);
        return new ResponseEntity<>(seller, HttpStatus.CREATED);
    }

    @PostMapping(path = "/onboard/guide")
    @RoleRequired("ADMIN")
    @RateLimiter(name = "guideOnboardLimiter", fallbackMethod = "rateLimitFallbackGuide")
    public ResponseEntity<UserDto> onboardGuide(@RequestBody OnboardGuideDto onboardGuideDto){
        UserDto guide = authService.onboardGuide(onboardGuideDto);
        return new ResponseEntity<>(guide, HttpStatus.CREATED);
    }

    public ResponseEntity<String> rateLimitFallbackSignup(UserRegisterDto signupDto, Throwable throwable) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .body("Too many signup attempts. Please try again later.");
    }

    public ResponseEntity<String> rateLimitFallbackLogin(UserLoginDto loginDto, Throwable throwable) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .body("Too many login attempts. Please try again later.");
    }

    public ResponseEntity<String> rateLimitFallbackSeller(OnboardSellerDto onboardSellerDto, Throwable throwable) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .body("Too many seller onboard requests. Please try again later.");
    }

    public ResponseEntity<String> rateLimitFallbackGuide(OnboardGuideDto onboardGuideDto, Throwable throwable) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .body("Too many guide onboard requests. Please try again later.");
    }
}

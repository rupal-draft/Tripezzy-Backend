package com.tripezzy.user_service.service;

import com.tripezzy.user_service.dto.*;

import java.util.List;

public interface AuthService {
    UserDto signup(UserRegisterDto signupDto);

    UserLoginResponseDto login(UserLoginDto loginDto);

    UserDto onboardSeller(OnboardSellerDto onboardSellerDto);

    UserDto onboardGuide(OnboardGuideDto onboardGuideDto);

    List<UserDto> getAllUsers();

    List<UserDto> getAllAdminUsers();

    List<UserDto> getAllSellerUsers();

    List<UserDto> getAllGuideUsers();

    UserDto getUserById(Long id);
}

package com.tripezzy.user_service.service;

import com.tripezzy.user_service.dto.*;

public interface AuthService {
    UserDto signup(UserRegisterDto signupDto);

    String login(UserLoginDto loginDto);

    UserDto onboardSeller(OnboardSellerDto onboardSellerDto);

    UserDto onboardGuide(OnboardGuideDto onboardGuideDto);
}

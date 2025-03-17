package com.tripezzy.user_service.service.implementations;

import com.tripezzy.user_service.dto.*;
import com.tripezzy.user_service.entity.Guide;
import com.tripezzy.user_service.entity.Seller;
import com.tripezzy.user_service.entity.User;
import com.tripezzy.user_service.entity.enums.UserRole;
import com.tripezzy.user_service.exceptions.ResourceConflictException;
import com.tripezzy.user_service.exceptions.ResourceNotFound;
import com.tripezzy.user_service.repository.UserRepository;
import com.tripezzy.user_service.security.JwtService;
import com.tripezzy.user_service.service.AuthService;
import com.tripezzy.user_service.utils.PasswordUtil;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
public class AuthServiceImpl implements AuthService {


    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final JwtService jwtService;

    public AuthServiceImpl(UserRepository userRepository, ModelMapper modelMapper, JwtService jwtService) {
        this.userRepository = userRepository;
        this.modelMapper = modelMapper;
        this.jwtService = jwtService;
    }

    @Override
    @Transactional
    public UserDto signup(UserRegisterDto signupDto) {
        log.info("Processing signup for email: {}", signupDto.getEmail());

        if (userRepository.existsByEmail(signupDto.getEmail())) {
            log.warn("Signup failed: Email already exists - {}", signupDto.getEmail());
            throw new ResourceConflictException("User already exists with email: " + signupDto.getEmail());
        }

        User user = modelMapper.map(signupDto, User.class);
        user.setPassword(PasswordUtil.hashPassword(signupDto.getPassword()));
        user.setRole(UserRole.USER);

        user = userRepository.save(user);

        log.info("User successfully signed up with ID: {}", user.getId());
        return modelMapper.map(user, UserDto.class);
    }

    @Override
    public UserLoginResponseDto login(UserLoginDto loginDto) {
        log.info("Processing login for email: {}", loginDto.getEmail());

        User user = userRepository.findByEmail(loginDto.getEmail())
                .orElseThrow(() -> {
                    log.warn("Login failed: User not found - {}", loginDto.getEmail());
                    return new ResourceNotFound("User not found with email: " + loginDto.getEmail());
                });

        if (!PasswordUtil.checkPassword(loginDto.getPassword(), user.getPassword())) {
            log.warn("Login failed: Invalid password for email - {}", loginDto.getEmail());
            throw new ResourceConflictException("Invalid credentials");
        }

        String token = jwtService.getAccessJwtToken(user);
        log.info("User successfully logged in with ID: {}", user.getId());

        UserLoginResponseDto userLoginResponseDto = new UserLoginResponseDto();
        userLoginResponseDto.setAccessToken(token);
        userLoginResponseDto.setUserId(user.getId());

        return userLoginResponseDto;
    }

    @Override
    @Transactional
    public UserDto onboardSeller(OnboardSellerDto onboardSellerDto) {
        log.info("Processing seller onboarding for email: {}", onboardSellerDto.getEmail());

        if (userRepository.existsByEmail(onboardSellerDto.getEmail())) {
            log.warn("Seller onboarding failed: Email already exists - {}", onboardSellerDto.getEmail());
            throw new ResourceConflictException("Seller already exists with email: " + onboardSellerDto.getEmail());
        }

        User seller = createUserWithProfile(onboardSellerDto, UserRole.SELLER);
        Seller sellerProfile = modelMapper.map(onboardSellerDto, Seller.class);
        sellerProfile.setUser(seller);
        seller.setSellerProfile(sellerProfile);

        userRepository.save(seller);

        log.info("Seller onboarded successfully with ID: {}", seller.getId());
        return modelMapper.map(seller, UserDto.class);
    }

    @Override
    @Transactional
    public UserDto onboardGuide(OnboardGuideDto onboardGuideDto) {
        log.info("Processing guide onboarding for email: {}", onboardGuideDto.getEmail());

        if (userRepository.existsByEmail(onboardGuideDto.getEmail())) {
            log.warn("Guide onboarding failed: Email already exists - {}", onboardGuideDto.getEmail());
            throw new ResourceConflictException("Guide already exists with email: " + onboardGuideDto.getEmail());
        }

        User guide = createUserWithProfile(onboardGuideDto, UserRole.GUIDE);
        Guide guideProfile = modelMapper.map(onboardGuideDto, Guide.class);
        guideProfile.setUser(guide);
        guide.setGuideProfile(guideProfile);

        userRepository.save(guide);

        log.info("Guide onboarded successfully with ID: {}", guide.getId());
        return modelMapper.map(guide, UserDto.class);
    }

    private <T> User createUserWithProfile(T profileDto, UserRole role) {
        User user = modelMapper.map(profileDto, User.class);
        user.setRole(role);

        if (profileDto instanceof OnboardSellerDto) {
            OnboardSellerDto sellerDto = (OnboardSellerDto) profileDto;
            user.setPassword(PasswordUtil.hashPassword(sellerDto.getPassword()));
            user.setPhoneNumber(sellerDto.getPhoneNumber());
        } else if (profileDto instanceof OnboardGuideDto) {
            OnboardGuideDto guideDto = (OnboardGuideDto) profileDto;
            user.setPassword(PasswordUtil.hashPassword(guideDto.getPassword()));
            user.setPhoneNumber(guideDto.getPhoneNumber());
        } else {
            throw new IllegalArgumentException("Invalid profile type: " + profileDto.getClass().getSimpleName());
        }
        return user;
    }

}

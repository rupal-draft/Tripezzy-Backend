package com.tripezzy.user_service.service.implementations;

import com.tripezzy.user_service.dto.*;
import com.tripezzy.user_service.entity.Guide;
import com.tripezzy.user_service.entity.Seller;
import com.tripezzy.user_service.entity.User;
import com.tripezzy.user_service.entity.enums.UserRole;
import com.tripezzy.user_service.exceptions.*;
import com.tripezzy.user_service.exceptions.ResourceConflictException;
import com.tripezzy.user_service.repository.UserRepository;
import com.tripezzy.user_service.security.JwtService;
import com.tripezzy.user_service.service.AuthService;
import com.tripezzy.user_service.utils.PasswordUtil;
import org.modelmapper.MappingException;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
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
        try {
            log.info("Processing signup for email: {}", signupDto.getEmail());

            // Validate input
            if (signupDto.getEmail() == null || signupDto.getEmail().isBlank()) {
                throw new BadRequestException("Email is required");
            }
            if (signupDto.getPassword() == null || signupDto.getPassword().isBlank()) {
                throw new BadRequestException("Password is required");
            }

            if (userRepository.existsByEmail(signupDto.getEmail())) {
                log.warn("Signup failed: Email already exists - {}", signupDto.getEmail());
                throw new ResourceConflictException("User already exists with email: " + signupDto.getEmail());
            }

            User user = modelMapper.map(signupDto, User.class);
            user.setPassword(PasswordUtil.hashPassword(signupDto.getPassword()));
            user.setRole(UserRole.USER);

            User savedUser = userRepository.save(user);
            log.info("User successfully signed up with ID: {}", savedUser.getId());

            return modelMapper.map(savedUser, UserDto.class);

        } catch (DataAccessException ex) {
            log.error("Database error during signup for email: {}", signupDto.getEmail(), ex);
            throw new DataIntegrityViolation("Failed to create user due to database error");
        } catch (MappingException ex) {
            log.error("Mapping error during signup", ex);
            throw new IllegalState("Failed to map user data");
        }
    }

    @Override
    public UserLoginResponseDto login(UserLoginDto loginDto) {
        try {
            log.info("Processing login for email: {}", loginDto.getEmail());

            // Validate input
            if (loginDto.getEmail() == null || loginDto.getEmail().isBlank()) {
                throw new BadRequestException("Email is required");
            }
            if (loginDto.getPassword() == null || loginDto.getPassword().isBlank()) {
                throw new BadRequestException("Password is required");
            }

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

            UserLoginResponseDto response = new UserLoginResponseDto();
            response.setAccessToken(token);
            response.setUserId(user.getId());

            return response;

        } catch (DataAccessException ex) {
            log.error("Database error during login for email: {}", loginDto.getEmail(), ex);
            throw new ServiceUnavailable("Unable to process login at this time");
        }
    }

    @Override
    @Transactional
    public UserDto onboardSeller(OnboardSellerDto onboardSellerDto) {
        try {
            log.info("Processing seller onboarding for email: {}", onboardSellerDto.getEmail());

            // Validate input
            if (onboardSellerDto.getEmail() == null || onboardSellerDto.getEmail().isBlank()) {
                throw new BadRequestException("Email is required");
            }
            if (onboardSellerDto.getPassword() == null || onboardSellerDto.getPassword().isBlank()) {
                throw new BadRequestException("Password is required");
            }
            if (onboardSellerDto.getPhoneNumber() == null || onboardSellerDto.getPhoneNumber().isBlank()) {
                throw new BadRequestException("Phone number is required");
            }

            if (userRepository.existsByEmail(onboardSellerDto.getEmail())) {
                log.warn("Seller onboarding failed: Email already exists - {}", onboardSellerDto.getEmail());
                throw new ResourceConflictException("Seller already exists with email: " + onboardSellerDto.getEmail());
            }

            User seller = createUserWithProfile(onboardSellerDto, UserRole.SELLER);
            Seller sellerProfile = modelMapper.map(onboardSellerDto, Seller.class);
            sellerProfile.setUser(seller);
            seller.setSellerProfile(sellerProfile);

            User savedSeller = userRepository.save(seller);
            log.info("Seller onboarded successfully with ID: {}", savedSeller.getId());

            return modelMapper.map(savedSeller, UserDto.class);

        } catch (DataAccessException ex) {
            log.error("Database error during seller onboarding", ex);
            throw new DataIntegrityViolation("Failed to onboard seller due to database error");
        } catch (MappingException ex) {
            log.error("Mapping error during seller onboarding", ex);
            throw new IllegalState("Failed to map seller data");
        }
    }

    @Override
    @Transactional
    public UserDto onboardGuide(OnboardGuideDto onboardGuideDto) {
        try {
            log.info("Processing guide onboarding for email: {}", onboardGuideDto.getEmail());

            // Validate input
            if (onboardGuideDto.getEmail() == null || onboardGuideDto.getEmail().isBlank()) {
                throw new BadRequestException("Email is required");
            }
            if (onboardGuideDto.getPassword() == null || onboardGuideDto.getPassword().isBlank()) {
                throw new BadRequestException("Password is required");
            }
            if (onboardGuideDto.getPhoneNumber() == null || onboardGuideDto.getPhoneNumber().isBlank()) {
                throw new BadRequestException("Phone number is required");
            }

            if (userRepository.existsByEmail(onboardGuideDto.getEmail())) {
                log.warn("Guide onboarding failed: Email already exists - {}", onboardGuideDto.getEmail());
                throw new ResourceConflictException("Guide already exists with email: " + onboardGuideDto.getEmail());
            }

            User guide = createUserWithProfile(onboardGuideDto, UserRole.GUIDE);
            Guide guideProfile = modelMapper.map(onboardGuideDto, Guide.class);
            guideProfile.setUser(guide);
            guide.setGuideProfile(guideProfile);

            User savedGuide = userRepository.save(guide);
            log.info("Guide onboarded successfully with ID: {}", savedGuide.getId());

            return modelMapper.map(savedGuide, UserDto.class);

        } catch (DataAccessException ex) {
            log.error("Database error during guide onboarding", ex);
            throw new DataIntegrityViolation("Failed to onboard guide due to database error");
        } catch (MappingException ex) {
            log.error("Mapping error during guide onboarding", ex);
            throw new IllegalState("Failed to map guide data");
        }
    }

    private <T> User createUserWithProfile(T profileDto, UserRole role) {
        try {
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
        } catch (Exception ex) {
            log.error("Error creating user with profile", ex);
            throw new IllegalState("Failed to create user profile");
        }
    }
}

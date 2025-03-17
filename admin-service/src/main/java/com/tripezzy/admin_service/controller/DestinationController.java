package com.tripezzy.admin_service.controller;

import com.tripezzy.admin_service.advices.ApiError;
import com.tripezzy.admin_service.advices.ApiResponse;
import com.tripezzy.admin_service.annotations.RoleRequired;
import com.tripezzy.admin_service.dto.DestinationDto;
import com.tripezzy.admin_service.service.DestinationService;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/destinations")
@Validated
public class DestinationController {

    private final DestinationService destinationService;

    public DestinationController(DestinationService destinationService) {
        this.destinationService = destinationService;
    }

    @PostMapping
    @RateLimiter(name = "createDestinationRateLimiter", fallbackMethod = "createDestinationRateLimitFallback")
    @RoleRequired("ADMIN")
    public ResponseEntity<DestinationDto> createDestination(@Valid @RequestBody DestinationDto dto) {
        return ResponseEntity.ok(destinationService.createDestination(dto));
    }

    @GetMapping("/public")
    @RateLimiter(name = "getAllDestinationsRateLimiter", fallbackMethod = "getAllDestinationsRateLimitFallback")
    public ResponseEntity<Page<DestinationDto>> getAllDestinations(Pageable pageable) {
        return ResponseEntity.ok(destinationService.getAllDestinations(pageable));
    }

    @GetMapping("/public/{id}")
    @RateLimiter(name = "getDestinationByIdRateLimiter", fallbackMethod = "getDestinationByIdRateLimitFallback")
    public ResponseEntity<DestinationDto> getDestinationById(@PathVariable Long id) {
        return ResponseEntity.ok(destinationService.getDestinationById(id));
    }

    @PutMapping("/{id}")
    @RateLimiter(name = "updateDestinationRateLimiter", fallbackMethod = "updateDestinationRateLimitFallback")
    @RoleRequired("ADMIN")
    public ResponseEntity<DestinationDto> updateDestination(@PathVariable Long id, @Valid @RequestBody DestinationDto dto) {
        return ResponseEntity.ok(destinationService.updateDestination(id, dto));
    }

    @DeleteMapping("/soft-delete/{id}")
    @RateLimiter(name = "softDeleteDestinationRateLimiter", fallbackMethod = "softDeleteDestinationRateLimitFallback")
    @RoleRequired("ADMIN")
    public ResponseEntity<ApiResponse<String>> softDeleteDestination(@PathVariable Long id) {
        destinationService.softDeleteDestination(id);
        return ResponseEntity.ok(ApiResponse.success("Destination deleted successfully"));
    }

    @GetMapping("/public/search")
    @RateLimiter(name = "searchDestinationsRateLimiter", fallbackMethod = "searchDestinationsRateLimitFallbackForSearch")
    public ResponseEntity<List<DestinationDto>> searchDestinations(@RequestParam String keyword) {
        return ResponseEntity.ok(destinationService.searchDestinations(keyword));
    }

    @GetMapping("/public/filter")
    @RateLimiter(name = "filterDestinationsRateLimiter", fallbackMethod = "filterDestinationsRateLimitFallbackForFilter")
    public ResponseEntity<List<DestinationDto>> filterDestinations(
            @RequestParam(required = false) String country) {
        return ResponseEntity.ok(destinationService.filterDestinations(country));
    }

    private ResponseEntity<ApiResponse<String>> rateLimitFallback(String serviceName, Throwable throwable) {
        ApiError apiError = new ApiError
                .ApiErrorBuilder()
                .setMessage("Too many requests to " + serviceName + ". Please try again later.")
                .setStatus(HttpStatus.TOO_MANY_REQUESTS).build();
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .body(ApiResponse.error(apiError));
    }

    public ResponseEntity<ApiResponse<String>> createDestinationRateLimitFallback(DestinationDto dto, Throwable throwable) {
        return rateLimitFallback("createDestination", throwable);
    }

    public ResponseEntity<ApiResponse<String>> getAllDestinationsRateLimitFallback(Pageable pageable, Throwable throwable) {
        return rateLimitFallback("getAllDestinations", throwable);
    }

    public ResponseEntity<ApiResponse<String>> getDestinationByIdRateLimitFallback(Long id, Throwable throwable) {
        return rateLimitFallback("getDestinationById", throwable);
    }

    public ResponseEntity<ApiResponse<String>> updateDestinationRateLimitFallback(Long id, DestinationDto dto, Throwable throwable) {
        return rateLimitFallback("updateDestination", throwable);
    }

    public ResponseEntity<ApiResponse<String>> softDeleteDestinationRateLimitFallback(Long id, Throwable throwable) {
        return rateLimitFallback("softDeleteDestination", throwable);
    }

    public ResponseEntity<ApiResponse<String>> searchDestinationsRateLimitFallbackForSearch(String keyword, Throwable throwable) {
        return rateLimitFallback("searchDestinations", throwable);
    }

    public ResponseEntity<ApiResponse<String>> filterDestinationsRateLimitFallbackForFilter(String country, Throwable throwable) {
        return rateLimitFallback("filterDestinations", throwable);
    }


}

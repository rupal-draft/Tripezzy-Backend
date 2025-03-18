package com.tripezzy.admin_service.controller;

import com.tripezzy.admin_service.advices.ApiError;
import com.tripezzy.admin_service.advices.ApiResponse;
import com.tripezzy.admin_service.annotations.RoleRequired;
import com.tripezzy.admin_service.dto.TourDto;
import com.tripezzy.admin_service.service.TourService;
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
@RequestMapping("/tours")
@Validated
public class TourController {

    private final TourService tourService;

    public TourController(TourService tourService) {
        this.tourService = tourService;
    }

    @PostMapping
    @RateLimiter(name = "createTourRateLimiter", fallbackMethod = "createTourRateLimitFallback")
    @RoleRequired("ADMIN")
    public ResponseEntity<TourDto> createTour(@Valid @RequestBody TourDto dto) {
        return ResponseEntity.ok(tourService.createTour(dto));
    }

    @GetMapping("/public")
    @RateLimiter(name = "getAllToursRateLimiter", fallbackMethod = "getAllToursRateLimitFallback")
    public ResponseEntity<Page<TourDto>> getAllTours(Pageable pageable) {
        return ResponseEntity.ok(tourService.getAllTours(pageable));
    }

    @GetMapping("/public/{id}")
    @RateLimiter(name = "getTourByIdRateLimiter", fallbackMethod = "getTourByIdRateLimitFallback")
    public ResponseEntity<TourDto> getTourById(@PathVariable Long id) {
        return ResponseEntity.ok(tourService.getTourById(id));
    }

    @GetMapping("/public/destination/{destinationId}")
    @RateLimiter(name = "getToursByDestinationRateLimiter", fallbackMethod = "getToursByDestinationRateLimitFallback")
    public ResponseEntity<List<TourDto>> getToursByDestination(@PathVariable Long destinationId) {
        return ResponseEntity.ok(tourService.getToursByDestination(destinationId));
    }

    @PutMapping("/{id}")
    @RateLimiter(name = "updateTourRateLimiter", fallbackMethod = "updateTourRateLimitFallback")
    @RoleRequired("ADMIN")
    public ResponseEntity<TourDto> updateTour(@PathVariable Long id, @Valid @RequestBody TourDto dto) {
        return ResponseEntity.ok(tourService.updateTour(id, dto));
    }

    @DeleteMapping("/soft-delete/{id}")
    @RateLimiter(name = "softDeleteTourRateLimiter", fallbackMethod = "softDeleteTourRateLimitFallback")
    @RoleRequired("ADMIN")
    public ResponseEntity<ApiResponse<String>> softDeleteTour(@PathVariable Long id) {
        tourService.softDeleteTour(id);
        return ResponseEntity.ok(ApiResponse.success("Tour deleted successfully"));
    }

    @GetMapping("/public/search")
    @RateLimiter(name = "searchToursRateLimiter", fallbackMethod = "searchToursRateLimitFallback")
    public ResponseEntity<List<TourDto>> searchTours(@RequestParam String keyword) {
        return ResponseEntity.ok(tourService.searchTours(keyword));
    }

    @GetMapping("/public/filter")
    @RateLimiter(name = "filterToursRateLimiter", fallbackMethod = "filterToursRateLimitFallback")
    public ResponseEntity<List<TourDto>> filterTours(
            @RequestParam(required = false) Long destinationId,
            @RequestParam(required = false) Double minPrice,
                                                     @RequestParam(required = false) Double maxPrice,
                                                     @RequestParam(required = false) Integer capacity) {
        return ResponseEntity.ok(tourService.filterTours(destinationId,minPrice, maxPrice, capacity));
    }

    private ResponseEntity<ApiResponse<String>> rateLimitFallback(String serviceName, Throwable throwable) {
        ApiError apiError = new ApiError
                .ApiErrorBuilder()
                .setMessage("Too many requests to " + serviceName + ". Please try again later.")
                .setStatus(HttpStatus.TOO_MANY_REQUESTS).build();
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .body(ApiResponse.error(apiError));
    }

    public ResponseEntity<ApiResponse<String>> createTourRateLimitFallback(TourDto dto, Throwable throwable) {
        return rateLimitFallback("createTour", throwable);
    }

    public ResponseEntity<ApiResponse<String>> getAllToursRateLimitFallback(Pageable pageable, Throwable throwable) {
        return rateLimitFallback("getAllTours", throwable);
    }

    public ResponseEntity<ApiResponse<String>> getTourByIdRateLimitFallback(Long id, Throwable throwable) {
        return rateLimitFallback("getTourById", throwable);
    }

    public ResponseEntity<ApiResponse<String>> getToursByDestinationRateLimitFallback(Long destinationId, Throwable throwable) {
        return rateLimitFallback("getToursByDestination", throwable);
    }

    public ResponseEntity<ApiResponse<String>> updateTourRateLimitFallback(Long id, TourDto dto, Throwable throwable) {
        return rateLimitFallback("updateTour", throwable);
    }

    public ResponseEntity<ApiResponse<String>> softDeleteTourRateLimitFallback(Long id, Throwable throwable) {
        return rateLimitFallback("softDeleteTour", throwable);
    }

    public ResponseEntity<ApiResponse<String>> searchToursRateLimitFallback(String keyword, Throwable throwable) {
        return rateLimitFallback("searchTours", throwable);
    }

    public ResponseEntity<ApiResponse<String>> filterToursRateLimitFallback(Long destinationId, Double minPrice, Double maxPrice, Integer capacity, Throwable throwable) {
        return rateLimitFallback("filterTours", throwable);
    }

}

package com.tripezzy.admin_service.controller;

import com.tripezzy.admin_service.dto.TourDto;
import com.tripezzy.admin_service.exceptions.RuntimeConflict;
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
    @RateLimiter(name = "createTourRateLimiter", fallbackMethod = "rateLimitFallback")
    public ResponseEntity<TourDto> createTour(@Valid @RequestBody TourDto dto) {
        return ResponseEntity.ok(tourService.createTour(dto));
    }

    @GetMapping
    @RateLimiter(name = "getAllToursRateLimiter", fallbackMethod = "rateLimitFallback")
    public ResponseEntity<Page<TourDto>> getAllTours(Pageable pageable) {
        return ResponseEntity.ok(tourService.getAllTours(pageable));
    }

    @GetMapping("/{id}")
    @RateLimiter(name = "getTourByIdRateLimiter", fallbackMethod = "rateLimitFallback")
    public ResponseEntity<TourDto> getTourById(@PathVariable Long id) {
        return ResponseEntity.ok(tourService.getTourById(id));
    }

    @GetMapping("/destination/{destinationId}")
    @RateLimiter(name = "getToursByDestinationRateLimiter", fallbackMethod = "rateLimitFallback")
    public ResponseEntity<List<TourDto>> getToursByDestination(@PathVariable Long destinationId) {
        return ResponseEntity.ok(tourService.getToursByDestination(destinationId));
    }

    @PutMapping("/{id}")
    @RateLimiter(name = "updateTourRateLimiter", fallbackMethod = "rateLimitFallback")
    public ResponseEntity<TourDto> updateTour(@PathVariable Long id, @Valid @RequestBody TourDto dto) {
        return ResponseEntity.ok(tourService.updateTour(id, dto));
    }

    @DeleteMapping("/{id}")
    @RateLimiter(name = "deleteTourRateLimiter", fallbackMethod = "rateLimitFallback")
    public ResponseEntity<String> deleteTour(@PathVariable Long id) {
        tourService.deleteTour(id);
        return ResponseEntity.ok("Tour deleted successfully.");
    }

    @DeleteMapping("/soft-delete/{id}")
    @RateLimiter(name = "softDeleteTourRateLimiter", fallbackMethod = "rateLimitFallback")
    public ResponseEntity<String> softDeleteTour(@PathVariable Long id) {
        tourService.softDeleteTour(id);
        return ResponseEntity.ok("Tour soft deleted successfully.");
    }

    @GetMapping("/search")
    @RateLimiter(name = "searchToursRateLimiter", fallbackMethod = "rateLimitFallback")
    public ResponseEntity<List<TourDto>> searchTours(@RequestParam String keyword) {
        return ResponseEntity.ok(tourService.searchTours(keyword));
    }

    @GetMapping("/filter")
    @RateLimiter(name = "filterToursRateLimiter", fallbackMethod = "rateLimitFallback")
    public ResponseEntity<List<TourDto>> filterTours(
            @RequestParam(required = false) Long destinationId,
            @RequestParam(required = false) Double minPrice,
                                                     @RequestParam(required = false) Double maxPrice,
                                                     @RequestParam(required = false) String category,@RequestParam(required = false) String status) {
        return ResponseEntity.ok(tourService.filterTours(destinationId,minPrice, maxPrice, category,status));
    }

    public ResponseEntity<String> rateLimitFallback() {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .body("Too many requests. Please try again later.");
    }
}

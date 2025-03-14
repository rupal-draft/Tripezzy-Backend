package com.tripezzy.admin_service.controller;

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
    @RateLimiter(name = "createDestinationRateLimiter", fallbackMethod = "rateLimitFallback")
    @RoleRequired("ADMIN")
    public ResponseEntity<DestinationDto> createDestination(@Valid @RequestBody DestinationDto dto) {
        return ResponseEntity.ok(destinationService.createDestination(dto));
    }

    @GetMapping
    @RateLimiter(name = "getAllDestinationsRateLimiter", fallbackMethod = "rateLimitFallback")
    public ResponseEntity<Page<DestinationDto>> getAllDestinations(Pageable pageable) {
        return ResponseEntity.ok(destinationService.getAllDestinations(pageable));
    }

    @GetMapping("/{id}")
    @RateLimiter(name = "getDestinationByIdRateLimiter", fallbackMethod = "rateLimitFallback")
    public ResponseEntity<DestinationDto> getDestinationById(@PathVariable Long id) {
        return ResponseEntity.ok(destinationService.getDestinationById(id));
    }

    @PutMapping("/{id}")
    @RateLimiter(name = "updateDestinationRateLimiter", fallbackMethod = "rateLimitFallback")
    @RoleRequired("ADMIN")
    public ResponseEntity<DestinationDto> updateDestination(@PathVariable Long id, @Valid @RequestBody DestinationDto dto) {
        return ResponseEntity.ok(destinationService.updateDestination(id, dto));
    }

    @DeleteMapping("/{id}")
    @RateLimiter(name = "deleteDestinationRateLimiter", fallbackMethod = "rateLimitFallback")
    @RoleRequired("ADMIN")
    public ResponseEntity<String> deleteDestination(@PathVariable Long id) {
        destinationService.deleteDestination(id);
        return ResponseEntity.ok("Destination deleted successfully.");
    }

    @DeleteMapping("/soft-delete/{id}")
    @RateLimiter(name = "softDeleteDestinationRateLimiter", fallbackMethod = "rateLimitFallback")
    @RoleRequired("ADMIN")
    public ResponseEntity<String> softDeleteDestination(@PathVariable Long id) {
        destinationService.softDeleteDestination(id);
        return ResponseEntity.ok("Destination soft deleted successfully.");
    }

    @GetMapping("/search")
    @RateLimiter(name = "searchDestinationsRateLimiter", fallbackMethod = "rateLimitFallback")
    public ResponseEntity<List<DestinationDto>> searchDestinations(@RequestParam String keyword) {
        return ResponseEntity.ok(destinationService.searchDestinations(keyword));
    }

    @GetMapping("/filter")
    @RateLimiter(name = "filterDestinationsRateLimiter", fallbackMethod = "rateLimitFallback")
    public ResponseEntity<List<DestinationDto>> filterDestinations(
            @RequestParam(required = false) String country,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice) {
        return ResponseEntity.ok(destinationService.filterDestinations(country, category, minPrice, maxPrice));
    }

    public ResponseEntity<String> rateLimitFallback() {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .body("Too many requests. Please try again later.");
    }
}

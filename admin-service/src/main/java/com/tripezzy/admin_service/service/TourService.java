package com.tripezzy.admin_service.service;

import com.tripezzy.admin_service.dto.TourDto;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface TourService {
    TourDto createTour(@Valid TourDto dto);

    Page<TourDto> getAllTours(Pageable pageable);

    TourDto getTourById(Long id);

    List<TourDto> getToursByDestination(Long destinationId);

    TourDto updateTour(Long id, @Valid TourDto dto);

    void softDeleteTour(Long id);

    List<TourDto> searchTours(String keyword);

    List<TourDto> filterTours(Long destinationId, Double minPrice, Double maxPrice, Integer capacity);
}

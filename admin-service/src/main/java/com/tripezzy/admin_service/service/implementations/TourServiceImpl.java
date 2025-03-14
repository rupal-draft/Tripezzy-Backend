package com.tripezzy.admin_service.service.implementations;

import com.tripezzy.admin_service.dto.TourDto;
import com.tripezzy.admin_service.entity.TourPackage;
import com.tripezzy.admin_service.exceptions.DataIntegrityViolation;
import com.tripezzy.admin_service.exceptions.IllegalState;
import com.tripezzy.admin_service.exceptions.ResourceNotFound;
import com.tripezzy.admin_service.repository.TourRepository;
import com.tripezzy.admin_service.repository.specifications.TourSpecification;
import com.tripezzy.admin_service.service.TourService;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TourServiceImpl implements TourService {


    private static final Logger log = LoggerFactory.getLogger(TourServiceImpl.class);
    private final TourRepository tourRepository;
    private final ModelMapper modelMapper;

    public TourServiceImpl(TourRepository tourRepository, ModelMapper modelMapper) {
        this.tourRepository = tourRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    @CacheEvict(value = "destination", key = "#id")
    @Transactional
    public TourDto createTour(TourDto dto) {
        log.info("Creating new Tour: {}", dto.getName());
        try {
            if (dto.getName() == null || dto.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalState("Invalid tour data: Name and price must be valid");
            }

            TourPackage tour = modelMapper.map(dto, TourPackage.class);
            return modelMapper.map(tourRepository.save(tour), TourDto.class);
        } catch (DataIntegrityViolation ex) {
            log.error("Database error while creating tour", ex);
            throw new DataIntegrityViolation("Tour creation failed due to database constraints");
        }
    }

    @Override
    @Cacheable(value = "tours", key = "#pageable")
    public Page<TourDto> getAllTours(Pageable pageable) {
        log.info("Fetching all tours with pagination");
        Page<TourPackage> tourPage = tourRepository.findByDeletedFalse(pageable);

        if (tourPage.isEmpty()) {
            throw new ResourceNotFound("No tours available");
        }
        return tourPage.map(tour -> modelMapper.map(tour, TourDto.class));
    }

    @Override
    @Cacheable(value = "tour", key = "#id", unless = "#result == null")
    public TourDto getTourById(Long id) {
        log.info("Fetching Tour with ID: {}", id);
        return tourRepository.findById(id)
                .filter(tour -> !tour.isDeleted())
                .map(tour -> modelMapper.map(tour, TourDto.class))
                .orElseThrow(() -> new ResourceNotFound("Tour not found or deleted"));
    }

    @Override
    @Cacheable(value = "toursByDestination", key = "#destinationId")
    public List<TourDto> getToursByDestination(Long destinationId) {
        log.info("Fetching tours for Destination ID: {}", destinationId);
        List<TourPackage> tours = tourRepository.findByDestinationIdAndDeletedFalse(destinationId);

        if (tours.isEmpty()) {
            log.warn("No tours found for Destination ID: {}", destinationId);
            throw new ResourceNotFound("No tours found for the given destination");
        }
        return tours
                .stream()
                .map(tour -> modelMapper.map(tour, TourDto.class))
                .toList();
    }

    @Override
    @CacheEvict(value = "tour", key = "#id")
    @Transactional
    public TourDto updateTour(Long id, TourDto dto) {
        log.info("Updating Tour ID: {}", id);
        TourPackage tour = tourRepository.findById(id)
                .filter(t -> !t.isDeleted())
                .orElseThrow(() -> new ResourceNotFound("Tour not found or deleted"));

        if (dto.getPrice() != null && dto.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalState("Price must be greater than zero");
        }

        modelMapper.map(dto, tour);
        return modelMapper.map(tourRepository.save(tour), TourDto.class);
    }

    @Override
    @CacheEvict(value = "tour", key = "#id")
    public void deleteTour(Long id) {
        log.info("Deleting Tour ID: {}", id);
        tourRepository.findById(id)
                .filter(t -> !t.isDeleted())
                .orElseThrow(() -> new ResourceNotFound("Tour not found or deleted"));

        tourRepository.deleteById(id);
    }

    @Override
    @CacheEvict(value = "tour", key = "#id")
    public void softDeleteTour(Long id) {
        log.info("Soft deleting Tour ID: {}", id);
        TourPackage tour = tourRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFound("Tour not found"));

        if (tour.isDeleted()) {
            throw new IllegalState("Tour is already deleted");
        }

        tour.setDeleted(true);
        tourRepository.save(tour);
    }

    @Override
    @Cacheable(value = "tours", key = "#keyword")
    public List<TourDto> searchTours(String keyword) {
        log.info("Searching for tours with keyword: {}", keyword);

        if (keyword == null || keyword.trim().isEmpty()) {
            throw new IllegalArgumentException("Search keyword cannot be null or empty");
        }

        List<TourPackage> tours = tourRepository.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(keyword, keyword);

        if (tours.isEmpty()) {
            log.warn("No tours found for keyword: {}", keyword);
            return Collections.emptyList();
        }

        return tours.stream()
                .map(tour -> modelMapper.map(tour, TourDto.class))
                .collect(Collectors.toList());
    }


    @Override
    @Cacheable(value = "filteredTours", key = "{#destinationId, #minPrice, #maxPrice, #category, #status}")
    public List<TourDto> filterTours(Long destinationId, Double minPrice, Double maxPrice, String category, String status) {
        log.info("Filtering tours with params - Destination ID: {}, Min Price: {}, Max Price: {}, Category: {}, Status: {}",
                destinationId, minPrice, maxPrice, category, status);

        if (minPrice != null && maxPrice != null && minPrice > maxPrice) {
            throw new IllegalArgumentException("Minimum price cannot be greater than maximum price");
        }

        List<TourPackage> tours = tourRepository.findAll(TourSpecification.filterBy(destinationId, category, minPrice, maxPrice, status));

        if (tours.isEmpty()) {
            log.warn("No tours found matching the given filters");
            return Collections.emptyList();
        }

        log.info("Found {} tours matching the filter criteria", tours.size());

        return tours.stream()
                .map(tour -> modelMapper.map(tour, TourDto.class))
                .toList();
    }

}

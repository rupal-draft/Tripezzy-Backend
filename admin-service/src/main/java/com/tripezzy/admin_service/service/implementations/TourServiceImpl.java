package com.tripezzy.admin_service.service.implementations;

import com.tripezzy.admin_service.dto.TourDto;
import com.tripezzy.admin_service.entity.Destination;
import com.tripezzy.admin_service.entity.TourPackage;
import com.tripezzy.admin_service.exceptions.*;
import com.tripezzy.admin_service.repository.DestinationRepository;
import com.tripezzy.admin_service.repository.TourRepository;
import com.tripezzy.admin_service.repository.specifications.TourSpecification;
import com.tripezzy.admin_service.service.TourService;
import org.modelmapper.MappingException;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DataAccessException;
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
    private final DestinationRepository destinationRepository;
    private final ModelMapper modelMapper;

    public TourServiceImpl(TourRepository tourRepository, ModelMapper modelMapper,
                           DestinationRepository destinationRepository) {
        this.tourRepository = tourRepository;
        this.modelMapper = modelMapper;
        this.destinationRepository = destinationRepository;
    }

    @Override
    @CacheEvict(value = "destination", allEntries = true)
    @Transactional
    public TourDto createTour(TourDto dto) {
        log.info("Creating new Tour: {}", dto.getName());
        try {
            if (dto.getName() == null || dto.getName().isBlank()) {
                throw new BadRequestException("Tour name is required");
            }
            if (dto.getPrice() == null || dto.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
                throw new BadRequestException("Price must be greater than zero");
            }
            if (dto.getDestinationId() == null) {
                throw new BadRequestException("Destination ID is required");
            }

            if (tourRepository.existsByNameAndDeletedFalse(dto.getName())) {
                throw new RuntimeConflict("Tour with name '" + dto.getName() + "' already exists");
            }

            Destination destination = destinationRepository.findById(dto.getDestinationId())
                    .orElseThrow(() -> {
                        log.warn("Destination not found with ID: {}", dto.getDestinationId());
                        return new ResourceNotFound("Destination not found");
                    });

            TourPackage newTour = modelMapper.map(dto, TourPackage.class);
            newTour.setId(null);
            newTour.setDestination(destination);

            destination.getTourPackages().add(newTour);
            destinationRepository.save(destination);

            return modelMapper.map(newTour, TourDto.class);

        } catch (DataAccessException ex) {
            log.error("Database error while creating tour: {}", ex.getMessage(), ex);
            throw new DataIntegrityViolation("Failed to create tour due to database error");
        } catch (MappingException ex) {
            log.error("Mapping error while creating tour: {}", ex.getMessage(), ex);
            throw new IllegalState("Failed to map tour data");
        }
    }

    @Override
    @Cacheable(value = "tours", key = "#pageable")
    public Page<TourDto> getAllTours(Pageable pageable) {
        log.info("Fetching all tours with pagination");
        try {
            Page<TourPackage> tourPage = tourRepository.findByDeletedFalse(pageable);

            if (tourPage.isEmpty()) {
                log.warn("No tours found in database");
                throw new ResourceNotFound("No tours available");
            }

            return tourPage.map(tour -> {
                try {
                    return modelMapper.map(tour, TourDto.class);
                } catch (MappingException ex) {
                    log.error("Mapping error for tour ID {}: {}", tour.getId(), ex.getMessage());
                    throw new IllegalState("Failed to map tour data");
                }
            });

        } catch (DataAccessException ex) {
            log.error("Database error while fetching tours: {}", ex.getMessage(), ex);
            throw new ServiceUnavailable("Unable to retrieve tours at this time");
        }
    }

    @Override
    @Cacheable(value = "tour", key = "#id", unless = "#result == null")
    public TourDto getTourById(Long id) {
        log.info("Fetching Tour with ID: {}", id);
        if (id == null || id <= 0) {
            throw new BadRequestException("Invalid tour ID");
        }

        try {
            return tourRepository.findById(id)
                    .filter(tour -> !tour.isDeleted())
                    .map(tour -> {
                        try {
                            return modelMapper.map(tour, TourDto.class);
                        } catch (MappingException ex) {
                            log.error("Mapping error for tour ID {}: {}", id, ex.getMessage());
                            throw new IllegalState("Failed to map tour data");
                        }
                    })
                    .orElseThrow(() -> {
                        log.warn("Tour not found or deleted: {}", id);
                        return new ResourceNotFound("Tour not found or deleted");
                    });
        } catch (DataAccessException ex) {
            log.error("Database error while fetching tour ID {}: {}", id, ex.getMessage());
            throw new ServiceUnavailable("Unable to retrieve tour at this time");
        }
    }

    @Override
    @Cacheable(value = "toursByDestination", key = "#destinationId")
    public List<TourDto> getToursByDestination(Long destinationId) {
        log.info("Fetching tours for Destination ID: {}", destinationId);
        if (destinationId == null || destinationId <= 0) {
            throw new BadRequestException("Invalid destination ID");
        }

        try {
            List<TourPackage> tours = tourRepository.findByDestinationIdAndDeletedFalse(destinationId);

            if (tours.isEmpty()) {
                log.warn("No tours found for Destination ID: {}", destinationId);
                throw new ResourceNotFound("No tours found for the given destination");
            }

            return tours.stream()
                    .map(tour -> {
                        try {
                            return modelMapper.map(tour, TourDto.class);
                        } catch (MappingException ex) {
                            log.error("Mapping error for tour ID {}: {}", tour.getId(), ex.getMessage());
                            throw new IllegalState("Failed to map tour data");
                        }
                    })
                    .collect(Collectors.toList());

        } catch (DataAccessException ex) {
            log.error("Database error while fetching tours for destination {}: {}",
                    destinationId, ex.getMessage());
            throw new ServiceUnavailable("Unable to retrieve tours at this time");
        }
    }

    @Override
    @CacheEvict(value = "tour", key = "#id")
    @Transactional
    public TourDto updateTour(Long id, TourDto dto) {
        log.info("Updating Tour ID: {}", id);
        if (id == null || id <= 0) {
            throw new BadRequestException("Invalid tour ID");
        }
        if (dto == null) {
            throw new BadRequestException("Tour data cannot be null");
        }

        try {
            TourPackage tour = tourRepository.findById(id)
                    .filter(t -> !t.isDeleted())
                    .orElseThrow(() -> {
                        log.warn("Tour not found or deleted: {}", id);
                        return new ResourceNotFound("Tour not found or deleted");
                    });

            if (dto.getName() != null && dto.getName().isBlank()) {
                throw new BadRequestException("Tour name cannot be empty");
            }
            if (dto.getPrice() != null && dto.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
                throw new BadRequestException("Price must be greater than zero");
            }

            if (dto.getName() != null && !dto.getName().equals(tour.getName()) &&
                    tourRepository.existsByNameAndDeletedFalse(dto.getName())) {
                throw new RuntimeConflict("Tour with name '" + dto.getName() + "' already exists");
            }

            modelMapper.map(dto, tour);
            TourPackage updatedTour = tourRepository.save(tour);
            return modelMapper.map(updatedTour, TourDto.class);

        } catch (DataAccessException ex) {
            log.error("Database error while updating tour ID {}: {}", id, ex.getMessage(), ex);
            throw new DataIntegrityViolation("Failed to update tour due to database error");
        } catch (MappingException ex) {
            log.error("Mapping error while updating tour ID {}: {}", id, ex.getMessage(), ex);
            throw new IllegalState("Failed to map tour data");
        }
    }

    @Override
    @CacheEvict(value = "tour", key = "#id")
    @Transactional
    public void softDeleteTour(Long id) {
        log.info("Soft deleting Tour ID: {}", id);
        if (id == null || id <= 0) {
            throw new BadRequestException("Invalid tour ID");
        }

        try {
            TourPackage tour = tourRepository.findById(id)
                    .orElseThrow(() -> {
                        log.warn("Tour not found: {}", id);
                        return new ResourceNotFound("Tour not found");
                    });

            if (tour.isDeleted()) {
                log.warn("Attempt to delete already deleted tour: {}", id);
                throw new IllegalState("Tour is already deleted");
            }

            tour.setDeleted(true);
            tourRepository.save(tour);
            log.info("Tour ID {} marked as deleted", id);

        } catch (DataAccessException ex) {
            log.error("Database error while soft-deleting tour ID {}: {}", id, ex.getMessage(), ex);
            throw new DataIntegrityViolation("Failed to delete tour due to database error");
        }
    }

    @Override
    @Cacheable(value = "tours", key = "#keyword != null ? #keyword : 'default'")
    public List<TourDto> searchTours(String keyword) {
        log.info("Searching for tours with keyword: {}", keyword);
        if (keyword == null || keyword.trim().isEmpty()) {
            throw new BadRequestException("Search keyword cannot be empty");
        }

        try {
            List<TourPackage> tours = tourRepository
                    .findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(keyword, keyword);

            if (tours.isEmpty()) {
                log.warn("No tours found for keyword: {}", keyword);
                return Collections.emptyList();
            }

            return tours.stream()
                    .map(tour -> {
                        try {
                            return modelMapper.map(tour, TourDto.class);
                        } catch (MappingException ex) {
                            log.error("Mapping error for tour ID {}: {}", tour.getId(), ex.getMessage());
                            throw new IllegalState("Failed to map tour data");
                        }
                    })
                    .collect(Collectors.toList());

        } catch (DataAccessException ex) {
            log.error("Database error while searching tours: {}", ex.getMessage(), ex);
            throw new ServiceUnavailable("Search service is currently unavailable");
        }
    }

    @Override
    @Cacheable(value = "filteredTours", key = "{#destinationId, #minPrice, #maxPrice, #capacity}")
    public List<TourDto> filterTours(Long destinationId, Double minPrice, Double maxPrice, Integer capacity) {
        log.info("Filtering tours with params - Destination ID: {}, Min Price: {}, Max Price: {}, Capacity: {}",
                destinationId, minPrice, maxPrice, capacity);

        if (minPrice != null && maxPrice != null && minPrice > maxPrice) {
            throw new BadRequestException("Minimum price cannot be greater than maximum price");
        }
        if (minPrice != null && minPrice < 0) {
            throw new BadRequestException("Price cannot be negative");
        }

        try {
            List<TourPackage> tours = tourRepository.findAll(
                    TourSpecification.filterBy(destinationId, capacity, minPrice, maxPrice));

            if (tours.isEmpty()) {
                log.warn("No tours found matching the given filters");
                return Collections.emptyList();
            }

            log.info("Found {} tours matching the filter criteria", tours.size());
            return tours.stream()
                    .map(tour -> {
                        try {
                            return modelMapper.map(tour, TourDto.class);
                        } catch (MappingException ex) {
                            log.error("Mapping error for tour ID {}: {}", tour.getId(), ex.getMessage());
                            throw new IllegalState("Failed to map tour data");
                        }
                    })
                    .collect(Collectors.toList());

        } catch (DataAccessException ex) {
            log.error("Database error while filtering tours: {}", ex.getMessage(), ex);
            throw new ServiceUnavailable("Filter service is currently unavailable");
        }
    }
}

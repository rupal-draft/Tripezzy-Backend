package com.tripezzy.admin_service.service.implementations;

import com.tripezzy.admin_service.dto.DestinationDto;
import com.tripezzy.admin_service.entity.Destination;
import com.tripezzy.admin_service.exceptions.*;
import com.tripezzy.admin_service.repository.DestinationRepository;
import com.tripezzy.admin_service.repository.specifications.DestinationSpecification;
import com.tripezzy.admin_service.service.DestinationService;
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

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DestinationServiceImpl implements DestinationService {

    private static final Logger log = LoggerFactory.getLogger(DestinationServiceImpl.class);
    private final DestinationRepository destinationRepository;
    private final ModelMapper modelMapper;

    public DestinationServiceImpl(DestinationRepository destinationRepository, ModelMapper modelMapper) {
        this.destinationRepository = destinationRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    @CacheEvict(value = "destination", allEntries = true)
    @Transactional
    public DestinationDto createDestination(DestinationDto dto) {
        log.info("Creating new Destination: {}", dto.getName());
        try {
            if (dto.getName() == null || dto.getName().isBlank()) {
                throw new BadRequestException("Destination name is required");
            }
            if (dto.getCountry() == null || dto.getCountry().isBlank()) {
                throw new BadRequestException("Country is required");
            }

            if (destinationRepository.existsByNameAndDeletedFalse(dto.getName())) {
                throw new RuntimeConflict("Destination with name '" + dto.getName() + "' already exists");
            }

            Destination destination = modelMapper.map(dto, Destination.class);
            Destination savedDestination = destinationRepository.save(destination);
            return modelMapper.map(savedDestination, DestinationDto.class);

        } catch (DataAccessException ex) {
            log.error("Database error while creating destination: {}", ex.getMessage(), ex);
            throw new DataIntegrityViolation("Failed to create destination due to database error");
        } catch (MappingException ex) {
            log.error("Mapping error while creating destination: {}", ex.getMessage(), ex);
            throw new IllegalState("Failed to map destination data");
        }
    }

    @Override
    @Cacheable(value = "destinations", key = "#pageable")
    public Page<DestinationDto> getAllDestinations(Pageable pageable) {
        log.info("Fetching all destinations: {}", pageable);
        try {
            Page<Destination> destinations = destinationRepository.findByDeletedFalse(pageable);

            if (destinations.isEmpty()) {
                log.warn("No destinations found in database");
                throw new ResourceNotFound("No destinations available");
            }

            return destinations.map(dest -> {
                try {
                    return modelMapper.map(dest, DestinationDto.class);
                } catch (MappingException ex) {
                    log.error("Mapping error for destination ID {}: {}", dest.getId(), ex.getMessage());
                    throw new IllegalState("Failed to map destination data");
                }
            });

        } catch (DataAccessException ex) {
            log.error("Database error while fetching destinations: {}", ex.getMessage(), ex);
            throw new ServiceUnavailable("Unable to retrieve destinations at this time");
        }
    }

    @Override
    @Cacheable(value = "destination", key = "#id", unless = "#result == null")
    public DestinationDto getDestinationById(Long id) {
        log.info("Fetching Destination ID: {}", id);
        if (id == null || id <= 0) {
            throw new BadRequestException("Invalid destination ID");
        }

        try {
            return destinationRepository.findById(id)
                    .filter(dest -> !dest.isDeleted())
                    .map(dest -> {
                        try {
                            return modelMapper.map(dest, DestinationDto.class);
                        } catch (MappingException ex) {
                            log.error("Mapping error for destination ID {}: {}", id, ex.getMessage());
                            throw new IllegalState("Failed to map destination data");
                        }
                    })
                    .orElseThrow(() -> {
                        log.warn("Destination not found or deleted: {}", id);
                        return new ResourceNotFound("Destination not found or deleted");
                    });
        } catch (DataAccessException ex) {
            log.error("Database error while fetching destination ID {}: {}", id, ex.getMessage());
            throw new ServiceUnavailable("Unable to retrieve destination at this time");
        }
    }

    @Override
    @CacheEvict(value = "destination", key = "#id")
    @Transactional
    public DestinationDto updateDestination(Long id, DestinationDto dto) {
        log.info("Updating Destination ID: {}", id);
        if (id == null || id <= 0) {
            throw new BadRequestException("Invalid destination ID");
        }
        if (dto == null) {
            throw new BadRequestException("Destination data cannot be null");
        }

        try {
            Destination destination = destinationRepository.findById(id)
                    .filter(dest -> !dest.isDeleted())
                    .orElseThrow(() -> {
                        log.warn("Destination not found or deleted: {}", id);
                        return new ResourceNotFound("Destination not found or deleted");
                    });

            if (dto.getName() != null && dto.getName().isBlank()) {
                throw new BadRequestException("Destination name cannot be empty");
            }
            if (dto.getCountry() != null && dto.getCountry().isBlank()) {
                throw new BadRequestException("Country name cannot be empty");
            }

            if (dto.getName() != null && !dto.getName().equals(destination.getName()) &&
                    destinationRepository.existsByNameAndDeletedFalse(dto.getName())) {
                throw new RuntimeConflict("Destination with name '" + dto.getName() + "' already exists");
            }

            modelMapper.map(dto, destination);
            Destination updatedDestination = destinationRepository.save(destination);
            return modelMapper.map(updatedDestination, DestinationDto.class);

        } catch (DataAccessException ex) {
            log.error("Database error while updating destination ID {}: {}", id, ex.getMessage(), ex);
            throw new DataIntegrityViolation("Failed to update destination due to database error");
        } catch (MappingException ex) {
            log.error("Mapping error while updating destination ID {}: {}", id, ex.getMessage(), ex);
            throw new IllegalState("Failed to map destination data");
        }
    }

    @Override
    @CacheEvict(value = "destination", key = "#id")
    @Transactional
    public void softDeleteDestination(Long id) {
        log.info("Soft deleting Destination ID: {}", id);
        if (id == null || id <= 0) {
            throw new BadRequestException("Invalid destination ID");
        }

        try {
            Destination destination = destinationRepository.findById(id)
                    .orElseThrow(() -> {
                        log.warn("Destination not found: {}", id);
                        return new ResourceNotFound("Destination not found");
                    });

            if (destination.isDeleted()) {
                log.warn("Attempt to delete already deleted destination: {}", id);
                throw new IllegalState("Destination is already deleted");
            }

            destination.setDeleted(true);
            destinationRepository.save(destination);
            log.info("Destination ID {} marked as deleted", id);

        } catch (DataAccessException ex) {
            log.error("Database error while soft-deleting destination ID {}: {}", id, ex.getMessage(), ex);
            throw new DataIntegrityViolation("Failed to delete destination due to database error");
        }
    }

    @Override
    @Cacheable(value = "searchDestinations", key = "#keyword")
    public List<DestinationDto> searchDestinations(String keyword) {
        log.info("Searching destinations with keyword: {}", keyword);
        if (keyword == null || keyword.isBlank()) {
            throw new BadRequestException("Search keyword cannot be empty");
        }

        try {
            List<Destination> destinations = destinationRepository
                    .findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(keyword, keyword);

            if (destinations.isEmpty()) {
                log.warn("No destinations found for keyword: {}", keyword);
                throw new ResourceNotFound("No matching destinations found");
            }

            return destinations.stream()
                    .map(dest -> {
                        try {
                            return modelMapper.map(dest, DestinationDto.class);
                        } catch (MappingException ex) {
                            log.error("Mapping error for destination ID {}: {}", dest.getId(), ex.getMessage());
                            throw new IllegalState("Failed to map destination data");
                        }
                    })
                    .collect(Collectors.toList());

        } catch (DataAccessException ex) {
            log.error("Database error while searching destinations: {}", ex.getMessage(), ex);
            throw new ServiceUnavailable("Search service is currently unavailable");
        }
    }

    @Override
    @Cacheable(value = "filteredDestinations", key = "#country")
    public List<DestinationDto> filterDestinations(String country) {
        log.info("Filtering destinations with country: {}", country);
        if (country == null || country.isBlank()) {
            throw new BadRequestException("Country filter cannot be empty");
        }

        try {
            List<Destination> destinations = destinationRepository.findAll(
                    DestinationSpecification.filterBy(country));

            if (destinations.isEmpty()) {
                log.warn("No destinations found for country: {}", country);
                throw new ResourceNotFound("No destinations match the given filters");
            }

            return destinations.stream()
                    .map(dest -> {
                        try {
                            return modelMapper.map(dest, DestinationDto.class);
                        } catch (MappingException ex) {
                            log.error("Mapping error for destination ID {}: {}", dest.getId(), ex.getMessage());
                            throw new IllegalState("Failed to map destination data");
                        }
                    })
                    .collect(Collectors.toList());

        } catch (DataAccessException ex) {
            log.error("Database error while filtering destinations: {}", ex.getMessage(), ex);
            throw new ServiceUnavailable("Filter service is currently unavailable");
        }
    }
}

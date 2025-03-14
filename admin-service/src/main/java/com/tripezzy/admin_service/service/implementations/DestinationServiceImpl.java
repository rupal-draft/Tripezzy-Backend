package com.tripezzy.admin_service.service.implementations;

import com.tripezzy.admin_service.dto.DestinationDto;
import com.tripezzy.admin_service.entity.Destination;
import com.tripezzy.admin_service.exceptions.DataIntegrityViolation;
import com.tripezzy.admin_service.exceptions.IllegalState;
import com.tripezzy.admin_service.exceptions.ResourceNotFound;
import com.tripezzy.admin_service.repository.DestinationRepository;
import com.tripezzy.admin_service.repository.specifications.DestinationSpecification;
import com.tripezzy.admin_service.service.DestinationService;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
    @CacheEvict(value = "destinations", allEntries = true)
    @Transactional
    public DestinationDto createDestination(DestinationDto dto) {
        log.info("Creating new Destination: {}", dto.getName());
        try {
            if (dto.getName() == null || dto.getCountry() == null) {
                throw new IllegalState("Invalid destination data: Name and country are required");
            }

            Destination destination = modelMapper.map(dto, Destination.class);
            return modelMapper.map(destinationRepository.save(destination), DestinationDto.class);
        } catch (DataIntegrityViolation ex) {
            log.error("Database error while creating destination", ex);
            throw new DataIntegrityViolation("Destination creation failed due to database constraints");
        }
    }

    @Override
    @Cacheable(value = "destinations",key = "#pageable")
    public Page<DestinationDto> getAllDestinations(Pageable pageable) {
        log.info("Fetching all destinations");
        Page<Destination> destinations = destinationRepository.findByDeletedFalse(pageable);

        if (destinations.isEmpty()) {
            throw new ResourceNotFound("No destinations found");
        }
        return destinations.map(dest -> modelMapper.map(dest, DestinationDto.class));
    }

    @Override
    @Cacheable(value = "destination", key = "#id", unless = "#result == null")
    public DestinationDto getDestinationById(Long id) {
        log.info("Fetching Destination ID: {}", id);
        return destinationRepository.findById(id)
                .filter(dest -> !dest.isDeleted()) // Ensure it's not deleted
                .map(dest -> modelMapper.map(dest, DestinationDto.class))
                .orElseThrow(() -> new ResourceNotFound("Destination not found or deleted"));
    }

    @Override
    @CacheEvict(value = "destination", key = "#id")
    @Transactional
    public DestinationDto updateDestination(Long id, DestinationDto dto) {
        log.info("Updating Destination ID: {}", id);
        Destination destination = destinationRepository.findById(id)
                .filter(dest -> !dest.isDeleted())
                .orElseThrow(() -> new ResourceNotFound("Destination not found or deleted"));

        if (dto.getCountry() != null && dto.getCountry().isBlank()) {
            throw new IllegalState("Country name cannot be empty");
        }

        modelMapper.map(dto, destination);
        return modelMapper.map(destinationRepository.save(destination), DestinationDto.class);
    }

    @Override
    @CacheEvict(value = "destination", key = "#id")
    public void deleteDestination(Long id) {
        log.info("Deleting Destination ID: {}", id);
        destinationRepository.findById(id)
                .filter(dest -> !dest.isDeleted())
                .orElseThrow(() -> new ResourceNotFound("Destination not found or deleted"));

        destinationRepository.deleteById(id);
    }

    @Override
    @CacheEvict(value = "destination", key = "#id")
    public void softDeleteDestination(Long id) {
        log.info("Soft deleting Destination ID: {}", id);
        Destination destination = destinationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFound("Destination not found"));

        if (destination.isDeleted()) {
            throw new IllegalState("Destination is already deleted");
        }

        destination.setDeleted(true);
        destinationRepository.save(destination);
    }

    @Override
    @Cacheable(value = "searchDestinations", key = "#keyword")
    public List<DestinationDto> searchDestinations(String keyword) {
        log.info("Searching destinations with keyword: {}", keyword);
        List<Destination> destinations = destinationRepository
                .findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(keyword, keyword);

        if (destinations.isEmpty()) {
            log.warn("No destinations found for keyword: {}", keyword);
            throw new ResourceNotFound("No matching destinations found.");
        }

        return destinations.stream()
                .map(dest -> modelMapper.map(dest, DestinationDto.class))
                .toList();
    }

    @Override
    @Cacheable(value = "filteredDestinations", key = "#country, #category, #minPrice, #maxPrice")
    public List<DestinationDto> filterDestinations(String country, String category, Double minPrice, Double maxPrice) {
        log.info("Filtering destinations with country: {}, category: {}, minPrice: {}, maxPrice: {}",
                country, category, minPrice, maxPrice);

        List<Destination> destinations = destinationRepository.findAll(
                DestinationSpecification.filterBy(country, category, minPrice, maxPrice));

        if (destinations.isEmpty()) {
            log.warn("No destinations found for the applied filters.");
            throw new ResourceNotFound("No destinations match the given filters.");
        }

        return destinations.stream()
                .map(dest -> modelMapper.map(dest, DestinationDto.class))
                .toList();
    }
}

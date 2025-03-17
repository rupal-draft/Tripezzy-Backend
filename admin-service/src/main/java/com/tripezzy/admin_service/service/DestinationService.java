package com.tripezzy.admin_service.service;

import com.tripezzy.admin_service.dto.DestinationDto;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface DestinationService {
    DestinationDto createDestination(@Valid DestinationDto dto);

    Page<DestinationDto> getAllDestinations(Pageable pageable);

    DestinationDto getDestinationById(Long id);

    DestinationDto updateDestination(Long id, @Valid DestinationDto dto);

    void softDeleteDestination(Long id);

    List<DestinationDto> searchDestinations(String keyword);

    List<DestinationDto> filterDestinations(String country);
}

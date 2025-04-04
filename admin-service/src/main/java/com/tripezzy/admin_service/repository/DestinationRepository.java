package com.tripezzy.admin_service.repository;

import com.tripezzy.admin_service.entity.Destination;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DestinationRepository extends JpaRepository<Destination, Long> , JpaSpecificationExecutor<Destination> {
    @Cacheable(value = "destination", key = "#id", unless = "#result == null")
    Optional<Destination> findById(Long id);

    @Cacheable(value = "activeDestinations")
    Page<Destination> findByDeletedFalse(Pageable pageable);

    @Cacheable(value = "searchDestinations", key = "#keyword")
    List<Destination> findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(String keyword, String keyword1);

    boolean existsByNameAndDeletedFalse(@NotBlank(message = "Destination name is required") @Size(max = 100, message = "Name must not exceed 100 characters") String name);
}

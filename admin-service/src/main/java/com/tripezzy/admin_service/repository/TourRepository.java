package com.tripezzy.admin_service.repository;

import com.tripezzy.admin_service.entity.TourPackage;
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
public interface TourRepository extends JpaRepository<TourPackage, Long> , JpaSpecificationExecutor<TourPackage> {
    @Cacheable(value = "tour", key = "#id", unless = "#result == null")
    Optional<TourPackage> findById(Long id);

    @Cacheable(value = "activeTours")
    Page<TourPackage> findByDeletedFalse(Pageable pageable);

    @Cacheable(value = "tours", key = "#keyword != null ? #keyword : 'default'")
    List<TourPackage> findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(String name, String description);

    @Cacheable(value = "toursByDestination", key = "#destinationId")
    List<TourPackage> findByDestinationIdAndDeletedFalse(Long destinationId);

    boolean existsByNameAndDeletedFalse(@NotBlank(message = "Name is required") @Size(max = 100, message = "Name must not exceed 100 characters") String name);
}

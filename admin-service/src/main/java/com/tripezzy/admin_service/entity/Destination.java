package com.tripezzy.admin_service.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

import java.util.Set;

@Entity
@Table(name = "destinations", indexes = {
        @Index(name = "idx_destination_name", columnList = "name"),
        @Index(name = "idx_destination_country", columnList = "country")
})
public class Destination extends Auditable{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Destination name is required")
    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @NotBlank(message = "Country is required")
    @Column(nullable = false, length = 50)
    private String country;

    @NotBlank(message = "Description is required")
    @Column(nullable = false, length = 1000)
    private String description;

    @OneToMany(mappedBy = "destination", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<TourPackage> tourPackages;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public @NotBlank(message = "Destination name is required") String getName() {
        return name;
    }

    public void setName(@NotBlank(message = "Destination name is required") String name) {
        this.name = name;
    }

    public @NotBlank(message = "Country is required") String getCountry() {
        return country;
    }

    public void setCountry(@NotBlank(message = "Country is required") String country) {
        this.country = country;
    }

    public @NotBlank(message = "Description is required") String getDescription() {
        return description;
    }

    public void setDescription(@NotBlank(message = "Description is required") String description) {
        this.description = description;
    }

    public Set<TourPackage> getTourPackages() {
        return tourPackages;
    }

    public void setTourPackages(Set<TourPackage> tourPackages) {
        this.tourPackages = tourPackages;
    }
}

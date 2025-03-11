package com.tripezzy.user_service.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "guides", indexes = {
    @Index(name = "idx_guide_user_id", columnList = "user_id")
})
public class Guide extends Auditable{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Languages spoken are required")
    @Column(nullable = false)
    private String languagesSpoken;

    @NotBlank(message = "Experience details are required")
    @Column(nullable = false, length = 1000)
    private String experience;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public @NotBlank(message = "Languages spoken are required") String getLanguagesSpoken() {
        return languagesSpoken;
    }

    public void setLanguagesSpoken(@NotBlank(message = "Languages spoken are required") String languagesSpoken) {
        this.languagesSpoken = languagesSpoken;
    }

    public @NotBlank(message = "Experience details are required") String getExperience() {
        return experience;
    }

    public void setExperience(@NotBlank(message = "Experience details are required") String experience) {
        this.experience = experience;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}

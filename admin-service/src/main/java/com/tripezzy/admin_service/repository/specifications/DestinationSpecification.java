package com.tripezzy.admin_service.repository.specifications;

import com.tripezzy.admin_service.entity.Destination;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class DestinationSpecification {
    public static Specification<Destination> filterBy(String country) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (country != null) {
                predicates.add(criteriaBuilder.equal(root.get("country"), country));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}

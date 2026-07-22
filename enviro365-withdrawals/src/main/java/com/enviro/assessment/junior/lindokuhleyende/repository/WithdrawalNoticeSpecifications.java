package com.enviro.assessment.junior.lindokuhleyende.repository;

import com.enviro.assessment.junior.lindokuhleyende.entity.WithdrawalNotice;
import com.enviro.assessment.junior.lindokuhleyende.entity.enums.WithdrawalStatus;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

/**
 * Builds dynamic JPA Specifications used to filter withdrawal notices
 * for both the history endpoint and the CSV export endpoint.
 */
public final class WithdrawalNoticeSpecifications {

    private WithdrawalNoticeSpecifications() {
    }

    public static Specification<WithdrawalNotice> withFilters(Long investorId,
                                                                Long productId,
                                                                WithdrawalStatus status,
                                                                LocalDateTime from,
                                                                LocalDateTime to) {
        return (root, query, cb) -> {
            var predicates = cb.conjunction();

            if (investorId != null) {
                predicates = cb.and(predicates, cb.equal(root.get("investor").get("id"), investorId));
            }
            if (productId != null) {
                predicates = cb.and(predicates, cb.equal(root.get("product").get("id"), productId));
            }
            if (status != null) {
                predicates = cb.and(predicates, cb.equal(root.get("status"), status));
            }
            if (from != null) {
                predicates = cb.and(predicates, cb.greaterThanOrEqualTo(root.get("requestDate"), from));
            }
            if (to != null) {
                predicates = cb.and(predicates, cb.lessThanOrEqualTo(root.get("requestDate"), to));
            }
            return predicates;
        };
    }
}

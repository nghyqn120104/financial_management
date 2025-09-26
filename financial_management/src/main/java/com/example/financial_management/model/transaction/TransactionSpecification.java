package com.example.financial_management.model.transaction;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.domain.Specification;

import com.example.financial_management.entity.Transaction;

import jakarta.persistence.criteria.Predicate;

public class TransactionSpecification {
    public static Specification<Transaction> filter(UUID userId,
            TransactionFilterRequest request) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Lọc theo user
            predicates.add(cb.equal(root.get("userId"), userId));

            if (request.getFromDate() != null) {
                predicates.add(cb.greaterThanOrEqualTo(
                        root.get("createdAt"),
                        request.getFromDate().atStartOfDay() // 00:00:00
                ));
            }
            if (request.getToDate() != null) {
                predicates.add(cb.lessThanOrEqualTo(
                        root.get("createdAt"),
                        request.getToDate().atTime(23, 59, 59) // 23:59:59
                ));
            }

            if (request.getCategory() != 0) { // giả sử 0 = không lọc
                predicates.add(cb.equal(root.get("category"), request.getCategory()));
            }
            if (request.getType() != 0) { // giả sử 0 = không lọc
                predicates.add(cb.equal(root.get("type"), request.getType()));
            }
            if (request.getMinAmount() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("amount"), request.getMinAmount()));
            }
            if (request.getMaxAmount() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("amount"), request.getMaxAmount()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}

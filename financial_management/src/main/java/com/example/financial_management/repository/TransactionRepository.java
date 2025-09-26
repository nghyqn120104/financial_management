package com.example.financial_management.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.example.financial_management.entity.Transaction;

public interface TransactionRepository extends JpaRepository<Transaction, UUID>, JpaSpecificationExecutor<Transaction> {
    Page<Transaction> findByUserId(UUID userId, Pageable pageable);

    Optional<Transaction> findByIdAndUserId(UUID id, UUID userId);

    List<Transaction> findAllByUserIdAndType(UUID userId, int type);

    List<Transaction> findAllByAccountId(UUID accountId);

    List<Transaction> findAllByUserIdAndCategory(UUID userId, int category);

    List<Transaction> findAllByUserIdAndCurrency(UUID userId, int currency);

    boolean existsByAccountIdAndCurrencyNot(UUID accountId, int currency);

    Page<Transaction> findByAccountIdAndUserId(UUID accountId, UUID userId, Pageable pageable);
}

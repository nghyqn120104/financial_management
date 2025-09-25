package com.example.financial_management.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.financial_management.entity.Transaction;
import com.example.financial_management.model.transaction.TransactionResponse;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> {
    List<TransactionResponse> findByUserId(UUID userId);

    Optional<Transaction> findByIdAndUserId(UUID id, UUID userId);

    List<TransactionResponse> findAllByUserIdAndType(UUID userId, int type);

    List<TransactionResponse> findAllByAccountId(UUID accountId);

    List<TransactionResponse> findAllByUserIdAndCategory(UUID userId, int category);

    List<TransactionResponse> findAllByUserIdAndCurrency(UUID userId, int currency);

    boolean existsByAccountIdAndCurrencyNot(UUID accountId, int currency);

}

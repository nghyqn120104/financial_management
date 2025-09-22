package com.example.financial_management.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.financial_management.entity.Transaction;
import com.example.financial_management.model.transaction.TransactionResponse;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> {
    List<TransactionResponse> findByUserId(UUID userId);

    List<TransactionResponse> findByAccountId(UUID accountId);

    Optional<Transaction> findByIdAndUserId(UUID id, UUID userId);
}

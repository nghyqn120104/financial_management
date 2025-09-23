package com.example.financial_management.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.financial_management.entity.Account;

public interface AccountRepository extends JpaRepository<Account, UUID> {
    Optional<Account> findByIdAndUserId(UUID id, UUID userId);

    List<Account> findAllByUserIdAndStatus(UUID userId, int status);

    List<Account> findAllByCurrency(int currency);

    Optional<Account> findByIdAndStatus(UUID id, int status);
}

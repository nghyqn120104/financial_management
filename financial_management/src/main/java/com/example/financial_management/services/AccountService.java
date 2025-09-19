package com.example.financial_management.services;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.financial_management.entity.Account;
import com.example.financial_management.entity.User;
import com.example.financial_management.mapper.AccountMapper;
import com.example.financial_management.model.account.AccountRequest;
import com.example.financial_management.model.account.AccountResponse;
import com.example.financial_management.model.account.AccountStatus;
import com.example.financial_management.model.auth.Auth;
import com.example.financial_management.repository.AccountRepository;
import com.example.financial_management.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AccountService {
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final AccountMapper accountMapper;

    @Transactional
    public AccountResponse createAccount(AccountRequest request, Auth auth) {
        User user = validateUser(auth);

        Account account = accountMapper.toEntity(request, user.getId());
        Account saved = accountRepository.save(account);

        return accountMapper.toResponse(saved);
    }

    @Transactional
    public AccountResponse updateAccount(UUID accountId, AccountRequest request, Auth auth) {
        User user = validateUser(auth);

        Account account = accountRepository.findByIdAndUserId(accountId, user.getId())
                .orElseThrow(() -> new RuntimeException("Account not found"));

        // Cập nhật các field từ request
        account.setName(request.getName());
        account.setType(request.getType());
        account.setCurrency(request.getCurrency());
        account.setDescription(request.getDescription());

        Account saved = accountRepository.saveAndFlush(account);

        return accountMapper.toResponse(saved);
    }

    @Transactional
    public AccountResponse updateStatusAccount(AccountStatus accountStatus, Auth auth) {
        User user = validateUser(auth);

        Account account = accountRepository.findByIdAndUserId(accountStatus.getId(), user.getId())
                .orElseThrow(() -> new RuntimeException("Account not found"));

        account.setStatus(accountStatus.getStatus());
        Account saved = accountRepository.saveAndFlush(account);

        return accountMapper.toResponse(saved);
       
    }

    @Transactional
    public boolean deleteAccount(UUID accountId, Auth auth) {
        User user = validateUser(auth);

        Account account = accountRepository.findByIdAndUserId(accountId, user.getId())
                .orElseThrow(() -> new RuntimeException("Account not found"));

        accountRepository.delete(account);
        return true;
    }

    private User validateUser(Auth auth) {
        return userRepository.findById(UUID.fromString(auth.getId()))
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

}
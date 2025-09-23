package com.example.financial_management.services;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.example.financial_management.constant.Status;
import com.example.financial_management.entity.Account;
import com.example.financial_management.entity.User;
import com.example.financial_management.mapper.AccountMapper;
import com.example.financial_management.model.account.AccountRequest;
import com.example.financial_management.model.account.AccountResponse;
import com.example.financial_management.model.account.AccountStatus;
import com.example.financial_management.model.auth.Auth;
import com.example.financial_management.model.transaction.TransactionRequest;
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

        // đảm bảo initialBalance không null
        BigDecimal initial = request.getInitialBalance() != null
                ? request.getInitialBalance()
                : BigDecimal.ZERO;

        account.setBalance(initial);

        Account saved = accountRepository.save(account);

        return accountMapper.toResponse(saved);
    }

    @Transactional
    public AccountResponse updateAccount(UUID accountId, AccountRequest request, Auth auth) {
        Account account = validateAccount(accountId, auth, Status.ACTIVE);

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
        Account account = validateAccount(accountStatus.getId(), auth, Status.ACTIVE);
        // Cập nhật trạng thái

        account.setStatus(accountStatus.getStatus());
        Account saved = accountRepository.saveAndFlush(account);

        return accountMapper.toResponse(saved);

    }

    @Transactional
    public boolean deleteAccount(UUID accountId, Auth auth) {
        Account account = validateAccount(accountId, auth, Status.ACTIVE);

        accountRepository.delete(account);
        return true;
    }

    private User validateUser(Auth auth) {
        return userRepository.findById(UUID.fromString(auth.getId()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    public Account validateAccount(UUID accountId, Auth auth, int status) {
        User user = validateUser(auth);

        if(accountRepository.findByIdAndUserId(accountId, user.getId()).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found or inactive");
        }else {
            return accountRepository.findByIdAndStatus(accountId, status)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found or inactive"));
        }
    }

    public void updateAccountBalance(Account account, TransactionRequest request) {
        BigDecimal amount = request.getAmount();

        if (request.getType() == 1) {
            // Thu nhập
            account.setBalance(account.getBalance().add(amount));
        } else {
            // Chi tiêu
            account.setBalance(account.getBalance().subtract(amount));
        }

        accountRepository.save(account);
    }

    public AccountResponse getAccountById(UUID accountId, Auth auth) {
        Account account = validateAccount(accountId, auth, Status.ACTIVE);

        return accountMapper.toResponse(account);
    }

    public List<AccountResponse> getAllAccounts(Auth auth) {
        User user = validateUser(auth);
        List<Account> accounts = accountRepository.findAllByUserIdAndStatus(user.getId(), Status.ACTIVE);
        return accounts.stream()
                .map(accountMapper::toResponse)
                .toList();
    }

}
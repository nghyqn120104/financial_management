package com.example.financial_management.controllers;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.financial_management.model.AbstractResponse;
import com.example.financial_management.model.account.AccountRequest;
import com.example.financial_management.model.account.AccountResponse;
import com.example.financial_management.model.account.AccountStatus;
import com.example.financial_management.model.auth.Auth;
import com.example.financial_management.services.AccountService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/accounts")
@RequiredArgsConstructor
@Tag(name = "Account API", description = "Account Management")
public class AccountController {
    private final AccountService accountService;

    @PostMapping("/create")
    public ResponseEntity<AbstractResponse<AccountResponse>> createAccount(@RequestBody AccountRequest request,
            @AuthenticationPrincipal Auth auth) {
        return new AbstractResponse<AccountResponse>().withData(() -> accountService.createAccount(request, auth));
    }

    @PostMapping("/{id}")
    public ResponseEntity<AbstractResponse<AccountResponse>> updateAccount(@PathVariable("id") UUID accountId,
            @RequestBody AccountRequest request, @AuthenticationPrincipal Auth auth) {
        return new AbstractResponse<AccountResponse>()
                .withData(() -> accountService.updateAccount(accountId, request, auth));
    }

    @PostMapping("/{id}/status")
    public ResponseEntity<AbstractResponse<AccountResponse>> updateAccountStatus(
            @RequestBody AccountStatus accountStatus,
            @AuthenticationPrincipal Auth auth) {
        return new AbstractResponse<AccountResponse>()
                .withData(() -> accountService.updateStatusAccount(accountStatus, auth));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<AbstractResponse<Boolean>> deleteAccount(@PathVariable("id") UUID accountId,
            @AuthenticationPrincipal Auth auth) {
        return new AbstractResponse<Boolean>().withData(() -> accountService.deleteAccount(accountId, auth));
    }

    @PostMapping("/all")
    public ResponseEntity<AbstractResponse<List<AccountResponse>>> getAllAccounts(
            @AuthenticationPrincipal Auth auth) {
        return new AbstractResponse<List<AccountResponse>>()
                .withData(() -> accountService.getAllAccounts(auth));
    }
}

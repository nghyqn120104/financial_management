package com.example.financial_management.controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.financial_management.model.AbstractResponse;
import com.example.financial_management.model.auth.Auth;
import com.example.financial_management.model.transaction.TransactionRequest;
import com.example.financial_management.model.transaction.TransactionResponse;
import com.example.financial_management.model.transaction.TransactionUpdateResponse;
import com.example.financial_management.services.TransactionService;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@RequestMapping("/transactions")
@RequiredArgsConstructor
@Tag(name = "Transaction API", description = "Transaction Management")
public class TransactionController {
    private final TransactionService transactionService;

    // Các endpoint khác sẽ được thêm vào đây
    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AbstractResponse<TransactionResponse>> createTransaction(
            @ModelAttribute TransactionRequest request,
            @RequestPart(value = "file", required = false) MultipartFile file,
            @AuthenticationPrincipal Auth auth) {
        return new AbstractResponse<TransactionResponse>()
                .withData(() -> transactionService.createTransaction(request, auth, file));
    }

    @PostMapping(value = "/{id}")
    public ResponseEntity<AbstractResponse<TransactionUpdateResponse>> updateTransaction(
            @PathVariable("id") UUID transactionId,
            @RequestBody TransactionRequest request,
            @AuthenticationPrincipal Auth auth) {
        return new AbstractResponse<TransactionUpdateResponse>()
                .withData(() -> transactionService.updateTransaction(request, auth, transactionId));
    }
    

}

package com.example.financial_management.model.transaction;

import java.util.UUID;

import lombok.Data;

@Data
public class TransferRequest {
    private UUID transactionId;
    private UUID accountId;
}

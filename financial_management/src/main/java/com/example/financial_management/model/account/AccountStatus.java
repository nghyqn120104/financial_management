package com.example.financial_management.model.account;

import java.util.UUID;

import lombok.Data;

@Data
public class AccountStatus {
    private UUID id;
    private int status;
}

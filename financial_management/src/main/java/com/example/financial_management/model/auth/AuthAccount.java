package com.example.financial_management.model.auth;

import java.util.UUID;

import lombok.Data;

@Data
public class AuthAccount {
    private UUID id;
    private String name;
    private int status;
}

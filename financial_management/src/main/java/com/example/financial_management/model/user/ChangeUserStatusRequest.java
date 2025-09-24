package com.example.financial_management.model.user;

import java.util.UUID;

import lombok.Data;

@Data
public class ChangeUserStatusRequest {
    private UUID userId;
    private int status;
}

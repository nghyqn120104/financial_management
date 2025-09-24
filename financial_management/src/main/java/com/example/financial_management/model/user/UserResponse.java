package com.example.financial_management.model.user;

import java.util.UUID;

import lombok.Data;

@Data
public class UserResponse {
    private UUID id;
    private String name;
    private String email;
    private int status;
    private int role;
}

package com.example.financial_management.model.user;

import lombok.Data;

@Data
public class UserResponse {
    private String name;
    private String email;
    private int status;
}

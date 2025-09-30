package com.example.financial_management.model.auth;

import com.example.financial_management.constant.Role;
import com.example.financial_management.constant.Status;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class Auth {
    private String id;
    private String name;
    private String email;
    private int role; // vai trò
    private int status; // trạng thái user
    private List<AuthAccount> accounts;

    public boolean isActive() {
        return status == Status.ACTIVE;
    }

    public boolean isAdmin() {
        return role == Role.ADMIN;
    }

    public UUID getUUID() {
        return UUID.fromString(id);
    }
}

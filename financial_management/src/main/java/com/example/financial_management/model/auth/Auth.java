package com.example.financial_management.model.auth;

import com.example.financial_management.constant.Profile;
import com.example.financial_management.constant.Status;
import lombok.Data;

import java.util.UUID;

@Data
public class Auth {
    private String id;
    private String name;
    private String email;
    private int profile; // vai trò
    private int status; // trạng thái user

    public boolean isActive() {
        return status == Status.ACTIVE;
    }

    public boolean isOrganizer() {
        return profile == Profile.ORGANIZER;
    }

    public UUID getUUID() {
        return UUID.fromString(id);
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }
}

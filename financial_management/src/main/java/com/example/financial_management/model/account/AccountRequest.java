package com.example.financial_management.model.account;

import lombok.Data;
@Data
public class AccountRequest {
    private String name;
    private int type;
    private int currency;
    private String description;
}

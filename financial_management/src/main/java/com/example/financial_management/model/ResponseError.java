package com.example.financial_management.model;

import lombok.Data;

@Data
public class ResponseError {
    private String message;
    private String stackTrace;
}

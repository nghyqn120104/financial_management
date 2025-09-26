package com.example.financial_management.model.transaction;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TransactionDateRequest extends TransactionRequest {
    private LocalDateTime createAt;
}

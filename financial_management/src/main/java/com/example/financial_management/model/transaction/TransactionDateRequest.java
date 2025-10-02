package com.example.financial_management.model.transaction;

import java.time.OffsetDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TransactionDateRequest extends TransactionRequest {
    private OffsetDateTime createAt;
}

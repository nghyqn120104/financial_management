package com.example.financial_management.model.transaction;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TransactionUpdateResponse extends TransactionResponse {
    private BigDecimal difference;
}

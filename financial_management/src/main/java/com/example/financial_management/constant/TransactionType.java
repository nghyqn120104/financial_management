package com.example.financial_management.constant;

public class TransactionType {
    public static final int EXPENSE = 0;
    public static final int INCOME = 1;
    public static final int TRANSFER = 2;

    private TransactionType() {
        // Utility class
    }

    public static String getName(int type) {
        switch (type) {
            case EXPENSE:
                return "Expense";
            case INCOME:
                return "Income";
            case TRANSFER:
                return "Transfer";
            default:
                return "Unknown";
        }
    }
}

package com.example.financial_management.mapper;

import java.util.UUID;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import com.example.financial_management.entity.Transaction;
import com.example.financial_management.model.transaction.TransactionRequest;
import com.example.financial_management.model.transaction.TransactionResponse;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface TransactionMapper {
    @Mapping(target = "userId", source = "userId")
    Transaction toEntity(TransactionRequest request, UUID userId);

    TransactionResponse toResponse(Transaction entity);
}

package com.example.financial_management.mapper;

import java.util.UUID;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import com.example.financial_management.entity.Account;
import com.example.financial_management.model.account.AccountRequest;
import com.example.financial_management.model.account.AccountResponse;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface AccountMapper {
    AccountResponse toResponse(Account request);

    Account toEntity(AccountRequest request, UUID userId);
}

package com.example.financial_management.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import com.example.financial_management.entity.User;
import com.example.financial_management.model.user.UserResponse;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserMapper {
    UserResponse toResponse(User user);
}

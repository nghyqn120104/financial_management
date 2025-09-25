package com.example.financial_management.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PageResponse<T> {
    private List<T> items; // danh sách phần tử
    private int page; // trang hiện tại (1-based index)
    private int size; // số phần tử trên 1 trang
    private long totalElements; // tổng số bản ghi
    private int totalPages; // tổng số trang
}

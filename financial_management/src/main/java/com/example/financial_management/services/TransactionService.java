package com.example.financial_management.services;

import com.example.financial_management.constant.Category;
import com.example.financial_management.constant.Status;
import com.example.financial_management.constant.TransactionType;
import com.example.financial_management.entity.Account;
import com.example.financial_management.entity.Transaction;
import com.example.financial_management.entity.User;
import com.example.financial_management.mapper.TransactionMapper;
import com.example.financial_management.model.PageResponse;
import com.example.financial_management.model.auth.Auth;
import com.example.financial_management.model.transaction.TransactionRequest;
import com.example.financial_management.model.transaction.TransactionResponse;
import com.example.financial_management.model.transaction.TransactionUpdateResponse;
import com.example.financial_management.repository.AccountRepository;
import com.example.financial_management.repository.TransactionRepository;
import com.example.financial_management.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final TransactionMapper transactionMapper;
    private final UserRepository userRepository;
    private final AccountService accountService;
    private final AccountRepository accountRepository;
    @Value("${app.upload.dir}")
    private String uploadDir;

    public PageResponse<TransactionResponse> getAllTransactions(Auth auth, Pageable pageable) {
        User user = getUser(auth);

        Page<TransactionResponse> pageResult = transactionRepository.findByUserId(user.getId(), pageable)
                .map(transactionMapper::toResponse);

        PageResponse<TransactionResponse> response = new PageResponse<>(
                pageResult.getContent(),
                pageResult.getNumber() + 1, // cộng 1 vì Page mặc định bắt đầu từ 0
                pageResult.getSize(),
                pageResult.getTotalElements(),
                pageResult.getTotalPages());

        return response;
    }

    public TransactionResponse getById(UUID id, Auth auth) {
        User user = getUser(auth);
        return transactionRepository.findByIdAndUserId(id, user.getId())
                .map(transactionMapper::toResponse)
                .orElse(null);
    }

    public PageResponse<TransactionResponse> getTransactionByAccount(UUID accountId, Auth auth, Pageable pageable) {
        User user = getUser(auth);
        Account account = accountService.validateAccount(accountId, auth, Status.ACTIVE);
        Page<TransactionResponse> pageResult = transactionRepository
                .findByAccountIdAndUserId(account.getId(), user.getId(), pageable)
                .map(transactionMapper::toResponse);

        PageResponse<TransactionResponse> response = new PageResponse<>(
                pageResult.getContent(),
                pageResult.getNumber() + 1, // cộng 1 vì Page mặc định bắt đầu từ 0
                pageResult.getSize(),
                pageResult.getTotalElements(),
                pageResult.getTotalPages());

        return response;
    }

    @Transactional
    public TransactionResponse createTransaction(TransactionRequest request, Auth auth, MultipartFile file) {
        Account account = accountService.validateAccount(request.getAccountId(), auth, Status.ACTIVE);

        validateCurrency(request, account);
        validateCategory(request);

        // Tạo transaction
        Transaction transaction = transactionMapper.toEntity(request, account.getUserId());

        // Kiểm tra nếu client muốn có ảnh (haveImage = true) thì mới xử lý file
        if (request.isHaveImage()) {
            if (file != null && !file.isEmpty()) {
                String imagePath = saveImage(file);
                transaction.setImagePath(imagePath);
            } else {
                throw new RuntimeException("Image file is required when haveImage = true");
            }
        } else {
            transaction.setImagePath(null); // đảm bảo clear path nếu không có ảnh
        }

        // Cập nhật số dư account
        accountService.updateAccountBalance(account, request);

        // Lưu transaction trước
        Transaction saved = transactionRepository.save(transaction);

        return transactionMapper.toResponse(saved);
    }

    @Transactional
    public TransactionUpdateResponse updateTransaction(TransactionRequest updated, Auth auth, UUID transactionId) {
        Account account = accountService.validateAccount(updated.getAccountId(), auth, Status.ACTIVE);

        Transaction transaction = transactionRepository.findByIdAndUserId(transactionId, account.getUserId())
                .orElseThrow(() -> new RuntimeException("Transaction not found or access denied"));

        // Giữ lại amount cũ
        BigDecimal oldAmount = transaction.getAmount();

        // Cập nhật transaction
        transaction.setAmount(updated.getAmount());
        transaction.setDescription(updated.getDescription());
        transaction.setType(updated.getType());
        transaction.setAccountId(updated.getAccountId());
        transaction.setUserId(UUID.fromString(auth.getId()));
        transaction.setUpdatedAt(LocalDateTime.now());

        Transaction saved = transactionRepository.save(transaction);

        // Tính chênh lệch
        BigDecimal difference = updated.getAmount().subtract(oldAmount);

        // Cập nhật balance account
        account.setBalance(account.getBalance().add(difference));
        accountRepository.save(account);

        // Trả response có thêm difference
        TransactionUpdateResponse response = transactionMapper.toUpdateResponse(saved);
        response.setDifference(difference);
        return response;
    }

    public void delete(UUID id, Auth auth) {
        User user = getUser(auth);
        Transaction transaction = transactionRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new RuntimeException("Transaction not found or access denied"));
        transactionRepository.delete(transaction);
    }

    private User getUser(Auth auth) {
        return userRepository.findByIdAndStatus(UUID.fromString(auth.getId()), Status.ACTIVE)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private void validateCurrency(TransactionRequest request, Account account) {
        if (request.getCurrency() != account.getCurrency()) {
            throw new RuntimeException("Transaction currency does not match account currency");
        }
    }

    private void validateCategory(TransactionRequest request) {
        int type = request.getType();
        int category = request.getCategory();

        if (type == TransactionType.EXPENSE) {
            if (category < Category.FOOD || category > Category.OTHER_EXPENSE) {
                throw new RuntimeException("Invalid category for EXPENSE transaction");
            }
        } else if (type == TransactionType.INCOME) {
            if (category < Category.SALARY || category > Category.OTHER_INCOME) {
                throw new RuntimeException("Invalid category for INCOME transaction");
            }
        } else if (type == TransactionType.TRANSFER) {
            if (category != Category.TRANSFER) {
                throw new RuntimeException("Invalid category for TRANSFER transaction");
            }
        } else {
            throw new RuntimeException("Unknown transaction type: " + type);
        }
    }

    private String saveImage(MultipartFile file) {
        try {
            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path path = Paths.get(uploadDir + fileName);
            Files.createDirectories(path.getParent());
            Files.write(path, file.getBytes());

            // Trả về path để FE dùng (có thể đổi thành full URL nếu cần)
            return uploadDir + fileName;
        } catch (Exception e) {
            throw new RuntimeException("Upload file thất bại", e);
        }
    }
}

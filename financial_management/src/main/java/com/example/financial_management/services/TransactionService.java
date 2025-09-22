package com.example.financial_management.services;

import com.example.financial_management.entity.Account;
import com.example.financial_management.entity.Transaction;
import com.example.financial_management.entity.User;
import com.example.financial_management.mapper.TransactionMapper;
import com.example.financial_management.model.auth.Auth;
import com.example.financial_management.model.transaction.TransactionRequest;
import com.example.financial_management.model.transaction.TransactionResponse;
import com.example.financial_management.repository.AccountRepository;
import com.example.financial_management.repository.TransactionRepository;
import com.example.financial_management.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final TransactionMapper transactionMapper;
    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final String uploadDir = "uploads/";

    public List<TransactionResponse> getAllTransactions(Auth auth) {
        User user = getUser(auth);
        return transactionRepository.findByUserId(user.getId());
    }

    public TransactionResponse getById(UUID id, Auth auth) {
        User user = getUser(auth);
        return transactionRepository.findByIdAndUserId(id, user.getId())
                .map(transactionMapper::toResponse)
                .orElse(null);
    }

    @Transactional
    public TransactionResponse createTransaction(TransactionRequest request, Auth auth, MultipartFile file) {
        User user = getUser(auth);
        Account account = accountRepository.findByIdAndUserId(request.getAccountId(), user.getId())
                .orElseThrow(() -> new RuntimeException("Account not found"));

        // Tạo transaction
        Transaction transaction = transactionMapper.toEntity(request, user.getId());
        transaction.setCreatedAt(LocalDateTime.now());
        transaction.setUpdatedAt(LocalDateTime.now());

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

        // Lưu transaction trước
        Transaction saved = transactionRepository.save(transaction);

        // Cập nhật số dư account
        updateAccountBalance(account, request);

        return transactionMapper.toResponse(saved);
    }

    public TransactionResponse update(TransactionRequest updated, Auth auth, UUID id) {
        Transaction transaction = transactionRepository.findByIdAndUserId(id, UUID.fromString(auth.getId()))
                .orElseThrow(() -> new RuntimeException("Transaction not found or access denied"));
        transaction.setAmount(updated.getAmount());
        transaction.setDescription(updated.getDescription());
        transaction.setType(updated.getType());
        transaction.setAccountId(updated.getAccountId());
        transaction.setUserId(UUID.fromString(auth.getId()));
        transaction.setUpdatedAt(LocalDateTime.now());
        Transaction saved = transactionRepository.save(transaction);
        return transactionMapper.toResponse(saved);
    }

    public void delete(UUID id, Auth auth) {
        User user = getUser(auth);
        Transaction transaction = transactionRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new RuntimeException("Transaction not found or access denied"));
        transactionRepository.delete(transaction);
    }

    public User getUser(Auth auth) {
        return userRepository.findById(UUID.fromString(auth.getId()))
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public void updateAccountBalance(Account account, TransactionRequest request) {
        BigDecimal amount = request.getAmount();

        if (request.getType() == 1) {
            // Thu nhập
            account.setBalance(account.getBalance().add(amount));
        } else {
            // Chi tiêu
            account.setBalance(account.getBalance().subtract(amount));
        }

        accountRepository.save(account);
    }

    private String saveImage(MultipartFile file) {
        try {
            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path path = Paths.get(uploadDir + fileName);
            Files.createDirectories(path.getParent());
            Files.write(path, file.getBytes());

            // Trả về path để FE dùng (có thể đổi thành full URL nếu cần)
            return "/uploads/" + fileName;
        } catch (Exception e) {
            throw new RuntimeException("Upload file thất bại", e);
        }
    }
}

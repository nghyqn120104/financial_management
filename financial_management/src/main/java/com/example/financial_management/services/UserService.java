package com.example.financial_management.services;

import com.example.financial_management.model.auth.Auth;
import com.example.financial_management.model.user.LoginRequest;
import com.example.financial_management.model.user.UserResponse;
import com.example.financial_management.model.user.UserSignUpRequest;
import com.example.financial_management.entity.User;
import com.example.financial_management.mapper.UserMapper;
import com.example.financial_management.repository.UserRepository;
import com.example.financial_management.util.JwtTokenUtil;
import lombok.RequiredArgsConstructor;

import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;
import com.example.financial_management.constant.Status;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final JwtTokenUtil jwtTokenUtil;
    private final UserMapper userMapper;

    public UserResponse signUp(UserSignUpRequest request) {

        validateSignUp(request);

        String salt = BCrypt.gensalt();
        String passwordHash = BCrypt.hashpw(request.getPassword(), salt);

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPasswordSalt(salt);
        user.setPasswordHash(passwordHash);
        user.setStatus(Status.ACTIVE); // active

        User saved = userRepository.save(user);
        return userMapper.toResponse(saved);
    }

    public void validateSignUp(UserSignUpRequest request) {

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists");
        }

        if (request.getPassword() == null || request.getPassword().isEmpty()) {
            throw new RuntimeException("Password is required");
        }

        if (request.getConfirmPassword() == null || request.getConfirmPassword().isEmpty()) {
            throw new RuntimeException("Confirm password is required");
        }

        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("Password and confirm password do not match");
        }
    }

    public String login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        validateUser(user);
        validatePassword(user, request.getPassword());

        return generateToken(user);
    }

    private String generateToken(User user) {
        Auth auth = new Auth();
        auth.setId(user.getId().toString());
        auth.setName(user.getName());
        auth.setEmail(user.getEmail());
        auth.setStatus(user.getStatus());

        return jwtTokenUtil.generateToken(auth);
    }

    public void validateUser(User user) {
        if (user.getStatus() != Status.ACTIVE) {
            throw new RuntimeException("User is not active");
        }
    }

    public void validatePassword(User user, String password) {
        if (password == null || password.isEmpty()) {
            throw new RuntimeException("Password is required");
        }

        String hashedPassword = BCrypt.hashpw(password, user.getPasswordSalt());
        if (!hashedPassword.equals(user.getPasswordHash())) {
            throw new RuntimeException("Invalid password");
        }
    }

    public UserResponse getCurrentUser(Auth auth) {
        return userRepository.findById(auth.getUUID())
                .map(userMapper::toResponse)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user"));
    }
}

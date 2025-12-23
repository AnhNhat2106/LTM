package com.mathgame.mathgame.service;

import com.mathgame.mathgame.dto.RegisterRequest;
import com.mathgame.mathgame.entity.User;
import com.mathgame.mathgame.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class UserService {

    private final UserRepository repo;
    private final PasswordEncoder encoder;
    private final EmailOtpService otpService;

    public UserService(UserRepository repo, PasswordEncoder encoder, EmailOtpService otpService) {
        this.repo = repo;
        this.encoder = encoder;
        this.otpService = otpService;
    }

    public void register(RegisterRequest req) {
        if (req.getUsername() == null || req.getUsername().trim().isEmpty()) {
            throw new RuntimeException("Username is required");
        }
        if (req.getEmail() == null || req.getEmail().trim().isEmpty()) {
            throw new RuntimeException("Email is required");
        }
        if (req.getPassword() == null || req.getPassword().isEmpty()) {
            throw new RuntimeException("Password is required");
        }
        if (!req.getPassword().equals(req.getConfirmPassword())) {
            throw new RuntimeException("Confirm password does not match");
        }

        String username = req.getUsername().trim();
        String email = req.getEmail().trim().toLowerCase(Locale.ROOT);

        otpService.verifyOtp(email, EmailOtpService.PURPOSE_REGISTER, req.getOtpCode());

        if (repo.existsByUsername(username)) {
            throw new RuntimeException("Username already exists");
        }
        if (repo.existsByEmail(email)) {
            throw new RuntimeException("Email already exists");
        }

        User u = new User();
        u.setUsername(username);
        u.setEmail(email);
        u.setPasswordHash(encoder.encode(req.getPassword()));
        u.setRole("USER");

        repo.save(u);
    }

    public User findByUsername(String username) {
        return repo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public void changePassword(String username, String currentPassword, String newPassword, String confirmPassword) {
        if (currentPassword == null || currentPassword.isBlank()) {
            throw new RuntimeException("Current password is required");
        }
        if (newPassword == null || newPassword.isBlank()) {
            throw new RuntimeException("New password is required");
        }
        if (!newPassword.equals(confirmPassword)) {
            throw new RuntimeException("Confirm password does not match");
        }

        User user = findByUsername(username);
        if (!encoder.matches(currentPassword, user.getPasswordHash())) {
            throw new RuntimeException("Current password is invalid");
        }

        user.setPasswordHash(encoder.encode(newPassword));
        repo.save(user);
    }

    public void resetPasswordWithOtp(String email, String otpCode, String newPassword, String confirmPassword) {
        if (email == null || email.trim().isEmpty()) {
            throw new RuntimeException("Email is required");
        }
        if (newPassword == null || newPassword.isBlank()) {
            throw new RuntimeException("New password is required");
        }
        if (!newPassword.equals(confirmPassword)) {
            throw new RuntimeException("Confirm password does not match");
        }

        otpService.verifyOtp(email, EmailOtpService.PURPOSE_RESET, otpCode);

        User user = repo.findByEmail(email.trim().toLowerCase(Locale.ROOT))
                .orElseThrow(() -> new RuntimeException("Email not found"));

        user.setPasswordHash(encoder.encode(newPassword));
        repo.save(user);
    }
}

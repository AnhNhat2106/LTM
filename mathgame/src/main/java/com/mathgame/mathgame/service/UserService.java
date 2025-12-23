package com.mathgame.mathgame.service;

import com.mathgame.mathgame.dto.RegisterRequest;
import com.mathgame.mathgame.entity.User;
import com.mathgame.mathgame.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository repo;
    private final PasswordEncoder encoder;

    public UserService(UserRepository repo, PasswordEncoder encoder) {
        this.repo = repo;
        this.encoder = encoder;
    }

    public void register(RegisterRequest req) {

        if (req.getUsername() == null || req.getUsername().trim().isEmpty()) {
            throw new RuntimeException("Username không được để trống");
        }
        if (req.getEmail() == null || req.getEmail().trim().isEmpty()) {
            throw new RuntimeException("Email không được để trống");
        }
        if (req.getPassword() == null || req.getPassword().isEmpty()) {
            throw new RuntimeException("Password không được để trống");
        }

        if (!req.getPassword().equals(req.getConfirmPassword())) {
            throw new RuntimeException("Mật khẩu xác nhận không khớp");
        }

        String username = req.getUsername().trim();
        String email = req.getEmail().trim();

        if (repo.existsByUsername(username)) {
            throw new RuntimeException("Username đã tồn tại");
        }
        if (repo.existsByEmail(email)) {
            throw new RuntimeException("Email đã tồn tại");
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
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user"));
    }

    public void changePassword(String username, String currentPassword, String newPassword, String confirmPassword) {
        if (currentPassword == null || currentPassword.isBlank()) {
            throw new RuntimeException("Mật khẩu hiện tại không được để trống");
        }
        if (newPassword == null || newPassword.isBlank()) {
            throw new RuntimeException("Mật khẩu mới không được để trống");
        }
        if (!newPassword.equals(confirmPassword)) {
            throw new RuntimeException("Mật khẩu xác nhận không khớp");
        }

        User user = findByUsername(username);
        if (!encoder.matches(currentPassword, user.getPasswordHash())) {
            throw new RuntimeException("Mật khẩu hiện tại không đúng");
        }

        user.setPasswordHash(encoder.encode(newPassword));
        repo.save(user);
    }

    public String resetPasswordByEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new RuntimeException("Email không được để trống");
        }

        User user = repo.findByEmail(email.trim())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy email này"));

        String tempPassword = generateTempPassword();
        user.setPasswordHash(encoder.encode(tempPassword));
        repo.save(user);

        return tempPassword;
    }

    private String generateTempPassword() {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789";
        StringBuilder sb = new StringBuilder(8);
        for (int i = 0; i < 8; i++) {
            int idx = (int) (Math.random() * chars.length());
            sb.append(chars.charAt(idx));
        }
        return sb.toString();
    }
}

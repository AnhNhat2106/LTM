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

        // Validate basic
        if (req.getUsername() == null || req.getUsername().trim().isEmpty()) {
            throw new RuntimeException("Username không được để trống");
        }
        if (req.getEmail() == null || req.getEmail().trim().isEmpty()) {
            throw new RuntimeException("Email không được để trống");
        }
        if (req.getPassword() == null || req.getPassword().isEmpty()) {
            throw new RuntimeException("Password không được để trống");
        }

        // 1) Check confirm password
        if (!req.getPassword().equals(req.getConfirmPassword())) {
            throw new RuntimeException("Mật khẩu xác nhận không khớp");
        }

        String username = req.getUsername().trim();
        String email = req.getEmail().trim();

        // 2) Check trùng username/email
        if (repo.existsByUsername(username)) {
            throw new RuntimeException("Username đã tồn tại");
        }
        if (repo.existsByEmail(email)) {
            throw new RuntimeException("Email đã tồn tại");
        }

        // 3) Tạo user & mã hóa password
        User u = new User();
        u.setUsername(username);
        u.setEmail(email);
        u.setPasswordHash(encoder.encode(req.getPassword()));
        u.setRole("USER"); // ✅ đồng bộ với Security

        // 4) Lưu DB
        repo.save(u);
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

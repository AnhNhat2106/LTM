package com.mathgame.mathgame.service;

import com.mathgame.mathgame.entity.EmailOtp;
import com.mathgame.mathgame.repository.EmailOtpRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Random;

@Service
public class EmailOtpService {

    public static final String PURPOSE_REGISTER = "REGISTER";
    public static final String PURPOSE_RESET = "RESET";
    public static final String PURPOSE_CHANGE_PASSWORD = "CHANGE_PASSWORD";

    private final EmailOtpRepository repo;
    private final JavaMailSender mailSender;
    private final Random random = new Random();

    @Value("${app.otp.expire-minutes:5}")
    private int expireMinutes;

    @Value("${app.mail.from:}")
    private String mailFrom;

    public EmailOtpService(EmailOtpRepository repo, JavaMailSender mailSender) {
        this.repo = repo;
        this.mailSender = mailSender;
    }

    public void sendOtp(String email, String purpose) {
        String normEmail = normalizeEmail(email);
        String code = String.valueOf(100000 + random.nextInt(900000));

        EmailOtp otp = new EmailOtp();
        otp.setEmail(normEmail);
        otp.setPurpose(purpose);
        otp.setCode(code);
        otp.setCreatedAt(LocalDateTime.now());
        otp.setExpiresAt(LocalDateTime.now().plusMinutes(expireMinutes));
        repo.save(otp);

        SimpleMailMessage msg = new SimpleMailMessage();
        if (mailFrom != null && !mailFrom.isBlank()) {
            msg.setFrom(mailFrom.trim());
        }
        msg.setTo(normEmail);
        msg.setSubject("MathGame OTP");
        msg.setText("Your OTP code is: " + code + "\nThis code expires in " + expireMinutes + " minutes.");
        mailSender.send(msg);
    }

    public void verifyOtp(String email, String purpose, String code) {
        String normEmail = normalizeEmail(email);
        if (code == null || code.trim().isEmpty()) {
            throw new RuntimeException("OTP code is required");
        }

        EmailOtp latest = repo.findTopByEmailAndPurposeAndUsedAtIsNullOrderByCreatedAtDesc(normEmail, purpose)
                .orElseThrow(() -> new RuntimeException("OTP not found"));

        if (latest.getExpiresAt() != null && latest.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("OTP expired");
        }
        if (!latest.getCode().equals(code.trim())) {
            throw new RuntimeException("OTP invalid");
        }

        latest.setUsedAt(LocalDateTime.now());
        repo.save(latest);
    }

    private String normalizeEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new RuntimeException("Email is required");
        }
        String norm = email.trim().toLowerCase(Locale.ROOT);
        if (!norm.endsWith("@gmail.com")) {
            throw new RuntimeException("Email must be Gmail (@gmail.com)");
        }
        return norm;
    }
}

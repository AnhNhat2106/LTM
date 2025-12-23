package com.mathgame.mathgame.repository;

import com.mathgame.mathgame.entity.EmailOtp;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmailOtpRepository extends JpaRepository<EmailOtp, Long> {
    Optional<EmailOtp> findTopByEmailAndPurposeAndUsedAtIsNullOrderByCreatedAtDesc(String email, String purpose);
}

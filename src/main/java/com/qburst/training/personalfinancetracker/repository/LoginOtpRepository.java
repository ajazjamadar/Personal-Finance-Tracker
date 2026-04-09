package com.qburst.training.personalfinancetracker.repository;

import com.qburst.training.personalfinancetracker.entity.LoginOtp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LoginOtpRepository extends JpaRepository<LoginOtp, Long> {
    Optional<LoginOtp> findTopByUserIdAndPurposeAndUsedAtIsNullOrderByCreatedAtDesc(
            Long userId,
            LoginOtp.OtpPurpose purpose
    );
}

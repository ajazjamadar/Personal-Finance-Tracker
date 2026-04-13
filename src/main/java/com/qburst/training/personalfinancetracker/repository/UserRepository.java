package com.qburst.training.personalfinancetracker.repository;

import com.qburst.training.personalfinancetracker.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    List<User> findAllByOrderByCreatedAtDesc();
    List<User> findTop8ByOrderByCreatedAtDesc();
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByUsernameAndIdNot(String username, Long id);
    boolean existsByEmailAndIdNot(String email, Long id);
    boolean existsByIdAndRole(Long id, com.qburst.training.personalfinancetracker.entity.UserRole role);
    long countByCreatedAtBetween(LocalDateTime startInclusive, LocalDateTime endExclusive);
}

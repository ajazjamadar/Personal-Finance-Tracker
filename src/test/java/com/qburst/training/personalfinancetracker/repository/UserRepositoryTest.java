package com.qburst.training.personalfinancetracker.repository;

import com.qburst.training.personalfinancetracker.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
public class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .username("john_doe")
                .email("john@email.com")
                .passwordHash("hashed_password")
                .fullName("John Doe")
                .build();

        entityManager.persistAndFlush(testUser);
    }

    @Test
    @DisplayName("findByUsername: should find the user by exact username")
    void findByUsername_shouldReturnUser() {
        Optional<User> found = userRepository.findByUsername("john_doe");
        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(testUser.getId());
    }

    @Test
    @DisplayName("findByUsername: should return empty for unknown username")
    void findByUsername_shouldReturnEmpty_whereNotFound() {
        Optional<User> found = userRepository.findByUsername("nobody");
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("findByEmail: should find user by email")
    void findByEmail_shouldReturnUser() {
        Optional<User> found = userRepository.findByEmail("john@email.com");

        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo("john_doe");
    }

    @Test
    @DisplayName("findByEmail: should return empty for unknown email")
    void findByEmail_shouldReturnEmpty_whenNotFound() {
        Optional<User> found = userRepository.findByEmail("unknown@email.com");
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("existsByUsername: should return true if user exists")
    void existsByUsername_shouldReturnTrue_whenUsernameFound() {
        boolean exists = userRepository.existsByUsername("john_doe");
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("existsByUsername: should return false if user doesn't exist")
    void existsByUsername_shouldReturnFalse_whenUsernameNotFound() {
        boolean exists = userRepository.existsByUsername("unknown_user");
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("existsByEmail: should return true if email exists")
    void existsByEmail_shouldReturnTrue_whenEmailFound() {
        boolean exists = userRepository.existsByEmail("john@email.com");
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("existsByEmail: should return false for unknown email")
    void existsByEmail_shouldReturnFalse_whenEmailNotFound() {
        boolean exists = userRepository.existsByEmail("unknown@email.com");
        assertThat(exists).isFalse();
    }
}

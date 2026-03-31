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

    //spring injects the real JPA manager wired to h2
    @Autowired
    private TestEntityManager entityManager;

    //spring injects the real user repository implementation
    @Autowired
    private UserRepository userRepository;

    //reusable user instance
    private User testUser;

    //@Before each runs before every single @test method
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

    //Table Creation
    @Test
    @DisplayName("Table exists: should be able to save and retrieve a User")
    void tableShouldExistAndAllowBasicCRUD(){
        long count = userRepository.count();
        assertThat(count).isGreaterThanOrEqualTo(1);
    }

    //Basic Find
    @Test
    @DisplayName("findById: should return the user we persisted")
    void findById_should_ReturnUser(){
        Optional<User> found = userRepository.findById(testUser.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo("john_doe");
        assertThat(found.get().getEmail()).isEqualTo("john@email.com");
        assertThat(found.get().getFullName()).isEqualTo("John Doe");
    }

    @Test
    @DisplayName("findById: should return empty Optional for non-existent ID")
    void findById_shouldReturnEmpty_whenUserDoesNotExist(){
        Optional<User> found = userRepository.findById(999L);
        assertThat(found).isEmpty();
    }

    //Custom Query Methods
    @Test
    @DisplayName("findByUsername: should find the user by exact username")
    void findByUsername_shouldReturnUser(){
        Optional<User> found = userRepository.findByUsername("john_doe");
        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(testUser.getId());
    }

    @Test
    @DisplayName("findByUsername: should return empty for unknown username")
    void findByUsername_shouldReturnEmpty_whereNotFound(){
        Optional<User> found = userRepository.findByUsername("nobody");
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("findByEmail: should find user by email")
    void findByEmail_shouldReturnUser(){
        Optional<User> found = userRepository.findByEmail("john@email.com");

        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo("john_doe");
    }

    //Existing Checks
    @Test
    @DisplayName("existByUsername: should return true if user exists")
    void findByUsername_shouldReturnTrue_whenUsernameFound(){
        boolean exists = userRepository.existsByUsername("john_doe");
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("existByUsername: should return false if user doesn't exits")
    void findByUsername_shouldReturnFals_whenUsernameNotFound(){
        boolean exists = userRepository.existsByUsername("unknown_user");
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("existsByEmail: should return true if email exits")
    void findByEmail_shouldReturnTrue_ifEmailFound(){
        boolean exists = userRepository.existsByEmail("john@email.com");
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("existsByEmail: should return false for unknown email")
    void findByEmail_shouldReturnFlase_ifNotFound(){
        boolean exists = userRepository.existsByEmail("unknown@email.com");
        assertThat(exists).isFalse();
    }

    //Unique Constraints Validation
    @Test
    @DisplayName("Unique Constraints: duplicate username should throw on flush")
    void save_shouldFail_whenUsernameIsDuplicate(){
        User duplicate = User.builder()
                .username("john_doe")
                .email("another@email.com")
                .passwordHash("pass")
                .fullName("Different John")
                .build();

        org.junit.jupiter.api.Assertions.assertThrows(
                Exception.class, () -> entityManager.persistAndFlush(duplicate), "Expected a constraint violation for duplicate username"
        );
    }

    @Test
    @DisplayName("Unique Constraint: duplicate email should throw on flush")
    void save_shouldFail_whenEmailIsDuplicate(){
        User duplicate = User.builder()
                .username("jane_dow")
                .email("john@email.com")
                .passwordHash("pass")
                .fullName("Jane Dow")
                .build();

        org.junit.jupiter.api.Assertions.assertThrows(

                Exception.class, () -> entityManager.persistAndFlush(duplicate)
        );
    }

    //Audit Fields
    @Test
    @DisplayName("createdAt: sould be populated automatically by @prepersist")
    void createdAt_shouldBeSetAutomatically(){
        assertThat(testUser.getCreatedAt()).isNotNull();
    }
}

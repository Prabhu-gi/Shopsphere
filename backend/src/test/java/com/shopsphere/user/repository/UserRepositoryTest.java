package com.shopsphere.user.repository;

import com.shopsphere.user.entity.Role;
import com.shopsphere.user.entity.User;
import com.shopsphere.user.entity.UserStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("givenNewUser_whenSave_thenCanBeFoundByEmail")
    void givenNewUser_whenSave_thenCanBeFoundByEmail() {
        // Given
        User user = User.builder()
                .email("test.repo@shopsphere.com")
                .passwordHash("hashedPass")
                .firstName("Test")
                .lastName("Repo")
                .role(Role.CUSTOMER)
                .status(UserStatus.ACTIVE)
                .build();

        userRepository.save(user);

        // When
        Optional<User> found = userRepository.findByEmail("test.repo@shopsphere.com");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getFirstName()).isEqualTo("Test");
        assertThat(userRepository.existsByEmail("test.repo@shopsphere.com")).isTrue();
        assertThat(userRepository.existsByEmail("nonexistent@shopsphere.com")).isFalse();
    }
}

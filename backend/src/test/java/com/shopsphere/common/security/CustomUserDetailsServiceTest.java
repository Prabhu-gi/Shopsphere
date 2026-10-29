package com.shopsphere.common.security;

import com.shopsphere.user.entity.Role;
import com.shopsphere.user.entity.User;
import com.shopsphere.user.entity.UserStatus;
import com.shopsphere.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService userDetailsService;

    private User sampleUser;

    @BeforeEach
    void setUp() {
        sampleUser = User.builder()
                .id(1L)
                .email("user@example.com")
                .passwordHash("secret")
                .firstName("John")
                .lastName("Doe")
                .role(Role.CUSTOMER)
                .status(UserStatus.ACTIVE)
                .build();
    }

    @Test
    @DisplayName("givenExistingEmail_whenLoadUserByUsername_thenReturnsUserPrincipal")
    void givenExistingEmail_whenLoadUserByUsername_thenReturnsUserPrincipal() {
        given(userRepository.findByEmail("user@example.com")).willReturn(Optional.of(sampleUser));

        UserDetails userDetails = userDetailsService.loadUserByUsername("user@example.com");

        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getUsername()).isEqualTo("user@example.com");
        assertThat(userDetails.isAccountNonExpired()).isTrue();
        assertThat(userDetails.isAccountNonLocked()).isTrue();
        assertThat(userDetails.isCredentialsNonExpired()).isTrue();
        assertThat(userDetails.isEnabled()).isTrue();
    }

    @Test
    @DisplayName("givenNonExistingEmail_whenLoadUserByUsername_thenThrowsUsernameNotFound")
    void givenNonExistingEmail_whenLoadUserByUsername_thenThrowsUsernameNotFound() {
        given(userRepository.findByEmail("missing@example.com")).willReturn(Optional.empty());

        assertThatThrownBy(() -> userDetailsService.loadUserByUsername("missing@example.com"))
                .isInstanceOf(UsernameNotFoundException.class);
    }

    @Test
    @DisplayName("givenExistingId_whenLoadUserById_thenReturnsUserPrincipal")
    void givenExistingId_whenLoadUserById_thenReturnsUserPrincipal() {
        given(userRepository.findById(1L)).willReturn(Optional.of(sampleUser));

        UserDetails userDetails = userDetailsService.loadUserById(1L);

        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getUsername()).isEqualTo("user@example.com");
    }

    @Test
    @DisplayName("givenNonExistingId_whenLoadUserById_thenThrowsUsernameNotFound")
    void givenNonExistingId_whenLoadUserById_thenThrowsUsernameNotFound() {
        given(userRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> userDetailsService.loadUserById(99L))
                .isInstanceOf(UsernameNotFoundException.class);
    }
}


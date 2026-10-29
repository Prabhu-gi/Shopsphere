package com.shopsphere.user.service;

import com.shopsphere.common.exception.ResourceNotFoundException;
import com.shopsphere.user.dto.UserProfileResponse;
import com.shopsphere.user.dto.UserUpdateRequest;
import com.shopsphere.user.entity.Role;
import com.shopsphere.user.entity.User;
import com.shopsphere.user.entity.UserStatus;
import com.shopsphere.user.mapper.UserMapper;
import com.shopsphere.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    private User sampleUser;

    @BeforeEach
    void setUp() {
        sampleUser = User.builder()
                .id(1L)
                .email("user@example.com")
                .firstName("John")
                .lastName("Doe")
                .phone("+1234567890")
                .role(Role.CUSTOMER)
                .status(UserStatus.ACTIVE)
                .build();
    }

    @Test
    @DisplayName("givenExistingUserId_whenGetCurrentUserProfile_thenReturnsProfile")
    void givenExistingUserId_whenGetCurrentUserProfile_thenReturnsProfile() {
        // Given
        UserProfileResponse profileResponse = UserProfileResponse.builder()
                .id(1L)
                .email("user@example.com")
                .firstName("John")
                .lastName("Doe")
                .role(Role.CUSTOMER)
                .build();

        given(userRepository.findById(1L)).willReturn(Optional.of(sampleUser));
        given(userMapper.toProfileResponse(sampleUser)).willReturn(profileResponse);

        // When
        UserProfileResponse response = userService.getCurrentUserProfile(1L);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getEmail()).isEqualTo("user@example.com");
    }

    @Test
    @DisplayName("givenNonExistingUserId_whenGetCurrentUserProfile_thenThrowsResourceNotFoundException")
    void givenNonExistingUserId_whenGetCurrentUserProfile_thenThrowsResourceNotFoundException() {
        // Given
        given(userRepository.findById(99L)).willReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> userService.getCurrentUserProfile(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    @DisplayName("givenValidUpdateRequest_whenUpdateCurrentUserProfile_thenSavesAndReturnsUpdated")
    void givenValidUpdateRequest_whenUpdateCurrentUserProfile_thenSavesAndReturnsUpdated() {
        // Given
        UserUpdateRequest updateRequest = UserUpdateRequest.builder()
                .firstName("Jonathan")
                .lastName("Smith")
                .phone("+9876543210")
                .build();

        UserProfileResponse updatedProfile = UserProfileResponse.builder()
                .id(1L)
                .email("user@example.com")
                .firstName("Jonathan")
                .lastName("Smith")
                .phone("+9876543210")
                .build();

        given(userRepository.findById(1L)).willReturn(Optional.of(sampleUser));
        given(userRepository.save(any(User.class))).willReturn(sampleUser);
        given(userMapper.toProfileResponse(sampleUser)).willReturn(updatedProfile);

        // When
        UserProfileResponse response = userService.updateCurrentUserProfile(1L, updateRequest);

        // Then
        assertThat(response.getFirstName()).isEqualTo("Jonathan");
        assertThat(response.getLastName()).isEqualTo("Smith");
        verify(userMapper).updateUserFromDto(updateRequest, sampleUser);
        verify(userRepository).save(sampleUser);
    }
}


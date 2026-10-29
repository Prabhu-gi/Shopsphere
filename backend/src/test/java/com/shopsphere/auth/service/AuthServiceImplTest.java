package com.shopsphere.auth.service;

import com.shopsphere.auth.dto.AuthResponse;
import com.shopsphere.auth.dto.LoginRequest;
import com.shopsphere.auth.dto.RefreshTokenRequest;
import com.shopsphere.auth.dto.RegisterRequest;
import com.shopsphere.auth.entity.RefreshToken;
import com.shopsphere.auth.repository.RefreshTokenRepository;
import com.shopsphere.common.config.JwtTokenProvider;
import com.shopsphere.common.config.RateLimiterService;
import com.shopsphere.common.exception.BadRequestException;
import com.shopsphere.common.exception.UnauthorizedException;
import com.shopsphere.common.security.UserPrincipal;
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
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private RateLimiterService rateLimiterService;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private AuthServiceImpl authService;

    private User sampleUser;
    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        sampleUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .passwordHash("encodedPassword")
                .firstName("John")
                .lastName("Doe")
                .role(Role.CUSTOMER)
                .status(UserStatus.ACTIVE)
                .build();

        registerRequest = RegisterRequest.builder()
                .email("test@example.com")
                .password("Password@123")
                .firstName("John")
                .lastName("Doe")
                .phone("+1234567890")
                .build();

        loginRequest = LoginRequest.builder()
                .email("test@example.com")
                .password("Password@123")
                .build();
    }

    @Test
    @DisplayName("givenValidRegistration_whenRegister_thenSuccessAndReturnsTokens")
    void givenValidRegistration_whenRegister_thenSuccessAndReturnsTokens() {
        // Given
        given(userRepository.existsByEmail("test@example.com")).willReturn(false);
        given(userMapper.toEntity(registerRequest)).willReturn(new User());
        given(passwordEncoder.encode(anyString())).willReturn("encodedPassword");
        given(userRepository.save(any(User.class))).willReturn(sampleUser);
        given(jwtTokenProvider.generateAccessTokenForUser(any(), any(), any(), any())).willReturn("jwt-access-token");
        given(jwtTokenProvider.getRefreshExpirationMs()).willReturn(604800000L);

        RefreshToken refreshToken = RefreshToken.builder()
                .token("mock-refresh-token")
                .user(sampleUser)
                .expiryDate(Instant.now().plusMillis(604800000L))
                .build();
        given(refreshTokenRepository.save(any(RefreshToken.class))).willReturn(refreshToken);

        // When
        AuthResponse response = authService.register(registerRequest, "127.0.0.1");

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("jwt-access-token");
        assertThat(response.getRefreshToken()).isEqualTo("mock-refresh-token");
        assertThat(response.getEmail()).isEqualTo("test@example.com");
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("givenDuplicateEmail_whenRegister_thenThrowsBadRequestException")
    void givenDuplicateEmail_whenRegister_thenThrowsBadRequestException() {
        // Given
        given(userRepository.existsByEmail("test@example.com")).willReturn(true);

        // When / Then
        assertThatThrownBy(() -> authService.register(registerRequest, "127.0.0.1"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("already registered");

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("givenValidCredentials_whenLogin_thenReturnsTokens")
    void givenValidCredentials_whenLogin_thenReturnsTokens() {
        // Given
        Authentication auth = mock(Authentication.class);
        UserPrincipal principal = UserPrincipal.create(sampleUser);
        given(auth.getPrincipal()).willReturn(principal);
        given(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).willReturn(auth);
        given(jwtTokenProvider.generateAccessToken(auth)).willReturn("jwt-access-token");
        given(userRepository.findById(1L)).willReturn(Optional.of(sampleUser));
        given(jwtTokenProvider.getRefreshExpirationMs()).willReturn(604800000L);

        RefreshToken refreshToken = RefreshToken.builder()
                .token("mock-refresh-token")
                .user(sampleUser)
                .expiryDate(Instant.now().plusMillis(604800000L))
                .build();
        given(refreshTokenRepository.save(any(RefreshToken.class))).willReturn(refreshToken);

        // When
        AuthResponse response = authService.login(loginRequest, "127.0.0.1");

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("jwt-access-token");
        assertThat(response.getRefreshToken()).isEqualTo("mock-refresh-token");
        assertThat(response.getUserId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("givenInvalidCredentials_whenLogin_thenThrowsBadCredentialsException")
    void givenInvalidCredentials_whenLogin_thenThrowsBadCredentialsException() {
        // Given
        given(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .willThrow(new BadCredentialsException("Bad credentials"));

        // When / Then
        assertThatThrownBy(() -> authService.login(loginRequest, "127.0.0.1"))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    @DisplayName("givenValidRefreshToken_whenRefresh_thenRotatesTokenAndReturnsNewAccess")
    void givenValidRefreshToken_whenRefresh_thenRotatesTokenAndReturnsNewAccess() {
        // Given
        RefreshToken oldToken = RefreshToken.builder()
                .token("old-refresh-token")
                .user(sampleUser)
                .expiryDate(Instant.now().plusSeconds(3600))
                .build();

        RefreshToken newToken = RefreshToken.builder()
                .token("new-refresh-token")
                .user(sampleUser)
                .expiryDate(Instant.now().plusSeconds(7200))
                .build();

        given(refreshTokenRepository.findByToken("old-refresh-token")).willReturn(Optional.of(oldToken));
        given(jwtTokenProvider.generateAccessTokenForUser(any(), any(), any(), any())).willReturn("new-jwt-access-token");
        given(jwtTokenProvider.getRefreshExpirationMs()).willReturn(604800000L);
        given(refreshTokenRepository.save(any(RefreshToken.class))).willReturn(newToken);

        // When
        AuthResponse response = authService.refreshToken(new RefreshTokenRequest("old-refresh-token"));

        // Then
        assertThat(response.getAccessToken()).isEqualTo("new-jwt-access-token");
        assertThat(response.getRefreshToken()).isEqualTo("new-refresh-token");
        verify(refreshTokenRepository).delete(oldToken);
    }

    @Test
    @DisplayName("givenExpiredRefreshToken_whenRefresh_thenThrowsUnauthorizedException")
    void givenExpiredRefreshToken_whenRefresh_thenThrowsUnauthorizedException() {
        // Given
        RefreshToken expiredToken = RefreshToken.builder()
                .token("expired-token")
                .user(sampleUser)
                .expiryDate(Instant.now().minusSeconds(3600))
                .build();

        given(refreshTokenRepository.findByToken("expired-token")).willReturn(Optional.of(expiredToken));

        // When / Then
        assertThatThrownBy(() -> authService.refreshToken(new RefreshTokenRequest("expired-token")))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("expired");

        verify(refreshTokenRepository).delete(expiredToken);
    }

    @Test
    @DisplayName("givenToken_whenLogout_thenDeletesByToken")
    void givenToken_whenLogout_thenDeletesByToken() {
        authService.logout("valid-refresh-token");
        verify(refreshTokenRepository).deleteByToken("valid-refresh-token");
    }

    @Test
    @DisplayName("givenNullOrBlankToken_whenLogout_thenDoesNotCallDelete")
    void givenNullOrBlankToken_whenLogout_thenDoesNotCallDelete() {
        authService.logout(null);
        authService.logout("");
        verify(refreshTokenRepository, never()).deleteByToken(anyString());
    }
}

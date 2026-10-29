package com.shopsphere.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shopsphere.auth.dto.AuthResponse;
import com.shopsphere.auth.dto.LoginRequest;
import com.shopsphere.auth.dto.RefreshTokenRequest;
import com.shopsphere.auth.dto.RegisterRequest;
import com.shopsphere.auth.service.AuthService;
import com.shopsphere.common.config.JwtAuthenticationFilter;
import com.shopsphere.common.config.JwtTokenProvider;
import com.shopsphere.common.config.SecurityConfig;
import com.shopsphere.common.security.CustomUserDetailsService;
import com.shopsphere.user.entity.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AuthController.class)
@Import(SecurityConfig.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    @DisplayName("givenValidRegisterRequest_whenPostRegister_thenReturns201AndTokens")
    void givenValidRegisterRequest_whenPostRegister_thenReturns201AndTokens() throws Exception {
        // Given
        RegisterRequest request = RegisterRequest.builder()
                .email("newuser@example.com")
                .password("Password@123")
                .firstName("Jane")
                .lastName("Doe")
                .phone("+1234567890")
                .build();

        AuthResponse authResponse = AuthResponse.builder()
                .accessToken("mock-access-token")
                .refreshToken("mock-refresh-token")
                .tokenType("Bearer")
                .userId(1L)
                .email("newuser@example.com")
                .firstName("Jane")
                .lastName("Doe")
                .role(Role.CUSTOMER)
                .build();

        given(authService.register(any(RegisterRequest.class), anyString())).willReturn(authResponse);

        // When & Then
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").value("mock-access-token"))
                .andExpect(jsonPath("$.data.email").value("newuser@example.com"))
                .andExpect(jsonPath("$.data.role").value("CUSTOMER"));
    }

    @Test
    @DisplayName("givenInvalidEmail_whenPostRegister_thenReturns422WithFieldErrors")
    void givenInvalidEmail_whenPostRegister_thenReturns422WithFieldErrors() throws Exception {
        // Given - bad email and short password
        RegisterRequest request = RegisterRequest.builder()
                .email("not-an-email")
                .password("123")
                .firstName("")
                .lastName("Doe")
                .build();

        // When & Then
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error").value("Validation Failed"))
                .andExpect(jsonPath("$.fieldErrors.email").exists())
                .andExpect(jsonPath("$.fieldErrors.password").exists())
                .andExpect(jsonPath("$.fieldErrors.firstName").exists());
    }

    @Test
    @DisplayName("givenValidLoginRequest_whenPostLogin_thenReturns200AndTokens")
    void givenValidLoginRequest_whenPostLogin_thenReturns200AndTokens() throws Exception {
        // Given
        LoginRequest request = LoginRequest.builder()
                .email("user@example.com")
                .password("Password@123")
                .build();

        AuthResponse authResponse = AuthResponse.builder()
                .accessToken("mock-access-token")
                .refreshToken("mock-refresh-token")
                .tokenType("Bearer")
                .userId(2L)
                .email("user@example.com")
                .role(Role.CUSTOMER)
                .build();

        given(authService.login(any(LoginRequest.class), anyString())).willReturn(authResponse);

        // When & Then
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").value("mock-access-token"));
    }

    @Test
    @DisplayName("givenValidRefreshToken_whenPostRefresh_thenReturns200")
    void givenValidRefreshToken_whenPostRefresh_thenReturns200() throws Exception {
        // Given
        RefreshTokenRequest request = new RefreshTokenRequest("valid-refresh-token");
        AuthResponse authResponse = AuthResponse.builder()
                .accessToken("new-access-token")
                .refreshToken("new-refresh-token")
                .tokenType("Bearer")
                .userId(2L)
                .email("user@example.com")
                .role(Role.CUSTOMER)
                .build();

        given(authService.refreshToken(any(RefreshTokenRequest.class))).willReturn(authResponse);

        // When & Then
        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").value("new-access-token"));
    }

    @Test
    @DisplayName("givenRefreshToken_whenPostLogout_thenReturns200")
    void givenRefreshToken_whenPostLogout_thenReturns200() throws Exception {
        RefreshTokenRequest request = new RefreshTokenRequest("valid-refresh-token");

        mockMvc.perform(post("/api/v1/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Logout successful"));
    }
}

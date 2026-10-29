package com.shopsphere.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shopsphere.common.config.JwtAuthenticationFilter;
import com.shopsphere.common.config.JwtTokenProvider;
import com.shopsphere.common.security.CustomUserDetailsService;
import com.shopsphere.common.security.UserPrincipal;
import com.shopsphere.user.dto.UserProfileResponse;
import com.shopsphere.user.dto.UserUpdateRequest;
import com.shopsphere.user.entity.Role;
import com.shopsphere.user.entity.UserStatus;
import com.shopsphere.user.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @MockBean
        private UserService userService;

        @MockBean
        private JwtTokenProvider jwtTokenProvider;

        @MockBean
        private CustomUserDetailsService customUserDetailsService;

        @MockBean
        private JwtAuthenticationFilter jwtAuthenticationFilter;

        private UserPrincipal principal;

        @org.junit.jupiter.api.BeforeEach
        void setUp() {
                principal = UserPrincipal.builder()
                                .id(1L)
                                .email("user@example.com")
                                .firstName("John")
                                .lastName("Doe")
                                .role(Role.CUSTOMER)
                                .status(UserStatus.ACTIVE)
                                .authorities(java.util.List.of(
                                                new org.springframework.security.core.authority.SimpleGrantedAuthority(
                                                                "ROLE_CUSTOMER")))
                                .build();
                org.springframework.security.authentication.UsernamePasswordAuthenticationToken auth = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                                principal, null, principal.getAuthorities());
                org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(auth);
        }

        @org.junit.jupiter.api.AfterEach
        void tearDown() {
                org.springframework.security.core.context.SecurityContextHolder.clearContext();
        }

        @Test
        @DisplayName("givenAuthenticatedUser_whenGetProfile_thenReturns200AndProfile")
        void givenAuthenticatedUser_whenGetProfile_thenReturns200AndProfile() throws Exception {
                UserProfileResponse profileResponse = UserProfileResponse.builder()
                                .id(1L)
                                .email("user@example.com")
                                .firstName("John")
                                .lastName("Doe")
                                .role(Role.CUSTOMER)
                                .status(UserStatus.ACTIVE)
                                .build();

                given(userService.getCurrentUserProfile(1L)).willReturn(profileResponse);

                mockMvc.perform(get("/api/v1/users/me")
                                .with(user(principal)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data.email").value("user@example.com"));
        }

        @Test
        @DisplayName("givenValidUpdate_whenPutProfile_thenReturns200AndUpdatedProfile")
        void givenValidUpdate_whenPutProfile_thenReturns200AndUpdatedProfile() throws Exception {
                UserUpdateRequest updateRequest = UserUpdateRequest.builder()
                                .firstName("Jonathan")
                                .lastName("Smith")
                                .phone("+9876543210")
                                .build();

                UserProfileResponse updated = UserProfileResponse.builder()
                                .id(1L)
                                .email("user@example.com")
                                .firstName("Jonathan")
                                .lastName("Smith")
                                .build();

                given(userService.updateCurrentUserProfile(eq(1L), any(UserUpdateRequest.class))).willReturn(updated);

                mockMvc.perform(put("/api/v1/users/me")
                                .with(user(principal))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updateRequest)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data.firstName").value("Jonathan"))
                                .andExpect(jsonPath("$.data.lastName").value("Smith"));
        }
}

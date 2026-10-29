package com.shopsphere.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shopsphere.common.config.JwtAuthenticationFilter;
import com.shopsphere.common.config.JwtTokenProvider;
import com.shopsphere.common.security.CustomUserDetailsService;
import com.shopsphere.common.security.UserPrincipal;
import com.shopsphere.user.dto.AddressRequest;
import com.shopsphere.user.dto.AddressResponse;
import com.shopsphere.user.entity.Role;
import com.shopsphere.user.entity.UserStatus;
import com.shopsphere.user.service.AddressService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AddressController.class)
@AutoConfigureMockMvc(addFilters = false)
class AddressControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AddressService addressService;

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
                .authorities(java.util.List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_CUSTOMER")))
                .build();
        org.springframework.security.authentication.UsernamePasswordAuthenticationToken auth =
                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
        org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @org.junit.jupiter.api.AfterEach
    void tearDown() {
        org.springframework.security.core.context.SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("givenUserWithAddresses_whenGetAddresses_thenReturns200AndList")
    void givenUserWithAddresses_whenGetAddresses_thenReturns200AndList() throws Exception {
        AddressResponse address = AddressResponse.builder()
                .id(10L)
                .fullName("John Doe")
                .phone("+1234567890")
                .streetLine1("123 Main St")
                .city("New York")
                .state("NY")
                .postalCode("10001")
                .country("USA")
                .isDefault(true)
                .build();

        given(addressService.getUserAddresses(1L)).willReturn(List.of(address));

        mockMvc.perform(get("/api/v1/addresses")
                        .with(user(principal)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value(10L))
                .andExpect(jsonPath("$.data[0].city").value("New York"));
    }

    @Test
    @DisplayName("givenValidAddress_whenCreateAddress_thenReturns201Created")
    void givenValidAddress_whenCreateAddress_thenReturns201Created() throws Exception {
        AddressRequest request = AddressRequest.builder()
                .fullName("John Doe")
                .phone("+1234567890")
                .streetLine1("123 Main St")
                .city("New York")
                .state("NY")
                .postalCode("10001")
                .country("USA")
                .isDefault(true)
                .build();

        AddressResponse response = AddressResponse.builder()
                .id(11L)
                .fullName("John Doe")
                .city("New York")
                .isDefault(true)
                .build();

        given(addressService.addAddress(eq(1L), any(AddressRequest.class))).willReturn(response);

        mockMvc.perform(post("/api/v1/addresses")
                        .with(user(principal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").value(11L))
                .andExpect(jsonPath("$.data.isDefault").value(true));
    }

    @Test
    @DisplayName("givenAddressId_whenDeleteAddress_thenReturns200Ok")
    void givenAddressId_whenDeleteAddress_thenReturns200Ok() throws Exception {
        mockMvc.perform(delete("/api/v1/addresses/10")
                        .with(user(principal)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Address deleted successfully"));
    }
}

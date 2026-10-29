package com.shopsphere.catalog.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shopsphere.catalog.dto.CategoryRequest;
import com.shopsphere.catalog.dto.CategoryResponse;
import com.shopsphere.catalog.service.CategoryService;
import com.shopsphere.common.config.JwtAuthenticationFilter;
import com.shopsphere.common.config.JwtTokenProvider;
import com.shopsphere.common.security.CustomUserDetailsService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AdminCategoryController.class)
@AutoConfigureMockMvc(addFilters = false)
class AdminCategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CategoryService categoryService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    @DisplayName("POST /api/v1/admin/categories should create category")
    void givenValidRequest_whenCreateCategory_thenReturnsCreated() throws Exception {
        CategoryRequest request = CategoryRequest.builder()
                .name("Smartphones")
                .description("Mobile phones")
                .build();

        CategoryResponse response = CategoryResponse.builder()
                .id(2L)
                .name("Smartphones")
                .slug("smartphones")
                .active(true)
                .build();

        given(categoryService.createCategory(any(CategoryRequest.class))).willReturn(response);

        mockMvc.perform(post("/api/v1/admin/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Smartphones"));
    }

    @Test
    @DisplayName("PUT /api/v1/admin/categories/{id} should update category")
    void givenValidRequest_whenUpdateCategory_thenReturnsOk() throws Exception {
        CategoryRequest request = CategoryRequest.builder()
                .name("Smartphones & Phablets")
                .build();

        CategoryResponse response = CategoryResponse.builder()
                .id(2L)
                .name("Smartphones & Phablets")
                .slug("smartphones-phablets")
                .active(true)
                .build();

        given(categoryService.updateCategory(eq(2L), any(CategoryRequest.class))).willReturn(response);

        mockMvc.perform(put("/api/v1/admin/categories/2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Smartphones & Phablets"));
    }

    @Test
    @DisplayName("DELETE /api/v1/admin/categories/{id} should delete category")
    void givenId_whenDeleteCategory_thenReturnsOk() throws Exception {
        mockMvc.perform(delete("/api/v1/admin/categories/2")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Category deleted successfully"));
    }
}


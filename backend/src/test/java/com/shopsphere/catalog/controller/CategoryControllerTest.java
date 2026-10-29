package com.shopsphere.catalog.controller;

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

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = CategoryController.class)
@AutoConfigureMockMvc(addFilters = false)
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CategoryService categoryService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    @DisplayName("GET /api/v1/categories?tree=true should return category tree")
    void givenCategories_whenGetCategories_thenReturnsTree() throws Exception {
        CategoryResponse response = CategoryResponse.builder()
                .id(1L)
                .name("Electronics")
                .slug("electronics")
                .active(true)
                .build();

        given(categoryService.getCategoryTree()).willReturn(List.of(response));

        mockMvc.perform(get("/api/v1/categories?tree=true")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].name").value("Electronics"));
    }

    @Test
    @DisplayName("GET /api/v1/categories?tree=false should return flat category list")
    void givenCategories_whenGetFlatCategories_thenReturnsList() throws Exception {
        CategoryResponse response = CategoryResponse.builder()
                .id(1L)
                .name("Electronics")
                .slug("electronics")
                .active(true)
                .build();

        given(categoryService.getAllCategories()).willReturn(List.of(response));

        mockMvc.perform(get("/api/v1/categories?tree=false")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].name").value("Electronics"));
    }

    @Test
    @DisplayName("GET /api/v1/categories/{idOrSlug} with numeric ID should return category")
    void givenNumericId_whenGetCategory_thenReturnsCategory() throws Exception {
        CategoryResponse response = CategoryResponse.builder()
                .id(1L)
                .name("Electronics")
                .slug("electronics")
                .active(true)
                .build();

        given(categoryService.getCategoryById(1L)).willReturn(response);

        mockMvc.perform(get("/api/v1/categories/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1));
    }
}


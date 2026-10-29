package com.shopsphere.catalog.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shopsphere.catalog.dto.ProductCreateRequest;
import com.shopsphere.catalog.dto.ProductDetailResponse;
import com.shopsphere.catalog.dto.ProductImageDto;
import com.shopsphere.catalog.dto.ProductUpdateRequest;
import com.shopsphere.catalog.service.ProductService;
import com.shopsphere.common.config.JwtAuthenticationFilter;
import com.shopsphere.common.config.JwtTokenProvider;
import com.shopsphere.common.security.CustomUserDetailsService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AdminProductController.class)
@AutoConfigureMockMvc(addFilters = false)
class AdminProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProductService productService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    @DisplayName("POST /api/v1/admin/products should create product")
    void givenValidRequest_whenCreateProduct_thenReturnsCreated() throws Exception {
        ProductCreateRequest request = ProductCreateRequest.builder()
                .categoryId(1L)
                .name("Ergonomic Keyboard")
                .basePrice(new BigDecimal("129.99"))
                .build();

        ProductDetailResponse response = ProductDetailResponse.builder()
                .id(5L)
                .name("Ergonomic Keyboard")
                .slug("ergonomic-keyboard")
                .basePrice(new BigDecimal("129.99"))
                .build();

        given(productService.createProduct(any(ProductCreateRequest.class))).willReturn(response);

        mockMvc.perform(post("/api/v1/admin/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Ergonomic Keyboard"));
    }

    @Test
    @DisplayName("PUT /api/v1/admin/products/{id} should update product")
    void givenValidRequest_whenUpdateProduct_thenReturnsOk() throws Exception {
        ProductUpdateRequest request = ProductUpdateRequest.builder()
                .categoryId(1L)
                .name("Ergonomic Keyboard V2")
                .basePrice(new BigDecimal("149.99"))
                .build();

        ProductDetailResponse response = ProductDetailResponse.builder()
                .id(5L)
                .name("Ergonomic Keyboard V2")
                .slug("ergonomic-keyboard-v2")
                .basePrice(new BigDecimal("149.99"))
                .build();

        given(productService.updateProduct(eq(5L), any(ProductUpdateRequest.class))).willReturn(response);

        mockMvc.perform(put("/api/v1/admin/products/5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Ergonomic Keyboard V2"));
    }

    @Test
    @DisplayName("DELETE /api/v1/admin/products/{id} should deactivate product")
    void givenId_whenDeleteProduct_thenReturnsOk() throws Exception {
        mockMvc.perform(delete("/api/v1/admin/products/5")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Product deactivated successfully"));
    }

    @Test
    @DisplayName("POST /api/v1/admin/products/{id}/images should upload multipart image")
    void givenMultipartFile_whenUploadImage_thenReturnsCreated() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "keyboard.jpg",
                "image/jpeg",
                "sample-image-bytes".getBytes()
        );

        ProductImageDto response = ProductImageDto.builder()
                .id(10L)
                .imageUrl("/uploads/products/keyboard.jpg")
                .primary(true)
                .displayOrder(0)
                .build();

        given(productService.uploadProductImage(eq(5L), any(), eq(true))).willReturn(response);

        mockMvc.perform(multipart("/api/v1/admin/products/5/images")
                        .file(file)
                        .param("isPrimary", "true"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.imageUrl").value("/uploads/products/keyboard.jpg"));
    }

    @Test
    @DisplayName("DELETE /api/v1/admin/products/images/{imageId} should delete image")
    void givenImageId_whenDeleteImage_thenReturnsOk() throws Exception {
        mockMvc.perform(delete("/api/v1/admin/products/images/10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Image deleted successfully"));
    }

    @Test
    @DisplayName("PATCH /api/v1/admin/products/{productId}/images/{imageId}/primary should set primary image")
    void givenIds_whenSetPrimaryImage_thenReturnsOk() throws Exception {
        mockMvc.perform(patch("/api/v1/admin/products/5/images/10/primary")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Primary image updated successfully"));
    }
}


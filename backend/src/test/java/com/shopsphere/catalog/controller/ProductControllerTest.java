package com.shopsphere.catalog.controller;

import com.shopsphere.catalog.dto.ProductDetailResponse;
import com.shopsphere.catalog.dto.ProductFilterRequest;
import com.shopsphere.catalog.dto.ProductSummaryResponse;
import com.shopsphere.catalog.service.ProductService;
import com.shopsphere.common.config.JwtAuthenticationFilter;
import com.shopsphere.common.config.JwtTokenProvider;
import com.shopsphere.common.dto.PagedResponse;
import com.shopsphere.common.security.CustomUserDetailsService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ProductController.class)
@AutoConfigureMockMvc(addFilters = false)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService productService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    @DisplayName("GET /api/v1/products should return paginated list")
    void givenParams_whenGetProducts_thenReturnsPagedResponse() throws Exception {
        ProductSummaryResponse summary = ProductSummaryResponse.builder()
                .id(1L)
                .name("Mechanical Keyboard")
                .slug("mechanical-keyboard")
                .basePrice(new BigDecimal("99.99"))
                .inStock(true)
                .build();

        PagedResponse<ProductSummaryResponse> pagedResponse = PagedResponse.<ProductSummaryResponse>builder()
                .content(List.of(summary))
                .pageNumber(0)
                .pageSize(10)
                .totalElements(1)
                .totalPages(1)
                .isLast(true)
                .build();

        given(productService.getProducts(any(ProductFilterRequest.class))).willReturn(pagedResponse);

        mockMvc.perform(get("/api/v1/products?search=keyboard&sort=price_asc")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].name").value("Mechanical Keyboard"));
    }

    @Test
    @DisplayName("GET /api/v1/products/featured should return featured products")
    void givenLimit_whenGetFeaturedProducts_thenReturnsList() throws Exception {
        ProductSummaryResponse summary = ProductSummaryResponse.builder()
                .id(1L)
                .name("Mechanical Keyboard")
                .slug("mechanical-keyboard")
                .basePrice(new BigDecimal("99.99"))
                .build();

        given(productService.getFeaturedProducts(8)).willReturn(List.of(summary));

        mockMvc.perform(get("/api/v1/products/featured?limit=8")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].slug").value("mechanical-keyboard"));
    }

    @Test
    @DisplayName("GET /api/v1/products/{idOrSlug} should return product details")
    void givenSlug_whenGetProductDetails_thenReturnsDetail() throws Exception {
        ProductDetailResponse detail = ProductDetailResponse.builder()
                .id(1L)
                .name("Mechanical Keyboard")
                .slug("mechanical-keyboard")
                .basePrice(new BigDecimal("99.99"))
                .build();

        given(productService.getProductByIdOrSlug("mechanical-keyboard")).willReturn(detail);

        mockMvc.perform(get("/api/v1/products/mechanical-keyboard")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Mechanical Keyboard"));
    }

    @Test
    @DisplayName("GET /api/v1/products/{idOrSlug} with numeric ID should return product details")
    void givenId_whenGetProductDetails_thenReturnsDetail() throws Exception {
        ProductDetailResponse detail = ProductDetailResponse.builder()
                .id(1L)
                .name("Mechanical Keyboard")
                .slug("mechanical-keyboard")
                .basePrice(new BigDecimal("99.99"))
                .build();

        given(productService.getProductByIdOrSlug("1")).willReturn(detail);

        mockMvc.perform(get("/api/v1/products/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1));
    }
}


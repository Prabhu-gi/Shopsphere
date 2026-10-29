package com.shopsphere.catalog.controller;

import com.shopsphere.catalog.dto.ProductDetailResponse;
import com.shopsphere.catalog.dto.ProductFilterRequest;
import com.shopsphere.catalog.dto.ProductSummaryResponse;
import com.shopsphere.catalog.service.ProductService;
import com.shopsphere.common.dto.ApiResponse;
import com.shopsphere.common.dto.PagedResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Tag(name = "Catalog - Products", description = "Public Product browsing and search endpoints")
public class ProductController {

    private final ProductService productService;

    @GetMapping
    @Operation(summary = "Search and filter products", description = "Supports filtering by category, search text, price range, rating, stock status, sorting and pagination")
    public ResponseEntity<ApiResponse<PagedResponse<ProductSummaryResponse>>> getProducts(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String categorySlug,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) BigDecimal minRating,
            @RequestParam(required = false) Boolean inStockOnly,
            @RequestParam(required = false) Boolean featured,
            @RequestParam(defaultValue = "newest") String sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {

        ProductFilterRequest filter = ProductFilterRequest.builder()
                .categoryId(categoryId)
                .categorySlug(categorySlug)
                .search(search)
                .minPrice(minPrice)
                .maxPrice(maxPrice)
                .minRating(minRating)
                .inStockOnly(inStockOnly)
                .featured(featured)
                .sort(sort)
                .page(page)
                .size(size)
                .build();

        PagedResponse<ProductSummaryResponse> response = productService.getProducts(filter);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/featured")
    @Operation(summary = "Get featured products")
    public ResponseEntity<ApiResponse<List<ProductSummaryResponse>>> getFeaturedProducts(
            @RequestParam(defaultValue = "8") int limit) {
        List<ProductSummaryResponse> response = productService.getFeaturedProducts(limit);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{idOrSlug}")
    @Operation(summary = "Get product details by ID or slug")
    public ResponseEntity<ApiResponse<ProductDetailResponse>> getProductDetails(@PathVariable String idOrSlug) {
        ProductDetailResponse response = productService.getProductByIdOrSlug(idOrSlug);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}


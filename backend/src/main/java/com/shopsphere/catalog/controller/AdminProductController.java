package com.shopsphere.catalog.controller;

import com.shopsphere.catalog.dto.ProductCreateRequest;
import com.shopsphere.catalog.dto.ProductDetailResponse;
import com.shopsphere.catalog.dto.ProductImageDto;
import com.shopsphere.catalog.dto.ProductUpdateRequest;
import com.shopsphere.catalog.service.ProductService;
import com.shopsphere.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/admin/products")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "Admin - Products", description = "Admin Product and Inventory management endpoints")
public class AdminProductController {

    private final ProductService productService;

    @PostMapping
    @Operation(summary = "Create product with variants")
    public ResponseEntity<ApiResponse<ProductDetailResponse>> createProduct(
            @Valid @RequestBody ProductCreateRequest request) {
        ProductDetailResponse response = productService.createProduct(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Product created successfully", response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update product and variants")
    public ResponseEntity<ApiResponse<ProductDetailResponse>> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductUpdateRequest request) {
        ProductDetailResponse response = productService.updateProduct(id, request);
        return ResponseEntity.ok(ApiResponse.success("Product updated successfully", response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deactivate/delete product")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.ok(ApiResponse.message("Product deactivated successfully"));
    }

    @PostMapping(value = "/{id}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload product image")
    public ResponseEntity<ApiResponse<ProductImageDto>> uploadImage(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file,
            @RequestParam(name = "isPrimary", defaultValue = "false") boolean isPrimary) {
        ProductImageDto response = productService.uploadProductImage(id, file, isPrimary);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Image uploaded successfully", response));
    }

    @DeleteMapping("/images/{imageId}")
    @Operation(summary = "Delete product image")
    public ResponseEntity<ApiResponse<Void>> deleteImage(@PathVariable Long imageId) {
        productService.deleteProductImage(imageId);
        return ResponseEntity.ok(ApiResponse.message("Image deleted successfully"));
    }

    @PatchMapping("/{productId}/images/{imageId}/primary")
    @Operation(summary = "Set primary product image")
    public ResponseEntity<ApiResponse<Void>> setPrimaryImage(
            @PathVariable Long productId,
            @PathVariable Long imageId) {
        productService.setPrimaryProductImage(productId, imageId);
        return ResponseEntity.ok(ApiResponse.message("Primary image updated successfully"));
    }
}


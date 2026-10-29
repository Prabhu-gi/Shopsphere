package com.shopsphere.catalog.service;

import com.shopsphere.catalog.dto.*;
import com.shopsphere.common.dto.PagedResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ProductService {

    PagedResponse<ProductSummaryResponse> getProducts(ProductFilterRequest filter);

    List<ProductSummaryResponse> getFeaturedProducts(int limit);

    ProductDetailResponse getProductByIdOrSlug(String idOrSlug);

    ProductDetailResponse createProduct(ProductCreateRequest request);

    ProductDetailResponse updateProduct(Long id, ProductUpdateRequest request);

    void deleteProduct(Long id);

    ProductImageDto uploadProductImage(Long productId, MultipartFile file, boolean isPrimary);

    void deleteProductImage(Long imageId);

    void setPrimaryProductImage(Long productId, Long imageId);
}


package com.shopsphere.catalog.mapper;

import com.shopsphere.catalog.dto.*;
import com.shopsphere.catalog.entity.Category;
import com.shopsphere.catalog.entity.Product;
import com.shopsphere.catalog.entity.ProductImage;
import com.shopsphere.catalog.entity.ProductVariant;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CatalogMapper {

    @Mapping(target = "parentId", source = "parent.id")
    CategoryResponse toCategoryResponse(Category category);

    List<CategoryResponse> toCategoryResponseList(List<Category> categories);

    ProductVariantDto toVariantDto(ProductVariant variant);

    List<ProductVariantDto> toVariantDtoList(List<ProductVariant> variants);

    @Mapping(target = "product", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    ProductVariant toVariantEntity(ProductVariantDto dto);

    ProductImageDto toImageDto(ProductImage image);

    List<ProductImageDto> toImageDtoList(List<ProductImage> images);

    @Mapping(target = "categoryId", source = "category.id")
    @Mapping(target = "categoryName", source = "category.name")
    @Mapping(target = "categorySlug", source = "category.slug")
    @Mapping(target = "primaryImageUrl", source = "product", qualifiedByName = "extractPrimaryImageUrl")
    @Mapping(target = "inStock", source = "product", qualifiedByName = "calculateInStock")
    @Mapping(target = "totalStock", source = "product", qualifiedByName = "calculateTotalStock")
    ProductSummaryResponse toSummaryResponse(Product product);

    @Mapping(target = "categoryId", source = "category.id")
    @Mapping(target = "categoryName", source = "category.name")
    @Mapping(target = "categorySlug", source = "category.slug")
    ProductDetailResponse toDetailResponse(Product product);

    @Named("extractPrimaryImageUrl")
    default String extractPrimaryImageUrl(Product product) {
        if (product.getImages() == null || product.getImages().isEmpty()) {
            return null;
        }
        return product.getImages().stream()
                .filter(ProductImage::isPrimary)
                .map(ProductImage::getImageUrl)
                .findFirst()
                .orElse(product.getImages().get(0).getImageUrl());
    }

    @Named("calculateInStock")
    default boolean calculateInStock(Product product) {
        if (product.getVariants() == null || product.getVariants().isEmpty()) {
            return false;
        }
        return product.getVariants().stream()
                .filter(ProductVariant::isActive)
                .anyMatch(v -> v.getStockQuantity() != null && v.getStockQuantity() > 0);
    }

    @Named("calculateTotalStock")
    default Integer calculateTotalStock(Product product) {
        if (product.getVariants() == null || product.getVariants().isEmpty()) {
            return 0;
        }
        return product.getVariants().stream()
                .filter(ProductVariant::isActive)
                .mapToInt(v -> v.getStockQuantity() != null ? v.getStockQuantity() : 0)
                .sum();
    }
}


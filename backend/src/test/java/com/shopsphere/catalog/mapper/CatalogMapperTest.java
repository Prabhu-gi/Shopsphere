package com.shopsphere.catalog.mapper;

import com.shopsphere.catalog.dto.*;
import com.shopsphere.catalog.entity.Category;
import com.shopsphere.catalog.entity.Product;
import com.shopsphere.catalog.entity.ProductImage;
import com.shopsphere.catalog.entity.ProductVariant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CatalogMapperTest {

    private CatalogMapper catalogMapper;

    @BeforeEach
    void setUp() {
        catalogMapper = Mappers.getMapper(CatalogMapper.class);
    }

    @Test
    @DisplayName("should map Category to CategoryResponse and list")
    void shouldMapCategory() {
        Category parent = Category.builder().id(1L).name("Parent").slug("parent").build();
        Category cat = Category.builder()
                .id(2L)
                .parent(parent)
                .name("Subcat")
                .slug("subcat")
                .active(true)
                .build();

        CategoryResponse resp = catalogMapper.toCategoryResponse(cat);
        assertThat(resp).isNotNull();
        assertThat(resp.getParentId()).isEqualTo(1L);
        assertThat(resp.getName()).isEqualTo("Subcat");

        List<CategoryResponse> list = catalogMapper.toCategoryResponseList(List.of(cat));
        assertThat(list).hasSize(1);
    }

    @Test
    @DisplayName("should map ProductVariant to DTO and Entity and list")
    void shouldMapVariant() {
        ProductVariant variant = ProductVariant.builder()
                .id(1L)
                .sku("SKU-1")
                .title("Var Title")
                .stockQuantity(15)
                .priceAdjustment(BigDecimal.TEN)
                .build();

        ProductVariantDto dto = catalogMapper.toVariantDto(variant);
        assertThat(dto).isNotNull();
        assertThat(dto.getSku()).isEqualTo("SKU-1");

        List<ProductVariantDto> dtoList = catalogMapper.toVariantDtoList(List.of(variant));
        assertThat(dtoList).hasSize(1);

        ProductVariant fromDto = catalogMapper.toVariantEntity(dto);
        assertThat(fromDto).isNotNull();
        assertThat(fromDto.getSku()).isEqualTo("SKU-1");
    }

    @Test
    @DisplayName("should map ProductImage to DTO and list")
    void shouldMapImage() {
        ProductImage image = ProductImage.builder()
                .id(5L)
                .imageUrl("https://example.com/img.jpg")
                .primary(true)
                .displayOrder(1)
                .build();

        ProductImageDto dto = catalogMapper.toImageDto(image);
        assertThat(dto).isNotNull();
        assertThat(dto.getImageUrl()).isEqualTo("https://example.com/img.jpg");
        assertThat(dto.isPrimary()).isTrue();

        List<ProductImageDto> list = catalogMapper.toImageDtoList(List.of(image));
        assertThat(list).hasSize(1);
    }

    @Test
    @DisplayName("should map Product to ProductSummaryResponse with calculated stock and image")
    void shouldMapProductSummary() {
        Category cat = Category.builder().id(3L).name("Gadgets").slug("gadgets").build();

        ProductImage primaryImg = ProductImage.builder().imageUrl("/uploads/primary.jpg").primary(true).build();
        ProductImage secondImg = ProductImage.builder().imageUrl("/uploads/second.jpg").primary(false).build();

        ProductVariant v1 = ProductVariant.builder().sku("V1").stockQuantity(5).active(true).build();
        ProductVariant v2 = ProductVariant.builder().sku("V2").stockQuantity(10).active(true).build();
        ProductVariant v3 = ProductVariant.builder().sku("V3").stockQuantity(20).active(false).build();

        Product product = Product.builder()
                .id(10L)
                .category(cat)
                .name("Smart Device")
                .slug("smart-device")
                .basePrice(new BigDecimal("99.99"))
                .images(List.of(primaryImg, secondImg))
                .variants(List.of(v1, v2, v3))
                .build();

        ProductSummaryResponse summary = catalogMapper.toSummaryResponse(product);
        assertThat(summary).isNotNull();
        assertThat(summary.getCategoryName()).isEqualTo("Gadgets");
        assertThat(summary.getPrimaryImageUrl()).isEqualTo("/uploads/primary.jpg");
        assertThat(summary.isInStock()).isTrue();
        assertThat(summary.getTotalStock()).isEqualTo(15); // Only active variants 5 + 10
    }

    @Test
    @DisplayName("should handle empty images and variants safely in summary response")
    void shouldHandleEmptyImagesAndVariantsInSummary() {
        Category cat = Category.builder().id(3L).name("Gadgets").slug("gadgets").build();

        Product product = Product.builder()
                .id(11L)
                .category(cat)
                .name("Empty Device")
                .slug("empty-device")
                .basePrice(new BigDecimal("49.99"))
                .images(Collections.emptyList())
                .variants(Collections.emptyList())
                .build();

        ProductSummaryResponse summary = catalogMapper.toSummaryResponse(product);
        assertThat(summary.getPrimaryImageUrl()).isNull();
        assertThat(summary.isInStock()).isFalse();
        assertThat(summary.getTotalStock()).isEqualTo(0);
    }

    @Test
    @DisplayName("should map Product to ProductDetailResponse")
    void shouldMapProductDetail() {
        Category cat = Category.builder().id(3L).name("Gadgets").slug("gadgets").build();
        Product product = Product.builder()
                .id(12L)
                .category(cat)
                .name("Detail Device")
                .slug("detail-device")
                .basePrice(new BigDecimal("89.99"))
                .variants(new ArrayList<>())
                .images(new ArrayList<>())
                .build();

        ProductDetailResponse detail = catalogMapper.toDetailResponse(product);
        assertThat(detail).isNotNull();
        assertThat(detail.getName()).isEqualTo("Detail Device");
        assertThat(detail.getCategorySlug()).isEqualTo("gadgets");
    }
}


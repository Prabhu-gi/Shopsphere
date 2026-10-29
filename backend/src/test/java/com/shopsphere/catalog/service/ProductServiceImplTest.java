package com.shopsphere.catalog.service;

import com.shopsphere.catalog.dto.*;
import com.shopsphere.catalog.entity.Category;
import com.shopsphere.catalog.entity.Product;
import com.shopsphere.catalog.entity.ProductImage;
import com.shopsphere.catalog.entity.ProductVariant;
import com.shopsphere.catalog.mapper.CatalogMapper;
import com.shopsphere.catalog.repository.CategoryRepository;
import com.shopsphere.catalog.repository.ProductImageRepository;
import com.shopsphere.catalog.repository.ProductRepository;
import com.shopsphere.catalog.repository.ProductVariantRepository;
import com.shopsphere.common.dto.PagedResponse;
import com.shopsphere.common.exception.BadRequestException;
import com.shopsphere.common.exception.ResourceNotFoundException;
import com.shopsphere.common.storage.FileStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.mock.web.MockMultipartFile;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ProductVariantRepository productVariantRepository;

    @Mock
    private ProductImageRepository productImageRepository;

    @Mock
    private CatalogMapper catalogMapper;

    @Mock
    private FileStorageService fileStorageService;

    @InjectMocks
    private ProductServiceImpl productService;

    private Category testCategory;
    private Product testProduct;
    private ProductSummaryResponse testSummary;
    private ProductDetailResponse testDetail;

    @BeforeEach
    void setUp() {
        testCategory = Category.builder()
                .id(1L)
                .name("Electronics")
                .slug("electronics")
                .active(true)
                .build();

        testProduct = Product.builder()
                .id(10L)
                .category(testCategory)
                .name("Wireless Mouse")
                .slug("wireless-mouse")
                .description("Ergonomic mouse")
                .basePrice(new BigDecimal("29.99"))
                .active(true)
                .featured(true)
                .ratingAvg(new BigDecimal("4.50"))
                .reviewCount(10)
                .variants(new ArrayList<>())
                .images(new ArrayList<>())
                .build();

        testSummary = ProductSummaryResponse.builder()
                .id(10L)
                .categoryId(1L)
                .categoryName("Electronics")
                .name("Wireless Mouse")
                .slug("wireless-mouse")
                .basePrice(new BigDecimal("29.99"))
                .inStock(true)
                .build();

        testDetail = ProductDetailResponse.builder()
                .id(10L)
                .categoryId(1L)
                .categoryName("Electronics")
                .name("Wireless Mouse")
                .slug("wireless-mouse")
                .basePrice(new BigDecimal("29.99"))
                .build();
    }

    @Test
    @DisplayName("getProducts should return paginated response")
    void givenFilter_whenGetProducts_thenReturnsPagedResponse() {
        ProductFilterRequest filter = ProductFilterRequest.builder()
                .page(0)
                .size(10)
                .sort("price_asc")
                .build();

        Page<Product> page = new PageImpl<>(List.of(testProduct));
        given(productRepository.findAll(any(Specification.class), any(Pageable.class))).willReturn(page);
        given(catalogMapper.toSummaryResponse(testProduct)).willReturn(testSummary);

        PagedResponse<ProductSummaryResponse> result = productService.getProducts(filter);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("Wireless Mouse");
    }

    @Test
    @DisplayName("getFeaturedProducts should return featured items")
    void givenLimit_whenGetFeaturedProducts_thenReturnsList() {
        Page<Product> page = new PageImpl<>(List.of(testProduct));
        given(productRepository.findByFeaturedTrueAndActiveTrue(any(Pageable.class))).willReturn(page);
        given(catalogMapper.toSummaryResponse(testProduct)).willReturn(testSummary);

        List<ProductSummaryResponse> result = productService.getFeaturedProducts(5);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Wireless Mouse");
    }

    @Test
    @DisplayName("getProductByIdOrSlug should find by ID when input is numeric")
    void givenNumericId_whenGetProductByIdOrSlug_thenFindsById() {
        given(productRepository.findByIdAndActiveTrue(10L)).willReturn(Optional.of(testProduct));
        given(catalogMapper.toDetailResponse(testProduct)).willReturn(testDetail);

        ProductDetailResponse result = productService.getProductByIdOrSlug("10");

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(10L);
    }

    @Test
    @DisplayName("getProductByIdOrSlug should find by slug when input is text")
    void givenSlug_whenGetProductByIdOrSlug_thenFindsBySlug() {
        given(productRepository.findBySlugAndActiveTrue("wireless-mouse")).willReturn(Optional.of(testProduct));
        given(catalogMapper.toDetailResponse(testProduct)).willReturn(testDetail);

        ProductDetailResponse result = productService.getProductByIdOrSlug("wireless-mouse");

        assertThat(result).isNotNull();
        assertThat(result.getSlug()).isEqualTo("wireless-mouse");
    }

    @Test
    @DisplayName("createProduct should reject duplicate variant SKU")
    void givenDuplicateSku_whenCreateProduct_thenThrowsBadRequest() {
        ProductVariantDto variantDto = ProductVariantDto.builder()
                .sku("MOUSE-BLK")
                .title("Black")
                .build();

        ProductCreateRequest request = ProductCreateRequest.builder()
                .categoryId(1L)
                .name("Wireless Mouse")
                .basePrice(new BigDecimal("29.99"))
                .variants(List.of(variantDto))
                .build();

        given(categoryRepository.findById(1L)).willReturn(Optional.of(testCategory));
        given(productVariantRepository.existsBySku("MOUSE-BLK")).willReturn(true);

        assertThatThrownBy(() -> productService.createProduct(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("SKU already exists");
    }

    @Test
    @DisplayName("createProduct should succeed and save with default variant if none provided")
    void givenNoVariants_whenCreateProduct_thenCreatesDefaultVariantAndSaves() {
        ProductCreateRequest request = ProductCreateRequest.builder()
                .categoryId(1L)
                .name("Wireless Mouse")
                .basePrice(new BigDecimal("29.99"))
                .build();

        given(categoryRepository.findById(1L)).willReturn(Optional.of(testCategory));
        given(productRepository.save(any(Product.class))).willReturn(testProduct);
        given(catalogMapper.toDetailResponse(testProduct)).willReturn(testDetail);

        ProductDetailResponse result = productService.createProduct(request);

        assertThat(result).isNotNull();
        verify(productRepository).save(any(Product.class));
    }

    @Test
    @DisplayName("deleteProduct should mark product inactive")
    void givenValidId_whenDeleteProduct_thenMarksInactive() {
        given(productRepository.findById(10L)).willReturn(Optional.of(testProduct));

        productService.deleteProduct(10L);

        assertThat(testProduct.isActive()).isFalse();
        verify(productRepository).save(testProduct);
    }

    @Test
    @DisplayName("uploadProductImage should store file and add image to product")
    void givenValidFile_whenUploadProductImage_thenStoresAndReturnsDto() {
        MockMultipartFile file = new MockMultipartFile("file", "mouse.jpg", "image/jpeg", "bytes".getBytes());
        given(productRepository.findById(10L)).willReturn(Optional.of(testProduct));
        given(fileStorageService.store(file, "products")).willReturn("/uploads/products/mouse-uuid.jpg");

        ProductImage image = ProductImage.builder()
                .id(100L)
                .imageUrl("/uploads/products/mouse-uuid.jpg")
                .primary(true)
                .build();
        given(productImageRepository.save(any(ProductImage.class))).willReturn(image);

        ProductImageDto imageDto = ProductImageDto.builder()
                .id(100L)
                .imageUrl("/uploads/products/mouse-uuid.jpg")
                .primary(true)
                .build();
        given(catalogMapper.toImageDto(image)).willReturn(imageDto);

        ProductImageDto result = productService.uploadProductImage(10L, file, true);

        assertThat(result).isNotNull();
        assertThat(result.getImageUrl()).isEqualTo("/uploads/products/mouse-uuid.jpg");
    }

    @Test
    @DisplayName("updateProduct should update product and synchronize variants")
    void givenUpdateRequest_whenUpdateProduct_thenUpdatesAndSaves() {
        ProductVariant existingVariant = ProductVariant.builder()
                .id(20L)
                .sku("MOUSE-OLD")
                .title("Old")
                .stockQuantity(5)
                .build();
        testProduct.addVariant(existingVariant);

        ProductVariantDto updateVarDto = ProductVariantDto.builder()
                .id(20L)
                .sku("MOUSE-UPDATED")
                .title("Updated Title")
                .stockQuantity(15)
                .priceAdjustment(BigDecimal.ONE)
                .build();

        ProductVariantDto newVarDto = ProductVariantDto.builder()
                .sku("MOUSE-NEW")
                .title("New Variant")
                .stockQuantity(10)
                .priceAdjustment(BigDecimal.ZERO)
                .build();

        ProductUpdateRequest updateRequest = ProductUpdateRequest.builder()
                .categoryId(1L)
                .name("Updated Mouse")
                .description("New Description")
                .basePrice(new BigDecimal("34.99"))
                .active(true)
                .featured(true)
                .variants(List.of(updateVarDto, newVarDto))
                .build();

        given(productRepository.findById(10L)).willReturn(Optional.of(testProduct));
        given(categoryRepository.findById(1L)).willReturn(Optional.of(testCategory));
        given(productVariantRepository.existsBySkuAndIdNot("MOUSE-UPDATED", 20L)).willReturn(false);
        given(productVariantRepository.existsBySku("MOUSE-NEW")).willReturn(false);
        given(catalogMapper.toVariantEntity(newVarDto)).willReturn(ProductVariant.builder().sku("MOUSE-NEW").build());
        given(productRepository.save(any(Product.class))).willReturn(testProduct);
        given(catalogMapper.toDetailResponse(testProduct)).willReturn(testDetail);

        ProductDetailResponse result = productService.updateProduct(10L, updateRequest);
        assertThat(result).isNotNull();
        verify(productRepository).save(testProduct);
    }

    @Test
    @DisplayName("deleteProductImage should delete image entity and physical file")
    void givenImageId_whenDeleteProductImage_thenRemovesAndDeletesFile() {
        ProductImage image = ProductImage.builder()
                .id(50L)
                .product(testProduct)
                .imageUrl("/uploads/products/image.jpg")
                .primary(true)
                .build();
        testProduct.addImage(image);

        given(productImageRepository.findById(50L)).willReturn(Optional.of(image));

        productService.deleteProductImage(50L);

        verify(productImageRepository).delete(image);
        verify(fileStorageService).delete("/uploads/products/image.jpg");
    }

    @Test
    @DisplayName("setPrimaryProductImage should set selected image to primary")
    void givenImageId_whenSetPrimaryProductImage_thenUpdatesPrimaryFlag() {
        ProductImage img1 = ProductImage.builder().id(1L).product(testProduct).primary(true).build();
        ProductImage img2 = ProductImage.builder().id(2L).product(testProduct).primary(false).build();
        testProduct.addImage(img1);
        testProduct.addImage(img2);

        given(productRepository.findById(10L)).willReturn(Optional.of(testProduct));

        productService.setPrimaryProductImage(10L, 2L);

        assertThat(img1.isPrimary()).isFalse();
        assertThat(img2.isPrimary()).isTrue();
        verify(productRepository).save(testProduct);
    }
}


package com.shopsphere.catalog.repository;

import com.shopsphere.catalog.dto.ProductFilterRequest;
import com.shopsphere.catalog.entity.Category;
import com.shopsphere.catalog.entity.Product;
import com.shopsphere.catalog.entity.ProductVariant;
import com.shopsphere.catalog.specification.ProductSpecification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class ProductRepositoryTest {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private Category electronics;
    private Product product1;
    private Product product2;

    @BeforeEach
    void setUp() {
        electronics = Category.builder()
                .name("Electronics")
                .slug("electronics")
                .active(true)
                .build();
        categoryRepository.save(electronics);

        product1 = Product.builder()
                .category(electronics)
                .name("Wireless Noise Canceling Headphones")
                .slug("wireless-noise-canceling-headphones")
                .description("High quality audio")
                .basePrice(new BigDecimal("199.99"))
                .active(true)
                .featured(true)
                .ratingAvg(new BigDecimal("4.80"))
                .reviewCount(15)
                .build();

        ProductVariant v1 = ProductVariant.builder()
                .sku("HEAD-BLK")
                .title("Black")
                .stockQuantity(10)
                .active(true)
                .build();
        product1.addVariant(v1);

        product2 = Product.builder()
                .category(electronics)
                .name("Budget Wired Earbuds")
                .slug("budget-wired-earbuds")
                .description("Simple headphones")
                .basePrice(new BigDecimal("19.99"))
                .active(true)
                .featured(false)
                .ratingAvg(new BigDecimal("3.50"))
                .reviewCount(5)
                .build();

        ProductVariant v2 = ProductVariant.builder()
                .sku("EAR-WHT")
                .title("White")
                .stockQuantity(0)
                .active(true)
                .build();
        product2.addVariant(v2);

        productRepository.saveAll(List.of(product1, product2));
    }

    @Test
    @DisplayName("should filter products by search keyword and category")
    void testSearchAndCategoryFilter() {
        ProductFilterRequest filter = ProductFilterRequest.builder()
                .categorySlug("electronics")
                .search("canceling")
                .build();

        Specification<Product> spec = ProductSpecification.buildSpecification(filter, true);
        Page<Product> result = productRepository.findAll(spec, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getName()).contains("Noise Canceling");
    }

    @Test
    @DisplayName("should filter products by price range and min rating")
    void testPriceAndRatingFilter() {
        ProductFilterRequest filter = ProductFilterRequest.builder()
                .minPrice(new BigDecimal("50.00"))
                .maxPrice(new BigDecimal("250.00"))
                .minRating(new BigDecimal("4.00"))
                .featured(true)
                .build();

        Specification<Product> spec = ProductSpecification.buildSpecification(filter, true);
        Page<Product> result = productRepository.findAll(spec, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getSlug()).isEqualTo("wireless-noise-canceling-headphones");
    }

    @Test
    @DisplayName("should filter products by in stock only")
    void testInStockFilter() {
        ProductFilterRequest filter = ProductFilterRequest.builder()
                .inStockOnly(true)
                .build();

        Specification<Product> spec = ProductSpecification.buildSpecification(filter, true);
        Page<Product> result = productRepository.findAll(spec, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getSlug()).isEqualTo("wireless-noise-canceling-headphones");
    }
}


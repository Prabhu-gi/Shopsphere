package com.shopsphere.catalog.dto;

import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductDetailResponse implements Serializable {

    private Long id;
    private Long categoryId;
    private String categoryName;
    private String categorySlug;
    private String name;
    private String slug;
    private String description;
    private BigDecimal basePrice;
    private boolean active;
    private boolean featured;
    private BigDecimal ratingAvg;
    private Integer reviewCount;
    private LocalDateTime createdAt;

    @Builder.Default
    private List<ProductImageDto> images = new ArrayList<>();

    @Builder.Default
    private List<ProductVariantDto> variants = new ArrayList<>();
}


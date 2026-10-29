package com.shopsphere.catalog.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductFilterRequest {

    private Long categoryId;
    private String categorySlug;
    private String search;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private BigDecimal minRating;
    private Boolean inStockOnly;
    private Boolean featured;
    @Builder.Default
    private String sort = "newest";
    @Builder.Default
    private int page = 0;
    @Builder.Default
    private int size = 12;
}


package com.shopsphere.catalog.dto;

import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductSummaryResponse implements Serializable {

    private Long id;
    private Long categoryId;
    private String categoryName;
    private String categorySlug;
    private String name;
    private String slug;
    private BigDecimal basePrice;
    private boolean active;
    private boolean featured;
    private BigDecimal ratingAvg;
    private Integer reviewCount;
    private String primaryImageUrl;
    private boolean inStock;
    private Integer totalStock;
}


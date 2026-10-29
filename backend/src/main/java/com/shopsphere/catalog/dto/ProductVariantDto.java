package com.shopsphere.catalog.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductVariantDto implements Serializable {

    private Long id;

    @NotBlank(message = "Variant SKU is required")
    private String sku;

    @NotBlank(message = "Variant title is required")
    private String title;

    private String sizeAttr;

    private String colorAttr;

    @DecimalMin(value = "0.00", message = "Price adjustment cannot be negative")
    @Builder.Default
    private BigDecimal priceAdjustment = BigDecimal.ZERO;

    @Min(value = 0, message = "Stock quantity cannot be negative")
    @Builder.Default
    private Integer stockQuantity = 0;

    @Builder.Default
    private Boolean active = true;
}


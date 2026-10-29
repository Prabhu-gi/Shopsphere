package com.shopsphere.catalog.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductCreateRequest {

    @NotNull(message = "Category ID is required")
    private Long categoryId;

    @NotBlank(message = "Product name is required")
    @Size(max = 255, message = "Product name must not exceed 255 characters")
    private String name;

    private String description;

    @NotNull(message = "Base price is required")
    @DecimalMin(value = "0.00", message = "Base price cannot be negative")
    private BigDecimal basePrice;

    @Builder.Default
    private Boolean active = true;

    @Builder.Default
    private Boolean featured = false;

    @Valid
    @Builder.Default
    private List<ProductVariantDto> variants = new ArrayList<>();
}


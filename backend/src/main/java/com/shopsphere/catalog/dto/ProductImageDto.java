package com.shopsphere.catalog.dto;

import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductImageDto implements Serializable {

    private Long id;
    private String imageUrl;
    private boolean primary;
    private int displayOrder;
}


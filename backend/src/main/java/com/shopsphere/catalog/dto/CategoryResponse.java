package com.shopsphere.catalog.dto;

import lombok.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryResponse implements Serializable {

    private Long id;
    private Long parentId;
    private String name;
    private String slug;
    private String description;
    private String imageUrl;
    private boolean active;

    @Builder.Default
    private List<CategoryResponse> subcategories = new ArrayList<>();
}


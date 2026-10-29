package com.shopsphere.catalog.controller;

import com.shopsphere.catalog.dto.CategoryResponse;
import com.shopsphere.catalog.service.CategoryService;
import com.shopsphere.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
@Tag(name = "Catalog - Categories", description = "Public Category browsing endpoints")
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    @Operation(summary = "Get category tree", description = "Retrieves hierarchical tree of active categories")
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getCategories(
            @RequestParam(name = "tree", defaultValue = "true") boolean tree) {
        List<CategoryResponse> categories = tree
                ? categoryService.getCategoryTree()
                : categoryService.getAllCategories();
        return ResponseEntity.ok(ApiResponse.success(categories));
    }

    @GetMapping("/{idOrSlug}")
    @Operation(summary = "Get category by ID or slug")
    public ResponseEntity<ApiResponse<CategoryResponse>> getCategory(@PathVariable String idOrSlug) {
        CategoryResponse response = idOrSlug.matches("\\d+")
                ? categoryService.getCategoryById(Long.parseLong(idOrSlug))
                : categoryService.getCategoryBySlug(idOrSlug);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}


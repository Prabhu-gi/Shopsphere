package com.shopsphere.catalog.service;

import com.shopsphere.catalog.dto.CategoryRequest;
import com.shopsphere.catalog.dto.CategoryResponse;

import java.util.List;

public interface CategoryService {

    List<CategoryResponse> getCategoryTree();

    List<CategoryResponse> getAllCategories();

    CategoryResponse getCategoryById(Long id);

    CategoryResponse getCategoryBySlug(String slug);

    CategoryResponse createCategory(CategoryRequest request);

    CategoryResponse updateCategory(Long id, CategoryRequest request);

    void deleteCategory(Long id);
}


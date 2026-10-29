package com.shopsphere.catalog.service;

import com.shopsphere.catalog.dto.CategoryRequest;
import com.shopsphere.catalog.dto.CategoryResponse;
import com.shopsphere.catalog.entity.Category;
import com.shopsphere.catalog.mapper.CatalogMapper;
import com.shopsphere.catalog.repository.CategoryRepository;
import com.shopsphere.common.exception.BadRequestException;
import com.shopsphere.common.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private CatalogMapper catalogMapper;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    private Category testCategory;
    private CategoryResponse testResponse;

    @BeforeEach
    void setUp() {
        testCategory = Category.builder()
                .id(1L)
                .name("Electronics")
                .slug("electronics")
                .description("All gadgets")
                .active(true)
                .build();

        testResponse = CategoryResponse.builder()
                .id(1L)
                .name("Electronics")
                .slug("electronics")
                .active(true)
                .build();
    }

    @Test
    @DisplayName("getCategoryTree should return active root categories")
    void givenCategories_whenGetCategoryTree_thenReturnsList() {
        given(categoryRepository.findByParentIsNullAndActiveTrue()).willReturn(List.of(testCategory));
        given(catalogMapper.toCategoryResponseList(List.of(testCategory))).willReturn(List.of(testResponse));

        List<CategoryResponse> result = categoryService.getCategoryTree();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Electronics");
    }

    @Test
    @DisplayName("getCategoryById should return category response when found")
    void givenValidId_whenGetCategoryById_thenReturnsCategory() {
        given(categoryRepository.findById(1L)).willReturn(Optional.of(testCategory));
        given(catalogMapper.toCategoryResponse(testCategory)).willReturn(testResponse);

        CategoryResponse result = categoryService.getCategoryById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("getCategoryById should throw ResourceNotFoundException when not found")
    void givenInvalidId_whenGetCategoryById_thenThrowsNotFound() {
        given(categoryRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.getCategoryById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("getCategoryBySlug should return category response when found")
    void givenValidSlug_whenGetCategoryBySlug_thenReturnsCategory() {
        given(categoryRepository.findBySlug("electronics")).willReturn(Optional.of(testCategory));
        given(catalogMapper.toCategoryResponse(testCategory)).willReturn(testResponse);

        CategoryResponse result = categoryService.getCategoryBySlug("electronics");

        assertThat(result).isNotNull();
        assertThat(result.getSlug()).isEqualTo("electronics");
    }

    @Test
    @DisplayName("createCategory should successfully generate slug and save")
    void givenValidRequest_whenCreateCategory_thenSavesAndReturnsResponse() {
        CategoryRequest request = CategoryRequest.builder()
                .name("Smart Phones")
                .description("Mobile devices")
                .build();

        given(categoryRepository.existsBySlug("smart-phones")).willReturn(false);
        given(categoryRepository.save(any(Category.class))).willReturn(testCategory);
        given(catalogMapper.toCategoryResponse(testCategory)).willReturn(testResponse);

        CategoryResponse result = categoryService.createCategory(request);

        assertThat(result).isNotNull();
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    @DisplayName("getAllCategories should return all categories")
    void givenCategories_whenGetAllCategories_thenReturnsList() {
        given(categoryRepository.findAll()).willReturn(List.of(testCategory));
        given(catalogMapper.toCategoryResponseList(List.of(testCategory))).willReturn(List.of(testResponse));

        List<CategoryResponse> result = categoryService.getAllCategories();
        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("createCategory with parent and slug collision should resolve properly")
    void givenParentAndCollision_whenCreateCategory_thenSaves() {
        Category parent = Category.builder().id(5L).name("Root").slug("root").build();
        CategoryRequest request = CategoryRequest.builder()
                .name("Smart Phones")
                .parentId(5L)
                .build();

        given(categoryRepository.existsBySlug("smart-phones")).willReturn(true);
        given(categoryRepository.findById(5L)).willReturn(Optional.of(parent));
        given(categoryRepository.save(any(Category.class))).willReturn(testCategory);
        given(catalogMapper.toCategoryResponse(testCategory)).willReturn(testResponse);

        CategoryResponse result = categoryService.createCategory(request);
        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("updateCategory should successfully update properties")
    void givenValidRequest_whenUpdateCategory_thenUpdates() {
        CategoryRequest request = CategoryRequest.builder()
                .name("Updated Electronics")
                .description("New desc")
                .active(true)
                .build();

        given(categoryRepository.findById(1L)).willReturn(Optional.of(testCategory));
        given(categoryRepository.save(any(Category.class))).willReturn(testCategory);
        given(catalogMapper.toCategoryResponse(testCategory)).willReturn(testResponse);

        CategoryResponse result = categoryService.updateCategory(1L, request);
        assertThat(result).isNotNull();
        verify(categoryRepository).save(testCategory);
    }

    @Test
    @DisplayName("updateCategory should throw BadRequest when category is set as own parent")
    void givenSelfParent_whenUpdateCategory_thenThrowsBadRequest() {
        CategoryRequest request = CategoryRequest.builder()
                .name("Electronics")
                .parentId(1L)
                .build();

        given(categoryRepository.findById(1L)).willReturn(Optional.of(testCategory));

        assertThatThrownBy(() -> categoryService.updateCategory(1L, request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("cannot be its own parent");
    }

    @Test
    @DisplayName("deleteCategory should delete found category")
    void givenValidId_whenDeleteCategory_thenDeletesFromRepo() {
        given(categoryRepository.findById(1L)).willReturn(Optional.of(testCategory));

        categoryService.deleteCategory(1L);

        verify(categoryRepository).delete(testCategory);
    }
}


package com.shopsphere.catalog.service;

import com.shopsphere.catalog.dto.*;
import com.shopsphere.catalog.entity.Category;
import com.shopsphere.catalog.entity.Product;
import com.shopsphere.catalog.entity.ProductImage;
import com.shopsphere.catalog.entity.ProductVariant;
import com.shopsphere.catalog.mapper.CatalogMapper;
import com.shopsphere.catalog.repository.CategoryRepository;
import com.shopsphere.catalog.repository.ProductImageRepository;
import com.shopsphere.catalog.repository.ProductRepository;
import com.shopsphere.catalog.repository.ProductVariantRepository;
import com.shopsphere.catalog.specification.ProductSpecification;
import com.shopsphere.common.dto.PagedResponse;
import com.shopsphere.common.exception.BadRequestException;
import com.shopsphere.common.exception.ResourceNotFoundException;
import com.shopsphere.common.storage.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private static final Pattern NONLATIN = Pattern.compile("[^\\w-]");
    private static final Pattern WHITESPACE = Pattern.compile("[\\s]");

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductVariantRepository productVariantRepository;
    private final ProductImageRepository productImageRepository;
    private final CatalogMapper catalogMapper;
    private final FileStorageService fileStorageService;

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<ProductSummaryResponse> getProducts(ProductFilterRequest filter) {
        Sort sort = resolveSort(filter != null ? filter.getSort() : null);
        int page = filter != null && filter.getPage() >= 0 ? filter.getPage() : 0;
        int size = filter != null && filter.getSize() > 0 ? Math.min(filter.getSize(), 50) : 12;

        Pageable pageable = PageRequest.of(page, size, sort);
        Specification<Product> spec = ProductSpecification.buildSpecification(filter, true);

        Page<Product> productPage = productRepository.findAll(spec, pageable);
        List<ProductSummaryResponse> content = productPage.getContent().stream()
                .map(catalogMapper::toSummaryResponse)
                .collect(Collectors.toList());

        return PagedResponse.<ProductSummaryResponse>builder()
                .content(content)
                .pageNumber(productPage.getNumber())
                .pageSize(productPage.getSize())
                .totalElements(productPage.getTotalElements())
                .totalPages(productPage.getTotalPages())
                .isLast(productPage.isLast())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "featuredProducts", key = "#limit")
    public List<ProductSummaryResponse> getFeaturedProducts(int limit) {
        Pageable pageable = PageRequest.of(0, Math.min(limit, 20), Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Product> featuredPage = productRepository.findByFeaturedTrueAndActiveTrue(pageable);
        return featuredPage.getContent().stream()
                .map(catalogMapper::toSummaryResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ProductDetailResponse getProductByIdOrSlug(String idOrSlug) {
        Product product;
        if (idOrSlug.matches("\\d+")) {
            Long id = Long.parseLong(idOrSlug);
            product = productRepository.findByIdAndActiveTrue(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
        } else {
            product = productRepository.findBySlugAndActiveTrue(idOrSlug)
                    .orElseThrow(() -> new ResourceNotFoundException("Product", "slug", idOrSlug));
        }
        return catalogMapper.toDetailResponse(product);
    }

    @Override
    @Transactional
    @CacheEvict(value = "featuredProducts", allEntries = true)
    public ProductDetailResponse createProduct(ProductCreateRequest request) {
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", request.getCategoryId()));

        String slug = generateUniqueSlug(request.getName(), null);

        Product product = Product.builder()
                .category(category)
                .name(request.getName().trim())
                .slug(slug)
                .description(request.getDescription())
                .basePrice(request.getBasePrice())
                .active(request.getActive() != null ? request.getActive() : true)
                .featured(request.getFeatured() != null ? request.getFeatured() : false)
                .ratingAvg(BigDecimal.ZERO)
                .reviewCount(0)
                .build();

        if (request.getVariants() != null && !request.getVariants().isEmpty()) {
            for (ProductVariantDto variantDto : request.getVariants()) {
                if (productVariantRepository.existsBySku(variantDto.getSku().trim())) {
                    throw new BadRequestException("SKU already exists: " + variantDto.getSku());
                }
                ProductVariant variant = catalogMapper.toVariantEntity(variantDto);
                product.addVariant(variant);
            }
        } else {
            // Create a default variant if none provided
            String defaultSku = slug.toUpperCase() + "-DEFAULT";
            if (productVariantRepository.existsBySku(defaultSku)) {
                defaultSku = defaultSku + "-" + System.currentTimeMillis();
            }
            ProductVariant defaultVariant = ProductVariant.builder()
                    .sku(defaultSku)
                    .title("Standard")
                    .priceAdjustment(BigDecimal.ZERO)
                    .stockQuantity(10)
                    .active(true)
                    .build();
            product.addVariant(defaultVariant);
        }

        Product saved = productRepository.save(product);
        log.info("Created product ID: {}, slug: {}", saved.getId(), saved.getSlug());
        return catalogMapper.toDetailResponse(saved);
    }

    @Override
    @Transactional
    @CacheEvict(value = "featuredProducts", allEntries = true)
    public ProductDetailResponse updateProduct(Long id, ProductUpdateRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", request.getCategoryId()));

        product.setCategory(category);
        product.setName(request.getName().trim());
        product.setDescription(request.getDescription());
        product.setBasePrice(request.getBasePrice());
        if (request.getActive() != null) {
            product.setActive(request.getActive());
        }
        if (request.getFeatured() != null) {
            product.setFeatured(request.getFeatured());
        }

        // Synchronize variants
        if (request.getVariants() != null) {
            Map<Long, ProductVariant> existingMap = product.getVariants().stream()
                    .filter(v -> v.getId() != null)
                    .collect(Collectors.toMap(ProductVariant::getId, v -> v));

            List<ProductVariant> updatedList = new ArrayList<>();
            for (ProductVariantDto dto : request.getVariants()) {
                if (dto.getId() != null && existingMap.containsKey(dto.getId())) {
                    // Update existing
                    ProductVariant existing = existingMap.get(dto.getId());
                    if (productVariantRepository.existsBySkuAndIdNot(dto.getSku().trim(), dto.getId())) {
                        throw new BadRequestException("SKU already taken: " + dto.getSku());
                    }
                    existing.setSku(dto.getSku().trim());
                    existing.setTitle(dto.getTitle().trim());
                    existing.setSizeAttr(dto.getSizeAttr());
                    existing.setColorAttr(dto.getColorAttr());
                    existing.setPriceAdjustment(dto.getPriceAdjustment() != null ? dto.getPriceAdjustment() : BigDecimal.ZERO);
                    existing.setStockQuantity(dto.getStockQuantity() != null ? dto.getStockQuantity() : 0);
                    if (dto.getActive() != null) {
                        existing.setActive(dto.getActive());
                    }
                    updatedList.add(existing);
                } else {
                    // Add new variant
                    if (productVariantRepository.existsBySku(dto.getSku().trim())) {
                        throw new BadRequestException("SKU already exists: " + dto.getSku());
                    }
                    ProductVariant newVar = catalogMapper.toVariantEntity(dto);
                    newVar.setProduct(product);
                    updatedList.add(newVar);
                }
            }
            product.getVariants().clear();
            product.getVariants().addAll(updatedList);
        }

        Product updated = productRepository.save(product);
        log.info("Updated product ID: {}", updated.getId());
        return catalogMapper.toDetailResponse(updated);
    }

    @Override
    @Transactional
    @CacheEvict(value = "featuredProducts", allEntries = true)
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
        // Soft-delete by marking inactive
        product.setActive(false);
        productRepository.save(product);
        log.info("Deactivated product ID: {}", id);
    }

    @Override
    @Transactional
    public ProductImageDto uploadProductImage(Long productId, MultipartFile file, boolean isPrimary) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

        String imageUrl = fileStorageService.store(file, "products");

        if (isPrimary || product.getImages().isEmpty()) {
            product.getImages().forEach(img -> img.setPrimary(false));
            isPrimary = true;
        }

        int nextOrder = product.getImages().size();
        ProductImage image = ProductImage.builder()
                .product(product)
                .imageUrl(imageUrl)
                .primary(isPrimary)
                .displayOrder(nextOrder)
                .build();

        product.addImage(image);
        ProductImage saved = productImageRepository.save(image);
        log.info("Uploaded image ID: {} for product ID: {}", saved.getId(), productId);
        return catalogMapper.toImageDto(saved);
    }

    @Override
    @Transactional
    public void deleteProductImage(Long imageId) {
        ProductImage image = productImageRepository.findById(imageId)
                .orElseThrow(() -> new ResourceNotFoundException("ProductImage", "id", imageId));

        String fileUrl = image.getImageUrl();
        Product product = image.getProduct();
        product.removeImage(image);
        productImageRepository.delete(image);

        // If removed image was primary and other images exist, make first primary
        if (image.isPrimary() && !product.getImages().isEmpty()) {
            product.getImages().get(0).setPrimary(true);
            productImageRepository.save(product.getImages().get(0));
        }

        fileStorageService.delete(fileUrl);
        log.info("Deleted product image ID: {}", imageId);
    }

    @Override
    @Transactional
    public void setPrimaryProductImage(Long productId, Long imageId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

        boolean found = false;
        for (ProductImage img : product.getImages()) {
            if (img.getId().equals(imageId)) {
                img.setPrimary(true);
                found = true;
            } else {
                img.setPrimary(false);
            }
        }

        if (!found) {
            throw new ResourceNotFoundException("ProductImage", "id", imageId);
        }

        productRepository.save(product);
        log.info("Set image ID: {} as primary for product ID: {}", imageId, productId);
    }

    private Sort resolveSort(String sortParam) {
        if (sortParam == null) {
            return Sort.by(Sort.Direction.DESC, "createdAt");
        }
        return switch (sortParam.toLowerCase()) {
            case "price_asc" -> Sort.by(Sort.Direction.ASC, "basePrice");
            case "price_desc" -> Sort.by(Sort.Direction.DESC, "basePrice");
            case "rating_desc" -> Sort.by(Sort.Direction.DESC, "ratingAvg");
            default -> Sort.by(Sort.Direction.DESC, "createdAt");
        };
    }

    private String generateUniqueSlug(String name, Long currentId) {
        String base = toSlug(name);
        String slug = base;
        int counter = 1;
        while (currentId == null ? productRepository.existsBySlug(slug) : productRepository.existsBySlugAndIdNot(slug, currentId)) {
            slug = base + "-" + counter++;
        }
        return slug;
    }

    private String toSlug(String input) {
        String nowhitespace = WHITESPACE.matcher(input.trim()).replaceAll("-");
        String normalized = Normalizer.normalize(nowhitespace, Normalizer.Form.NFD);
        String slug = NONLATIN.matcher(normalized).replaceAll("");
        return slug.toLowerCase(Locale.ENGLISH);
    }
}


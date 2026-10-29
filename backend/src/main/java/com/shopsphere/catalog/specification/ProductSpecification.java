package com.shopsphere.catalog.specification;

import com.shopsphere.catalog.dto.ProductFilterRequest;
import com.shopsphere.catalog.entity.Product;
import com.shopsphere.catalog.entity.ProductVariant;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class ProductSpecification {

    public static Specification<Product> buildSpecification(ProductFilterRequest filter, Boolean activeOnly) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Active filter
            if (Boolean.TRUE.equals(activeOnly)) {
                predicates.add(cb.isTrue(root.get("active")));
            }

            if (filter == null) {
                return cb.and(predicates.toArray(new Predicate[0]));
            }

            // Category filter
            if (filter.getCategoryId() != null) {
                predicates.add(cb.equal(root.get("category").get("id"), filter.getCategoryId()));
            } else if (StringUtils.hasText(filter.getCategorySlug())) {
                predicates.add(cb.equal(root.get("category").get("slug"), filter.getCategorySlug().trim()));
            }

            // Keyword Search (Name or Description)
            if (StringUtils.hasText(filter.getSearch())) {
                String term = "%" + filter.getSearch().trim().toLowerCase() + "%";
                Predicate nameMatch = cb.like(cb.lower(root.get("name")), term);
                Predicate descMatch = cb.like(cb.lower(root.get("description")), term);
                predicates.add(cb.or(nameMatch, descMatch));
            }

            // Min Price
            if (filter.getMinPrice() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("basePrice"), filter.getMinPrice()));
            }

            // Max Price
            if (filter.getMaxPrice() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("basePrice"), filter.getMaxPrice()));
            }

            // Rating
            if (filter.getMinRating() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("ratingAvg"), filter.getMinRating()));
            }

            // Featured
            if (Boolean.TRUE.equals(filter.getFeatured())) {
                predicates.add(cb.isTrue(root.get("featured")));
            }

            // In Stock only
            if (Boolean.TRUE.equals(filter.getInStockOnly())) {
                Subquery<Long> subquery = query.subquery(Long.class);
                var subRoot = subquery.from(ProductVariant.class);
                subquery.select(subRoot.get("product").get("id"))
                        .where(
                                cb.equal(subRoot.get("product").get("id"), root.get("id")),
                                cb.isTrue(subRoot.get("active")),
                                cb.greaterThan(subRoot.get("stockQuantity"), 0)
                        );
                predicates.add(cb.exists(subquery));
            }

            // Distinct to avoid duplicates if joins are used
            query.distinct(true);

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}


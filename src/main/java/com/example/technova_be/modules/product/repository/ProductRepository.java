package com.example.technova_be.modules.product.repository;

import com.example.technova_be.modules.product.dto.CategoryCountProjection;
import com.example.technova_be.modules.product.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {

    long countByIsActiveTrue();

    @Query(
        "SELECT c.name as name, COUNT(p) as count " +
        "FROM Product p JOIN p.category c " +
        "WHERE p.isActive = true " +
        "GROUP BY c.name " +
        "ORDER BY COUNT(p) DESC"
    )
    List<CategoryCountProjection> countActiveProductsByCategory();

    @Query("SELECT p FROM Product p JOIN p.category c WHERE " +
            "(COALESCE(:searchKeyword, '') = '' OR LOWER(p.name) LIKE LOWER(CONCAT('%', :searchKeyword, '%'))) AND " +
            "(COALESCE(:category, '') = '' OR LOWER(CAST(c.name AS string)) = LOWER(:category)) AND " +
            "(COALESCE(:minPrice, 0) = 0 OR p.price >= :minPrice) AND " +
            "(COALESCE(:maxPrice, 0) = 0 OR p.price <= :maxPrice) AND " +
            "p.isActive = :status")
    Page<Product> findAllWithFilters(
            @Param("searchKeyword") String keyword,
            @Param("category") String category,
            @Param("minPrice") Double minPrice,
            @Param("maxPrice") Double maxPrice,
            @Param("status") Boolean status,
            Pageable pageable
    );

    List<Product> findByNameContainingIgnoreCase(String keyword);

    @EntityGraph(attributePaths = {"category", "variants", "variants.attributes", "images"})
    List<Product> findByIsActiveTrue();

    @EntityGraph(attributePaths = {"category", "variants", "variants.attributes", "images"})
    List<Product> findByIsActiveTrueOrderByCreatedDateDesc(Pageable pageable);

    @EntityGraph(attributePaths = {"category", "variants", "variants.attributes", "images"})
    List<Product> findByIsActiveTrueAndIdIn(List<UUID> ids);
}

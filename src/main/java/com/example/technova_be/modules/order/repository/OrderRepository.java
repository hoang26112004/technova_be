package com.example.technova_be.modules.order.repository;

import com.example.technova_be.modules.order.dto.ProductCountProjection;
import com.example.technova_be.modules.order.dto.OrderSalesPointProjection;
import com.example.technova_be.comom.constants.OrderStatus;
import com.example.technova_be.modules.order.entity.Order;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID>, JpaSpecificationExecutor<Order> {
    Optional<Order> findByReference(String reference);

    long countByStatusNot(OrderStatus status);

    @org.springframework.data.jpa.repository.Query(
        "SELECT o.createdDate as createdDate, o.totalAmount as totalAmount " +
        "FROM Order o " +
        "WHERE o.status <> :excludedStatus " +
        "AND o.createdDate >= :start " +
        "AND o.createdDate < :endExclusive"
    )
    List<OrderSalesPointProjection> findSalesPoints(
        @org.springframework.data.repository.query.Param("start") LocalDateTime start,
        @org.springframework.data.repository.query.Param("endExclusive") LocalDateTime endExclusive,
        @org.springframework.data.repository.query.Param("excludedStatus") OrderStatus excludedStatus
    );

    @org.springframework.data.jpa.repository.Query(
        "SELECT COUNT(o) > 0 FROM Order o JOIN o.orderItems oi " +
        "JOIN ProductVariant pv ON oi.variantId = pv.id " +
        "WHERE o.userId = :userId " +
        "AND (oi.productId = :productId OR pv.product.id = :productId) " +
        "AND o.status = com.example.technova_be.comom.constants.OrderStatus.DELIVERED"
    )
    boolean hasUserPurchasedProduct(@org.springframework.data.repository.query.Param("userId") Long userId,
                                    @org.springframework.data.repository.query.Param("productId") UUID productId);

    @org.springframework.data.jpa.repository.Query(
        "SELECT oi.productId as productId, SUM(oi.quantity) as count " +
        "FROM Order o JOIN o.orderItems oi " +
        "JOIN Product p ON p.id = oi.productId " +
        "WHERE o.status <> com.example.technova_be.comom.constants.OrderStatus.CANCELLED " +
        "AND o.createdDate >= :since " +
        "AND oi.productId IS NOT NULL " +
        "AND p.isActive = true " +
        "GROUP BY oi.productId " +
        "ORDER BY SUM(oi.quantity) DESC"
    )
    List<ProductCountProjection> findTrendingProductCounts(
        @org.springframework.data.repository.query.Param("since") LocalDateTime since,
        Pageable pageable
    );

    @org.springframework.data.jpa.repository.Query(
        "SELECT oi2.productId as productId, SUM(oi2.quantity) as count " +
        "FROM Order o JOIN o.orderItems oi1 JOIN o.orderItems oi2 " +
        "JOIN Product p ON p.id = oi2.productId " +
        "WHERE oi1.productId IN :seedProductIds " +
        "AND oi2.productId NOT IN :seedProductIds " +
        "AND oi2.productId IS NOT NULL " +
        "AND o.status <> com.example.technova_be.comom.constants.OrderStatus.CANCELLED " +
        "AND p.isActive = true " +
        "GROUP BY oi2.productId " +
        "ORDER BY SUM(oi2.quantity) DESC"
    )
    List<ProductCountProjection> findCoPurchasedProductCounts(
        @org.springframework.data.repository.query.Param("seedProductIds") List<UUID> seedProductIds,
        Pageable pageable
    );

    @org.springframework.data.jpa.repository.Query(
        "SELECT oi.productId FROM Order o JOIN o.orderItems oi " +
        "WHERE o.userId = :userId " +
        "AND o.status <> com.example.technova_be.comom.constants.OrderStatus.CANCELLED " +
        "AND oi.productId IS NOT NULL " +
        "ORDER BY o.createdDate DESC"
    )
    List<UUID> findPurchasedProductIds(
        @org.springframework.data.repository.query.Param("userId") Long userId,
        Pageable pageable
    );
}

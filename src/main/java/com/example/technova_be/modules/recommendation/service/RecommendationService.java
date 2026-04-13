package com.example.technova_be.modules.recommendation.service;

import com.example.technova_be.modules.cart.entity.Cart;
import com.example.technova_be.modules.cart.entity.CartItem;
import com.example.technova_be.modules.cart.repository.CartRepository;
import com.example.technova_be.modules.order.dto.ProductCountProjection;
import com.example.technova_be.modules.order.repository.OrderRepository;
import com.example.technova_be.modules.product.dto.ProductResponse;
import com.example.technova_be.modules.product.entity.Product;
import com.example.technova_be.modules.product.entity.ProductVariant;
import com.example.technova_be.modules.product.repository.ProductRepository;
import com.example.technova_be.modules.product.util.ProductMapperUtil;
import com.example.technova_be.modules.recommendation.dto.RecommendationResponse;
import com.example.technova_be.modules.review.dto.ProductRatingProjection;
import com.example.technova_be.modules.review.repository.ReviewRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RecommendationService {
    private static final int RECOMMEND_LIMIT = 10;
    private static final int SEED_LIMIT = 5;
    private static final int PURCHASED_EXCLUDE_LIMIT = 100;
    private static final int CO_PURCHASE_LIMIT = 50;
    private static final int TREND_LIMIT = 50;
    private static final int RATING_LIMIT = 50;
    private static final int MIN_REVIEW_COUNT = 3;
    private static final int TREND_DAYS = 30;
    private static final double WEIGHT_COPURCHASE = 0.6;
    private static final double WEIGHT_TREND = 0.3;
    private static final double WEIGHT_RATING = 0.1;

    private final ProductRepository productRepository;
    private final ProductMapperUtil productMapperUtil;
    private final OrderRepository orderRepository;
    private final ReviewRepository reviewRepository;
    private final CartRepository cartRepository;

    public RecommendationService(
            ProductRepository productRepository,
            ProductMapperUtil productMapperUtil,
            OrderRepository orderRepository,
            ReviewRepository reviewRepository,
            CartRepository cartRepository
    ) {
        this.productRepository = productRepository;
        this.productMapperUtil = productMapperUtil;
        this.orderRepository = orderRepository;
        this.reviewRepository = reviewRepository;
        this.cartRepository = cartRepository;
    }

    @Transactional(readOnly = true)
    public RecommendationResponse getHomeRecommendations(Long userId) {
        Instant generatedAt = Instant.now();

        List<UUID> cartProductIds = userId == null ? List.of() : getCartProductIds(userId);
        List<UUID> purchasedProductIds = userId == null ? List.of() : getPurchasedProductIds(userId);

        List<UUID> seedIds = !cartProductIds.isEmpty()
                ? cartProductIds
                : purchasedProductIds.stream().limit(SEED_LIMIT).toList();

        Map<UUID, Double> scores = new HashMap<>();
        if (!seedIds.isEmpty()) {
            List<ProductCountProjection> coPurchase = orderRepository.findCoPurchasedProductCounts(
                    seedIds,
                    PageRequest.of(0, CO_PURCHASE_LIMIT)
            );
            addCountScores(scores, coPurchase, WEIGHT_COPURCHASE);
        }

        List<ProductCountProjection> trending = orderRepository.findTrendingProductCounts(
                LocalDateTime.now().minusDays(TREND_DAYS),
                PageRequest.of(0, TREND_LIMIT)
        );
        addCountScores(scores, trending, WEIGHT_TREND);

        List<ProductRatingProjection> topRated = reviewRepository.findTopRatedProducts(
                MIN_REVIEW_COUNT,
                PageRequest.of(0, RATING_LIMIT)
        );
        addRatingScores(scores, topRated, WEIGHT_RATING);

        Set<UUID> excluded = new HashSet<>();
        excluded.addAll(cartProductIds);
        excluded.addAll(purchasedProductIds);
        excluded.addAll(seedIds);

        List<UUID> rankedIds = scores.entrySet().stream()
                .sorted(Map.Entry.<UUID, Double>comparingByValue().reversed())
                .map(Map.Entry::getKey)
                .filter(id -> !excluded.contains(id))
                .limit(RECOMMEND_LIMIT)
                .toList();

        List<ProductResponse> recommendations = mapToResponses(rankedIds);
        if (recommendations.size() < RECOMMEND_LIMIT) {
            recommendations = fillWithLatest(recommendations, excluded, RECOMMEND_LIMIT);
        }

        List<ProductResponse> seedResponses = mapToResponses(seedIds);
        if (seedResponses.isEmpty()) {
            seedResponses = productRepository.findByIsActiveTrueOrderByCreatedDateDesc(
                    PageRequest.of(0, SEED_LIMIT)
            ).stream().map(productMapperUtil::toProductResponse).toList();
        }

        return new RecommendationResponse(
                seedResponses,
                recommendations,
                "hybrid-behavioral-v1",
                generatedAt
        );
    }

    private List<UUID> getCartProductIds(Long userId) {
        return cartRepository.findByUserId(userId)
                .map(this::extractProductIdsFromCart)
                .orElse(List.of());
    }

    private List<UUID> extractProductIdsFromCart(Cart cart) {
        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            return List.of();
        }
        List<UUID> ids = new ArrayList<>();
        for (CartItem item : cart.getItems()) {
            ProductVariant variant = item.getVariant();
            if (variant == null || variant.getProduct() == null) continue;
            if (variant.getProduct().getId() != null) {
                ids.add(variant.getProduct().getId());
            }
        }
        return unique(ids);
    }

    private List<UUID> getPurchasedProductIds(Long userId) {
        List<UUID> ids = orderRepository.findPurchasedProductIds(
                userId,
                PageRequest.of(0, PURCHASED_EXCLUDE_LIMIT)
        );
        return unique(ids);
    }

    private void addCountScores(Map<UUID, Double> scores, List<ProductCountProjection> counts, double weight) {
        if (counts == null || counts.isEmpty()) return;
        long max = counts.get(0).getCount() == null ? 0 : counts.get(0).getCount();
        if (max <= 0) return;
        for (ProductCountProjection row : counts) {
            UUID productId = row.getProductId();
            Long count = row.getCount();
            if (productId == null || count == null || count <= 0) continue;
            double normalized = count / (double) max;
            scores.merge(productId, weight * normalized, Double::sum);
        }
    }

    private void addRatingScores(Map<UUID, Double> scores, List<ProductRatingProjection> ratings, double weight) {
        if (ratings == null || ratings.isEmpty()) return;
        for (ProductRatingProjection row : ratings) {
            UUID productId = row.getProductId();
            Double avgRating = row.getAvgRating();
            Long reviewCount = row.getReviewCount();
            if (productId == null || avgRating == null || reviewCount == null) continue;
            if (avgRating <= 0 || reviewCount < MIN_REVIEW_COUNT) continue;
            double ratingScore = avgRating / 5.0;
            double volumeBoost = Math.min(1.0, reviewCount / (double) MIN_REVIEW_COUNT);
            scores.merge(productId, weight * ratingScore * volumeBoost, Double::sum);
        }
    }

    private List<ProductResponse> mapToResponses(List<UUID> ids) {
        if (ids == null || ids.isEmpty()) return List.of();
        List<Product> products = productRepository.findByIsActiveTrueAndIdIn(ids);
        Map<UUID, Product> productById = products.stream()
                .collect(Collectors.toMap(Product::getId, p -> p));
        return ids.stream()
                .map(productById::get)
                .filter(Objects::nonNull)
                .map(productMapperUtil::toProductResponse)
                .toList();
    }

    private List<ProductResponse> fillWithLatest(
            List<ProductResponse> existing,
            Set<UUID> excluded,
            int limit
    ) {
        List<ProductResponse> result = new ArrayList<>(existing);
        if (result.size() >= limit) return result;
        List<Product> latest = productRepository.findByIsActiveTrueOrderByCreatedDateDesc(
                PageRequest.of(0, limit)
        );
        for (Product product : latest) {
            if (product == null || product.getId() == null) continue;
            if (excluded.contains(product.getId())) continue;
            boolean exists = result.stream().anyMatch(r -> product.getId().equals(r.getId()));
            if (exists) continue;
            result.add(productMapperUtil.toProductResponse(product));
            if (result.size() >= limit) break;
        }
        return result;
    }

    private List<UUID> unique(List<UUID> ids) {
        if (ids == null || ids.isEmpty()) return List.of();
        return new ArrayList<>(new LinkedHashSet<>(ids));
    }
}

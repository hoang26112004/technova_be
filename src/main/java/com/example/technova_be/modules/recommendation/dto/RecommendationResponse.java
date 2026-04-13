package com.example.technova_be.modules.recommendation.dto;

import com.example.technova_be.modules.product.dto.ProductResponse;

import java.time.Instant;
import java.util.List;

public record RecommendationResponse(
        List<ProductResponse> seeds,
        List<ProductResponse> recommendations,
        String strategy,
        Instant generatedAt
) {}

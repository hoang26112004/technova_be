package com.example.technova_be.modules.review.dto;

import java.util.UUID;

public interface ProductRatingProjection {
    UUID getProductId();
    Double getAvgRating();
    Long getReviewCount();
}

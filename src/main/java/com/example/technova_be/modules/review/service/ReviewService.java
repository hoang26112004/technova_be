package com.example.technova_be.modules.review.service;

import com.example.technova_be.comom.response.PageResponse;
import com.example.technova_be.modules.review.dto.ReviewRequest;
import com.example.technova_be.modules.review.dto.ReviewResponse;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ReviewService {
    ReviewResponse createReview(Long userId, ReviewRequest request);
    
    PageResponse<ReviewResponse> getReviewsByProduct(UUID productId, Pageable pageable);
    
    void deleteReview(Long userId, Long reviewId);
}

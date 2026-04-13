package com.example.technova_be.modules.review.dto;

import com.example.technova_be.modules.product.dto.ProductResponse;
import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TopRatedProductResponse {
    ProductResponse product;
    Double avgRating;
    Long reviewCount;
}


package com.example.technova_be.modules.recommendation.controller;

import com.example.technova_be.comom.response.GlobalResponse;
import com.example.technova_be.comom.exception.BadRequestException;
import com.example.technova_be.modules.recommendation.dto.RecommendationResponse;
import com.example.technova_be.modules.recommendation.service.RecommendationService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/recommendations")
public class RecommendationController {
    private final RecommendationService recommendationService;

    public RecommendationController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    @GetMapping("/home")
    public GlobalResponse<RecommendationResponse> home(Authentication auth) {
        return GlobalResponse.ok(recommendationService.getHomeRecommendations(getOptionalUserId(auth)));
    }

    private Long getOptionalUserId(Authentication auth) {
        if (auth == null || auth.getName() == null) {
            return null;
        }
        try {
            return Long.parseLong(auth.getName());
        } catch (NumberFormatException ex) {
            throw new BadRequestException("Invalid user id");
        }
    }
}

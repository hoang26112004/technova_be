package com.example.technova_be.modules.order.dto;

import java.util.UUID;

public record OrderItemResponse(
        UUID productId,
        UUID variantId,
        Integer quantity,
        Double price
) {}

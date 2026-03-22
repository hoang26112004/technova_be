package com.example.technova_be.modules.product.dto;

import java.util.UUID;

public record ProductPriceResponse(
        UUID variantId,
        String productName,
        Integer quantity,
        double price
) {
}

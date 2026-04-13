package com.example.technova_be.modules.order.dto;

import java.time.LocalDateTime;

public interface OrderSalesPointProjection {
    LocalDateTime getCreatedDate();
    Double getTotalAmount();
}


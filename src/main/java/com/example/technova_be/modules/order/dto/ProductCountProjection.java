package com.example.technova_be.modules.order.dto;

import java.util.UUID;

public interface ProductCountProjection {
    UUID getProductId();
    Long getCount();
}

package com.example.technova_be.modules.order.dto;

public record PaymentResponse(
        String paymentUrl,
        String status,
        String message
) {}
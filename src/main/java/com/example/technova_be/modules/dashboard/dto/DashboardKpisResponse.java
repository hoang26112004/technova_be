package com.example.technova_be.modules.dashboard.dto;

public record DashboardKpisResponse(
        Double totalSales,
        Long newUsers,
        Long totalProducts,
        Double ordersPerUser
) {}


package com.example.technova_be.modules.dashboard.dto;

import java.util.List;

public record DashboardOverviewResponse(
        DashboardKpisResponse kpis,
        List<MonthlySalesResponse> monthlySales,
        List<CategoryCountResponse> categoryDistribution
) {}


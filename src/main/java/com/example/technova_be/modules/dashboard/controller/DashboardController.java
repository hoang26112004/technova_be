package com.example.technova_be.modules.dashboard.controller;

import com.example.technova_be.comom.response.GlobalResponse;
import com.example.technova_be.modules.dashboard.dto.DashboardOverviewResponse;
import com.example.technova_be.modules.dashboard.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/overview")
    public GlobalResponse<DashboardOverviewResponse> overview() {
        return GlobalResponse.ok(dashboardService.getOverview());
    }
}


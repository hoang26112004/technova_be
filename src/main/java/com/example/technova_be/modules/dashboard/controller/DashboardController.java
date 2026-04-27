package com.example.technova_be.modules.dashboard.controller;

import com.example.technova_be.comom.response.GlobalResponse;
import com.example.technova_be.modules.dashboard.dto.DashboardOverviewResponse;
import com.example.technova_be.modules.dashboard.dto.WeeklySalesResponse;
import com.example.technova_be.modules.dashboard.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping(value = "/overview", produces = "application/json;charset=UTF-8")
    public GlobalResponse<DashboardOverviewResponse> overview() {
        return GlobalResponse.ok(dashboardService.getOverview());
    }

    @GetMapping(value = "/weekly-sales", produces = "application/json;charset=UTF-8")
    public GlobalResponse<List<WeeklySalesResponse>> weeklySalesByMonth(
            @RequestParam(name = "year", required = false) Integer year,
            @RequestParam(name = "month", required = false) Integer month
    ) {
        LocalDate today = LocalDate.now();
        int y = (year == null ? today.getYear() : year);
        int m = (month == null ? today.getMonthValue() : month);
        if (m < 1 || m > 12) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "month must be between 1 and 12");
        }
        return GlobalResponse.ok(dashboardService.getWeeklySalesByMonth(y, m));
    }
}

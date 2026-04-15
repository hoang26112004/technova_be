package com.example.technova_be.modules.dashboard.service;

import com.example.technova_be.comom.constants.OrderStatus;
import com.example.technova_be.modules.dashboard.dto.CategoryCountResponse;
import com.example.technova_be.modules.dashboard.dto.DashboardKpisResponse;
import com.example.technova_be.modules.dashboard.dto.DashboardOverviewResponse;
import com.example.technova_be.modules.dashboard.dto.MonthlySalesResponse;
import com.example.technova_be.modules.order.dto.OrderSalesPointProjection;
import com.example.technova_be.modules.order.repository.OrderRepository;
import com.example.technova_be.modules.product.dto.CategoryCountProjection;
import com.example.technova_be.modules.product.repository.ProductRepository;
import com.example.technova_be.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private static final DateTimeFormatter MONTH_LABEL =
            DateTimeFormatter.ofPattern("MMM", Locale.ENGLISH);

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    public DashboardOverviewResponse getOverview() {
        LocalDateTime now = LocalDateTime.now();
        YearMonth currentMonth = YearMonth.from(now);
        YearMonth startMonth = currentMonth.minusMonths(11);

        LocalDateTime start = startMonth.atDay(1).atStartOfDay();
        LocalDateTime endExclusive = currentMonth.plusMonths(1).atDay(1).atStartOfDay();

        List<OrderSalesPointProjection> points = orderRepository.findSalesPoints(start, endExclusive, OrderStatus.CANCELLED);

        Map<YearMonth, Double> salesByMonth = new LinkedHashMap<>();
        for (int i = 0; i < 12; i++) {
            YearMonth ym = startMonth.plusMonths(i);
            salesByMonth.put(ym, 0.0);
        }

        double totalSales = 0.0;
        for (OrderSalesPointProjection p : points) {
            if (p == null || p.getCreatedDate() == null) continue;
            YearMonth ym = YearMonth.from(p.getCreatedDate());
            if (!salesByMonth.containsKey(ym)) continue;
            double amount = p.getTotalAmount() == null ? 0.0 : p.getTotalAmount();
            salesByMonth.put(ym, salesByMonth.get(ym) + amount);
            totalSales += amount;
        }

        long totalOrders = orderRepository.countByStatusNot(OrderStatus.CANCELLED);
        long totalUsers = userRepository.count();
        double ordersPerUser = totalUsers > 0 ? (double) totalOrders / (double) totalUsers : 0.0;

        long newUsers = userRepository.countByCreatedAtAfter(Instant.now().minus(Duration.ofDays(30)));
        long totalProducts = productRepository.countByIsActiveTrue();

        List<MonthlySalesResponse> monthlySales = new ArrayList<>(12);
        for (Map.Entry<YearMonth, Double> e : salesByMonth.entrySet()) {
            String label = e.getKey().atDay(1).format(MONTH_LABEL);
            monthlySales.add(new MonthlySalesResponse(label, e.getValue()));
        }

        List<CategoryCountProjection> categoryCounts = productRepository.countActiveProductsByCategory();
        List<CategoryCountResponse> categoryDistribution = categoryCounts.stream()
                .map(p -> new CategoryCountResponse(p.getName(), p.getCount()))
                .toList();

        return new DashboardOverviewResponse(
                new DashboardKpisResponse(totalSales, newUsers, totalProducts, ordersPerUser),
                monthlySales,
                categoryDistribution
        );
    }
}


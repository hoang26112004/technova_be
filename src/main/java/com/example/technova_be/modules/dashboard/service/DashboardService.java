package com.example.technova_be.modules.dashboard.service;

import com.example.technova_be.comom.constants.OrderStatus;
import com.example.technova_be.modules.dashboard.dto.CategorySalesProjection;
import com.example.technova_be.modules.dashboard.dto.CategorySalesResponse;
import com.example.technova_be.modules.dashboard.dto.CategoryCountResponse;
import com.example.technova_be.modules.dashboard.dto.DashboardKpisResponse;
import com.example.technova_be.modules.dashboard.dto.DashboardOverviewResponse;
import com.example.technova_be.modules.dashboard.dto.MonthlySalesResponse;
import com.example.technova_be.modules.dashboard.dto.WeeklySalesResponse;
import com.example.technova_be.modules.order.dto.OrderSalesPointProjection;
import com.example.technova_be.modules.order.repository.OrderRepository;
import com.example.technova_be.modules.product.dto.CategoryCountProjection;
import com.example.technova_be.modules.product.repository.ProductRepository;
import com.example.technova_be.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DashboardService {

    // Keep locale import for other existing code paths if needed elsewhere.

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    private static String formatMonthLabel(YearMonth ym) {
        // Use numeric month label (e.g. T4) instead of "Apr".
        return "T" + ym.getMonthValue();
    }

    private static String formatWeekLabel(LocalDate weekStart) {
        // Label by the week ending (Sunday) month to avoid edge cases where week starts in prev month.
        LocalDate weekEnd = weekStart.plusDays(6);
        int month = weekEnd.getMonthValue();
        int year = weekEnd.getYear();

        LocalDate firstDayOfMonth = LocalDate.of(year, month, 1);
        LocalDate firstWeekStart = firstDayOfMonth.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        int weekNo = (int) ChronoUnit.WEEKS.between(firstWeekStart, weekStart) + 1;

        return "T" + month + " - Tuần " + weekNo;
    }

    public List<WeeklySalesResponse> getWeeklySalesByMonth(int year, int month) {
        YearMonth ym = YearMonth.of(year, month);
        LocalDateTime start = ym.atDay(1).atStartOfDay();
        LocalDateTime endExclusive = ym.plusMonths(1).atDay(1).atStartOfDay();

        List<OrderSalesPointProjection> points =
                orderRepository.findSalesPoints(start, endExclusive, OrderStatus.CANCELLED);

        // Week 1..5 within the month: 1-7, 8-14, 15-21, 22-28, 29-end.
        Map<Integer, Double> salesByWeekOfMonth = new LinkedHashMap<>();
        for (int w = 1; w <= 5; w++) salesByWeekOfMonth.put(w, 0.0);

        for (OrderSalesPointProjection p : points) {
            if (p == null || p.getCreatedDate() == null) continue;
            int day = p.getCreatedDate().getDayOfMonth();
            int weekOfMonth = ((day - 1) / 7) + 1;
            if (weekOfMonth < 1) weekOfMonth = 1;
            if (weekOfMonth > 5) weekOfMonth = 5;
            double amount = p.getTotalAmount() == null ? 0.0 : p.getTotalAmount();
            salesByWeekOfMonth.put(weekOfMonth, salesByWeekOfMonth.get(weekOfMonth) + amount);
        }

        List<WeeklySalesResponse> res = new ArrayList<>(5);
        for (int w = 1; w <= 5; w++) {
            res.add(new WeeklySalesResponse("Tuần " + w, salesByWeekOfMonth.get(w)));
        }
        return res;
    }

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
            monthlySales.add(new MonthlySalesResponse(formatMonthLabel(e.getKey()), e.getValue()));
        }

        List<CategoryCountProjection> categoryCounts = productRepository.countActiveProductsByCategory();
        List<CategoryCountResponse> categoryDistribution = categoryCounts.stream()
                .map(p -> new CategoryCountResponse(p.getName(), p.getCount()))
                .toList();

        // Weekly sales for the last 12 weeks (including current week)
        LocalDate today = LocalDate.now();
        LocalDate currentWeekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate startWeekStart = currentWeekStart.minusWeeks(11);
        LocalDateTime weekStart = startWeekStart.atStartOfDay();
        LocalDateTime weekEndExclusive = currentWeekStart.plusWeeks(1).atStartOfDay();

        List<OrderSalesPointProjection> weeklyPoints =
                orderRepository.findSalesPoints(weekStart, weekEndExclusive, OrderStatus.CANCELLED);

        Map<LocalDate, Double> salesByWeek = new LinkedHashMap<>();
        for (int i = 0; i < 12; i++) {
            salesByWeek.put(startWeekStart.plusWeeks(i), 0.0);
        }
        for (OrderSalesPointProjection p : weeklyPoints) {
            if (p == null || p.getCreatedDate() == null) continue;
            LocalDate weekKey = p.getCreatedDate().toLocalDate()
                    .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            if (!salesByWeek.containsKey(weekKey)) continue;
            double amount = p.getTotalAmount() == null ? 0.0 : p.getTotalAmount();
            salesByWeek.put(weekKey, salesByWeek.get(weekKey) + amount);
        }
        List<WeeklySalesResponse> weeklySales = new ArrayList<>(12);
        for (Map.Entry<LocalDate, Double> e : salesByWeek.entrySet()) {
            weeklySales.add(new WeeklySalesResponse(formatWeekLabel(e.getKey()), e.getValue()));
        }

        List<CategorySalesProjection> categorySales =
                orderRepository.findSalesByCategory(weekStart, weekEndExclusive, OrderStatus.CANCELLED);
        List<CategorySalesResponse> salesByCategory = categorySales.stream()
                .map(p -> new CategorySalesResponse(p.getName(), p.getSales() == null ? 0.0 : p.getSales()))
                .toList();

        return new DashboardOverviewResponse(
                new DashboardKpisResponse(totalSales, newUsers, totalProducts, ordersPerUser),
                monthlySales,
                categoryDistribution,
                weeklySales,
                salesByCategory
        );
    }
}

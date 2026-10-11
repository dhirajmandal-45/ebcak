package ecommerce.back.controller;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ecommerce.back.entity.Order;
import ecommerce.back.repository.OrderRepository;

@RestController
@RequestMapping("/growth")
public class GrowthController {

    private final OrderRepository orderRepository;

    public GrowthController(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    // ============================================================
    // 1. OVERALL GROWTH
    // GET /growth
    // ============================================================

    @GetMapping
    public ResponseEntity<Map<String, Object>> getGrowth() {

        List<Order> orders = orderRepository.findAll();

        double totalRevenue = 0.0;
        int totalOrders = orders.size();

        Map<String, Double> monthlyRevenue = new LinkedHashMap<>();
        Map<String, Integer> monthlyOrders = new LinkedHashMap<>();

        for (Month month : Month.values()) {
            monthlyRevenue.put(month.name(), 0.0);
            monthlyOrders.put(month.name(), 0);
        }

        for (Order order : orders) {

            if (order.getTotalAmount() != null) {
                totalRevenue += order.getTotalAmount();
            }

            LocalDateTime createdAt = order.getCreatedAt();

            if (createdAt != null) {

                String monthName = createdAt.getMonth().name();

                monthlyRevenue.put(
                        monthName,
                        monthlyRevenue.get(monthName)
                                + (order.getTotalAmount() != null
                                        ? order.getTotalAmount()
                                        : 0.0));

                monthlyOrders.put(
                        monthName,
                        monthlyOrders.get(monthName) + 1);
            }
        }

        long totalCustomers = orders.stream()
                .map(Order::getUser)
                .filter(user -> user != null)
                .map(user -> user.getId())
                .distinct()
                .count();

        double averageOrderValue = totalOrders == 0
                ? 0.0
                : totalRevenue / totalOrders;

        Map<String, Object> response = new LinkedHashMap<>();

        response.put("totalRevenue", totalRevenue);
        response.put("totalOrders", totalOrders);
        response.put("totalCustomers", totalCustomers);
        response.put("averageOrderValue", averageOrderValue);
        response.put("monthlyRevenue", monthlyRevenue);
        response.put("monthlyOrders", monthlyOrders);

        return ResponseEntity.ok(response);
    }

    // ============================================================
    // 2. CURRENT YEAR SALES
    // GET /growth/sales
    // ============================================================

    @GetMapping("/sales")
    public ResponseEntity<Map<String, Double>> getCurrentYearSales() {

        int currentYear = LocalDateTime.now().getYear();

        Map<String, Double> sales = new LinkedHashMap<>();

        for (Month month : Month.values()) {
            sales.put(month.name(), 0.0);
        }

        List<Order> orders = orderRepository.findAll();

        for (Order order : orders) {

            LocalDateTime createdAt = order.getCreatedAt();

            if (createdAt == null) {
                continue;
            }

            if (createdAt.getYear() != currentYear) {
                continue;
            }

            double amount = order.getTotalAmount() == null
                    ? 0.0
                    : order.getTotalAmount();

            String monthName = createdAt.getMonth().name();

            sales.put(
                    monthName,
                    sales.get(monthName) + amount);
        }

        return ResponseEntity.ok(sales);
    }

    // ============================================================
    // 3. CURRENT YEAR ORDER COUNT
    // GET /growth/orders
    // ============================================================

    @GetMapping("/orders")
    public ResponseEntity<Map<String, Integer>> getCurrentYearOrders() {

        int currentYear = LocalDateTime.now().getYear();

        Map<String, Integer> ordersByMonth = new LinkedHashMap<>();

        for (Month month : Month.values()) {
            ordersByMonth.put(month.name(), 0);
        }

        List<Order> orders = orderRepository.findAll();

        for (Order order : orders) {

            LocalDateTime createdAt = order.getCreatedAt();

            if (createdAt == null) {
                continue;
            }

            if (createdAt.getYear() != currentYear) {
                continue;
            }

            String monthName = createdAt.getMonth().name();

            ordersByMonth.put(
                    monthName,
                    ordersByMonth.get(monthName) + 1);
        }

        return ResponseEntity.ok(ordersByMonth);
    }

    // ============================================================
    // 4. TOP SELLING PRODUCTS
    // GET /growth/top-products
    // ============================================================

    @GetMapping("/top-products")
    public ResponseEntity<List<Map<String, Object>>> getTopSellingProducts() {

        List<Order> orders = orderRepository.findAll();

        Map<Integer, Map<String, Object>> productSales = new HashMap<>();

        for (Order order : orders) {

            if (order.getItems() == null) {
                continue;
            }

            order.getItems().forEach(item -> {

                if (item.getProduct() == null) {
                    return;
                }

                Integer productId = item.getProduct().getId();

                Map<String, Object> productData = productSales.computeIfAbsent(
                        productId,
                        id -> {

                            Map<String, Object> data = new HashMap<>();

                            data.put("productId", id);
                            data.put(
                                    "productName",
                                    item.getProductName());
                            data.put("quantitySold", 0);
                            data.put("totalSales", 0.0);

                            return data;
                        });

                int currentQuantity = (Integer) productData.get("quantitySold");

                double currentSales = (Double) productData.get("totalSales");

                productData.put(
                        "quantitySold",
                        currentQuantity + item.getQuantity());

                productData.put(
                        "totalSales",
                        currentSales + item.getSubtotal());
            });
        }

        List<Map<String, Object>> result = new ArrayList<>(productSales.values());

        result.sort((a, b) -> Integer.compare(
                (Integer) b.get("quantitySold"),
                (Integer) a.get("quantitySold")));

        return ResponseEntity.ok(result);
    }

    // ============================================================
    // 5. CUSTOMER GROWTH
    // GET /growth/customers
    // ============================================================

    @GetMapping("/customers")
    public ResponseEntity<Map<String, Integer>> getCustomerGrowth() {

        int currentYear = LocalDateTime.now().getYear();

        Map<String, Set<Long>> customersByMonth = new LinkedHashMap<>();

        for (Month month : Month.values()) {

            customersByMonth.put(
                    month.name(),
                    new HashSet<>());
        }

        List<Order> orders = orderRepository.findAll();

        for (Order order : orders) {

            LocalDateTime createdAt = order.getCreatedAt();

            if (createdAt == null) {
                continue;
            }

            if (createdAt.getYear() != currentYear) {
                continue;
            }

            if (order.getUser() == null ||
                    order.getUser().getId() == null) {
                continue;
            }

            String monthName = createdAt.getMonth().name();

            customersByMonth
                    .get(monthName)
                    .add(order.getUser().getId());
        }

        Map<String, Integer> result = new LinkedHashMap<>();

        for (Month month : Month.values()) {

            String monthName = month.name();

            result.put(
                    monthName,
                    customersByMonth.get(monthName).size());
        }

        return ResponseEntity.ok(result);
    }

    // ============================================================
    // 6. ORDER STATUS SUMMARY
    // GET /growth/status
    // ============================================================

    @GetMapping("/status")
    public ResponseEntity<Map<String, Integer>> getOrderStatusSummary() {

        Map<String, Integer> statusSummary = new LinkedHashMap<>();

        List<Order> orders = orderRepository.findAll();

        for (Order order : orders) {

            String status = order.getStatus();

            if (status == null || status.isBlank()) {
                status = "UNKNOWN";
            }

            status = status.toUpperCase();

            statusSummary.put(
                    status,
                    statusSummary.getOrDefault(status, 0) + 1);
        }

        return ResponseEntity.ok(statusSummary);
    }

    // ============================================================
    // 7. REVENUE BY ORDER STATUS
    // GET /growth/status-revenue
    // ============================================================

    @GetMapping("/status-revenue")
    public ResponseEntity<Map<String, Double>> getRevenueByStatus() {

        Map<String, Double> revenueByStatus = new LinkedHashMap<>();

        List<Order> orders = orderRepository.findAll();

        for (Order order : orders) {

            String status = order.getStatus();

            if (status == null || status.isBlank()) {
                status = "UNKNOWN";
            }

            status = status.toUpperCase();

            double amount = order.getTotalAmount() == null
                    ? 0.0
                    : order.getTotalAmount();

            revenueByStatus.put(
                    status,
                    revenueByStatus.getOrDefault(status, 0.0)
                            + amount);
        }

        return ResponseEntity.ok(revenueByStatus);
    }
}
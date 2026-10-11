package ecommerce.back.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import ecommerce.back.entity.Order;
import ecommerce.back.entity.User;
import ecommerce.back.repository.OrderRepository;
import ecommerce.back.repository.UserRepository;

@RestController
@RequestMapping("/admin/customers")
public class AdminCustomerController {

    private final UserRepository userRepository;
    private final OrderRepository orderRepository;

    public AdminCustomerController(
            UserRepository userRepository,
            OrderRepository orderRepository) {

        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
    }

    // ==========================================
    // ONLY CUSTOMERS WHO PLACED AN ORDER
    // ==========================================

    @GetMapping
    public ResponseEntity<List<User>> getOrderedCustomers() {

        List<User> customers = orderRepository.findCustomersWhoPlacedOrders();

        return ResponseEntity.ok(customers);
    }

    // ==========================================
    // CUSTOMER DETAILS
    // ==========================================

    @GetMapping("/{id}")
    public ResponseEntity<User> getCustomer(
            @PathVariable Long id) {

        User customer = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        return ResponseEntity.ok(customer);
    }

    // ==========================================
    // CUSTOMER ORDERS
    // ==========================================

    @GetMapping("/{id}/orders")
    public ResponseEntity<List<Order>> getCustomerOrders(
            @PathVariable Long id) {

        User customer = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        List<Order> orders = orderRepository.findByUserOrderByCreatedAtDesc(customer);

        return ResponseEntity.ok(orders);
    }
}
package ecommerce.back.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ecommerce.back.entity.Order;
import ecommerce.back.entity.User;
import ecommerce.back.repository.UserRepository;
import ecommerce.back.service.OrderService;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;
    private final UserRepository userRepository;

    public OrderController(
            OrderService orderService,
            UserRepository userRepository) {

        this.orderService = orderService;
        this.userRepository = userRepository;
    }

    // ==========================================
    // CREATE ORDER FROM CART
    // ==========================================

    @PostMapping
    public ResponseEntity<Order> createOrder(
            @RequestBody CreateOrderRequest request,
            Authentication authentication) {

        // Get logged-in user's email from JWT
        String email = authentication.getName();

        // Find user from database
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Create order using user's cart
        Order order = orderService.createOrder(
                user,
                request.getShippingAddress(),
                request.getShippingCity(),
                request.getShippingState(),
                request.getShippingPincode());

        return ResponseEntity.ok(order);
    }

    // ==========================================
    // GET MY ORDERS
    // ==========================================

    @GetMapping("/my-orders")
    public ResponseEntity<List<Order>> getMyOrders(
            Authentication authentication) {

        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Order> orders = orderService.getOrdersByUser(user);

        return ResponseEntity.ok(orders);
    }

    // ==========================================
    // CREATE ORDER REQUEST
    // ==========================================

    public static class CreateOrderRequest {

        private String shippingAddress;
        private String shippingCity;
        private String shippingState;
        private String shippingPincode;

        public CreateOrderRequest() {
        }

        public String getShippingAddress() {
            return shippingAddress;
        }

        public void setShippingAddress(String shippingAddress) {
            this.shippingAddress = shippingAddress;
        }

        public String getShippingCity() {
            return shippingCity;
        }

        public void setShippingCity(String shippingCity) {
            this.shippingCity = shippingCity;
        }

        public String getShippingState() {
            return shippingState;
        }

        public void setShippingState(String shippingState) {
            this.shippingState = shippingState;
        }

        public String getShippingPincode() {
            return shippingPincode;
        }

        public void setShippingPincode(String shippingPincode) {
            this.shippingPincode = shippingPincode;
        }
    }
}
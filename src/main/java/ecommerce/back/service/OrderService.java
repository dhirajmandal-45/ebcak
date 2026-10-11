package ecommerce.back.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ecommerce.back.entity.CartItem;
import ecommerce.back.entity.Order;
import ecommerce.back.entity.OrderItem;
import ecommerce.back.entity.Product;
import ecommerce.back.entity.User;
import ecommerce.back.repository.CartItemRepository;
import ecommerce.back.repository.OrderRepository;
import ecommerce.back.repository.ProductRepository;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final CartItemRepository cartItemRepository;

    public OrderService(
            OrderRepository orderRepository,
            ProductRepository productRepository,
            CartItemRepository cartItemRepository) {

        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.cartItemRepository = cartItemRepository;
    }

    // ==========================================
    // CREATE ORDER FROM CART
    // ==========================================

    @Transactional
    public Order createOrder(
            User user,
            String shippingAddress,
            String shippingCity,
            String shippingState,
            String shippingPincode) {

        // Get user's cart
        List<CartItem> cartItems = cartItemRepository.findByUser(user);

        if (cartItems == null || cartItems.isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }

        Order order = new Order();

        order.setUser(user);
        order.setStatus("PENDING");
        order.setShippingAddress(shippingAddress);
        order.setShippingCity(shippingCity);
        order.setShippingState(shippingState);
        order.setShippingPincode(shippingPincode);
        order.setCreatedAt(LocalDateTime.now());

        List<OrderItem> orderItems = new ArrayList<>();

        double totalAmount = 0.0;

        // ==========================================
        // PROCESS CART ITEMS
        // ==========================================

        for (CartItem cartItem : cartItems) {

            Product product = cartItem.getProduct();

            Integer quantity = cartItem.getQuantity();

            // ==========================================
            // CHECK QUANTITY
            // ==========================================

            if (quantity == null || quantity <= 0) {
                throw new RuntimeException(
                        "Invalid quantity for product: "
                                + product.getName());
            }

            // ==========================================
            // CHECK STOCK
            // ==========================================

            if (product.getStock() < quantity) {
                throw new RuntimeException(
                        "Not enough stock for product: "
                                + product.getName());
            }

            // ==========================================
            // CALCULATE PRICE
            // ==========================================

            double price = product.getPrice();

            double subtotal = price * quantity;

            // ==========================================
            // CREATE ORDER ITEM
            // ==========================================

            OrderItem orderItem = new OrderItem();

            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setProductName(product.getName());
            orderItem.setPrice(price);
            orderItem.setQuantity(quantity);
            orderItem.setSubtotal(subtotal);

            orderItems.add(orderItem);

            totalAmount += subtotal;

            // ==========================================
            // REDUCE STOCK
            // ==========================================

            product.setStock(product.getStock() - quantity);

            productRepository.save(product);
        }

        // ==========================================
        // SAVE ORDER
        // ==========================================

        order.setTotalAmount(totalAmount);
        order.setItems(orderItems);

        Order savedOrder = orderRepository.save(order);

        // ==========================================
        // CLEAR CART
        // ==========================================

        cartItemRepository.deleteByUser(user);

        return savedOrder;
    }

    // ==========================================
    // GET USER ORDERS
    // ==========================================

    public List<Order> getOrdersByUser(User user) {

        return orderRepository.findByUserOrderByCreatedAtDesc(user);
    }
}
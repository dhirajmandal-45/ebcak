package ecommerce.back.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import ecommerce.back.entity.CartItem;
import ecommerce.back.entity.User;
import ecommerce.back.repository.UserRepository;
import ecommerce.back.service.CartService;

@RestController
@RequestMapping("/cart")
public class CartController {

    private final CartService cartService;
    private final UserRepository userRepository;

    public CartController(
            CartService cartService,
            UserRepository userRepository) {

        this.cartService = cartService;
        this.userRepository = userRepository;
    }

    // ==========================================
    // ADD PRODUCT TO CART
    // ==========================================

    @PostMapping
    public ResponseEntity<CartItem> addToCart(
            @RequestBody AddCartRequest request,
            Authentication authentication) {

        User user = getLoggedInUser(authentication);

        CartItem cartItem = cartService.addToCart(
                user,
                request.getProductId(),
                request.getQuantity());

        return ResponseEntity.ok(cartItem);
    }

    // ==========================================
    // GET MY CART
    // ==========================================

    @GetMapping
    public ResponseEntity<List<CartItem>> getCart(
            Authentication authentication) {

        User user = getLoggedInUser(authentication);

        return ResponseEntity.ok(
                cartService.getCart(user));
    }

    // ==========================================
    // UPDATE CART ITEM
    // ==========================================

    @PutMapping("/{id}")
    public ResponseEntity<CartItem> updateCartItem(
            @PathVariable Long id,
            @RequestBody UpdateCartRequest request,
            Authentication authentication) {

        User user = getLoggedInUser(authentication);

        CartItem cartItem = cartService.updateCartItem(
                user,
                id,
                request.getQuantity());

        return ResponseEntity.ok(cartItem);
    }

    // ==========================================
    // REMOVE CART ITEM
    // ==========================================

    @DeleteMapping("/{id}")
    public ResponseEntity<String> removeFromCart(
            @PathVariable Long id,
            Authentication authentication) {

        User user = getLoggedInUser(authentication);

        cartService.removeFromCart(user, id);

        return ResponseEntity.ok("Product removed from cart");
    }

    // ==========================================
    // CLEAR CART
    // ==========================================

    @DeleteMapping
    public ResponseEntity<String> clearCart(
            Authentication authentication) {

        User user = getLoggedInUser(authentication);

        cartService.clearCart(user);

        return ResponseEntity.ok("Cart cleared successfully");
    }

    // ==========================================
    // GET LOGGED-IN USER
    // ==========================================

    private User getLoggedInUser(Authentication authentication) {

        String email = authentication.getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    // ==========================================
    // REQUEST CLASSES
    // ==========================================

    public static class AddCartRequest {

        private Integer productId;
        private Integer quantity;

        public AddCartRequest() {
        }

        public Integer getProductId() {
            return productId;
        }

        public void setProductId(Integer productId) {
            this.productId = productId;
        }

        public Integer getQuantity() {
            return quantity;
        }

        public void setQuantity(Integer quantity) {
            this.quantity = quantity;
        }
    }

    public static class UpdateCartRequest {

        private Integer quantity;

        public UpdateCartRequest() {
        }

        public Integer getQuantity() {
            return quantity;
        }

        public void setQuantity(Integer quantity) {
            this.quantity = quantity;
        }
    }
}
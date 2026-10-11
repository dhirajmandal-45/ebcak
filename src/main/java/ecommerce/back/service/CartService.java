package ecommerce.back.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ecommerce.back.entity.CartItem;
import ecommerce.back.entity.Product;
import ecommerce.back.entity.User;
import ecommerce.back.repository.CartItemRepository;
import ecommerce.back.repository.ProductRepository;

@Service
public class CartService {

    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;

    public CartService(
            CartItemRepository cartItemRepository,
            ProductRepository productRepository) {

        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
    }

    // ==========================================
    // ADD PRODUCT TO CART
    // ==========================================

    @Transactional
    public CartItem addToCart(
            User user,
            Integer productId,
            Integer quantity) {

        if (quantity == null || quantity <= 0) {
            throw new RuntimeException("Quantity must be greater than 0");
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found: " + productId));

        Optional<CartItem> existingItem = cartItemRepository.findByUserAndProduct(user, product);

        if (existingItem.isPresent()) {

            CartItem cartItem = existingItem.get();

            int newQuantity = cartItem.getQuantity() + quantity;

            if (product.getStock() < newQuantity) {
                throw new RuntimeException(
                        "Not enough stock for product: " + product.getName());
            }

            cartItem.setQuantity(newQuantity);

            return cartItemRepository.save(cartItem);
        }

        if (product.getStock() < quantity) {
            throw new RuntimeException(
                    "Not enough stock for product: " + product.getName());
        }

        CartItem cartItem = new CartItem();

        cartItem.setUser(user);
        cartItem.setProduct(product);
        cartItem.setQuantity(quantity);

        return cartItemRepository.save(cartItem);
    }

    // ==========================================
    // GET USER CART
    // ==========================================

    public List<CartItem> getCart(User user) {

        return cartItemRepository.findByUser(user);
    }

    // ==========================================
    // UPDATE CART ITEM
    // ==========================================

    @Transactional
    public CartItem updateCartItem(
            User user,
            Long cartItemId,
            Integer quantity) {

        if (quantity == null || quantity <= 0) {
            throw new RuntimeException("Quantity must be greater than 0");
        }

        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("Cart item not found"));

        // Make sure this cart item belongs to the logged-in user
        if (!cartItem.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("You cannot update this cart item");
        }

        Product product = cartItem.getProduct();

        if (product.getStock() < quantity) {
            throw new RuntimeException(
                    "Not enough stock for product: " + product.getName());
        }

        cartItem.setQuantity(quantity);

        return cartItemRepository.save(cartItem);
    }

    // ==========================================
    // REMOVE ITEM FROM CART
    // ==========================================

    @Transactional
    public void removeFromCart(
            User user,
            Long cartItemId) {

        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("Cart item not found"));

        // Make sure this cart item belongs to the logged-in user
        if (!cartItem.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("You cannot remove this cart item");
        }

        cartItemRepository.delete(cartItem);
    }

    // ==========================================
    // CLEAR CART
    // ==========================================

    @Transactional
    public void clearCart(User user) {

        cartItemRepository.deleteByUser(user);
    }
}
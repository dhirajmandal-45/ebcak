package ecommerce.back.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import ecommerce.back.entity.OrderItem;

public interface OrderItemRepository extends JpaRepository<OrderItem, Integer> {
}
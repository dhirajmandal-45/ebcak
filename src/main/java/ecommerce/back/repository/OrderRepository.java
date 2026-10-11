package ecommerce.back.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import ecommerce.back.entity.Order;
import ecommerce.back.entity.User;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByUserOrderByCreatedAtDesc(User user);

    @Query("SELECT DISTINCT o.user FROM Order o")
    List<User> findCustomersWhoPlacedOrders();
}
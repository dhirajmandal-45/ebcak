package ecommerce.back.repository;

import java.util.List;

import ecommerce.back.entity.Product;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Integer> {
List<Product> findByCategoryIgnoreCase(String category);
}
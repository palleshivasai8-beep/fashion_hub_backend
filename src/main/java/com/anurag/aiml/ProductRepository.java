package com.anurag.aiml;

import org.springframework.data.jpa.repository.JpaRepository;
import com.anurag.aiml.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {
}
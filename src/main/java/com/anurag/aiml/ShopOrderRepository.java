package com.anurag.aiml;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.anurag.aiml.ShopOrder;

public interface ShopOrderRepository extends JpaRepository<ShopOrder, Long> {
    List<ShopOrder> findByUserIdOrderByIdDesc(Long userId);
}
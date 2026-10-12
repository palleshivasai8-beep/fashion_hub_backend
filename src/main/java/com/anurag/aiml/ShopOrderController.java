package com.anurag.aiml;

import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.anurag.aiml.ShopOrder;
import com.anurag.aiml.ShopOrderRepository;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "http://localhost:5173")
public class ShopOrderController {
    private final ShopOrderRepository repository;

    public ShopOrderController(ShopOrderRepository repository) {
        this.repository = repository;
    }

    @PostMapping
    public ResponseEntity<?> placeOrder(@RequestBody ShopOrder order) {
        if (order.getUserId() == null
                || order.getCustomerName() == null
                || order.getCustomerName().isBlank()
                || order.getAddress() == null
                || order.getAddress().isBlank()
                || order.getProductNames() == null
                || order.getQuantity() < 1
                || order.getTotalPrice() <= 0) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Enter valid order details"));
        }

        order.setStatus("Order Placed");
        return ResponseEntity.ok(repository.save(order));
    }

    @GetMapping
    public List<ShopOrder> getOrders(@RequestParam Long userId) {
        return repository.findByUserIdOrderByIdDesc(userId);
    }

    @PutMapping("/{id}/deliver")
    public ResponseEntity<?> deliverOrder(@PathVariable Long id) {
        return repository.findById(id)
                .<ResponseEntity<?>>map(order -> {
                    order.setStatus("Delivered");
                    return ResponseEntity.ok(repository.save(order));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
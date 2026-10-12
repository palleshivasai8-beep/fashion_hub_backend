package com.anurag.aiml;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.anurag.aiml.Product;
import com.anurag.aiml.ProductRepository;

@Configuration
public class ShopDataLoader {

    @Bean
    CommandLineRunner loadProducts(ProductRepository repository) {
        return args -> {
            if (repository.count() > 0) {
                return;
            }

            String[][] dresses = {
                {"Floral Dress", "Women", "799"},
                {"Party Wear Gown", "Women", "1499"},
                {"Traditional Kurti", "Women", "699"},
                {"Cotton Saree", "Women", "1299"},
                {"Summer Dress", "Women", "599"},
                {"Long Frock", "Women", "999"},
                {"Casual T-Shirt", "Men", "499"},
                {"Formal Shirt", "Men", "799"},
                {"Denim Jeans", "Men", "999"},
                {"Hoodie", "Men", "899"},
                {"Men's Jacket", "Men", "1499"},
                {"Kids Frock", "Kids", "499"},
                {"Kids Party Wear", "Kids", "799"},
                {"Kids T-Shirt", "Kids", "299"},
                {"Leggings", "Women", "399"},
                {"Palazzo Pants", "Women", "599"},
                {"Anarkali Dress", "Women", "1599"},
                {"Lehenga", "Women", "1999"},
                {"Kurta Set", "Men", "1199"},
                {"Sports Wear", "Men", "699"}
            };

            for (String[] d : dresses) {
                Product p = new Product();
                p.setName(d[0]);
                p.setCategory(d[1]);
                p.setPrice(Double.parseDouble(d[2]));
                p.setImage("https://placehold.co/300x350?text=Fashion");
                repository.save(p);
            }

            System.out.println("Fashion Hub dresses added successfully!");
        };
    }
}
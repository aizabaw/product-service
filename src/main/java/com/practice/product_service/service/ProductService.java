package com.practice.product_service.service;

import com.practice.product_service.data.entity.ProductEntity;
import com.practice.product_service.data.repository.ProductRepository;
import com.practice.product_service.model.Product;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductService {

//    private final ConcurrentHashMap<Long, Product> productMap =
//            new ConcurrentHashMap<>();
//
//    private final AtomicLong idCounter = new AtomicLong(0);

    private final ProductRepository productRepository;

    @PostConstruct
    public void init() {
        createProductAndSave("mug", new BigDecimal(500));
        createProductAndSave("shirt", new BigDecimal(1200));
        createProductAndSave("cap", new BigDecimal(650));
    }

    public List<Product> getProductList() {
        return productRepository.findAll()
                .stream().map(this::toProduct)
                .toList();

//        return productMap.values().stream().toList();
    }

    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id).map(this::toProduct);

//        return Optional.ofNullable(productMap.get(id));
    }

    public Product createProductAndSave(String name, BigDecimal price) {

        ProductEntity product = ProductEntity.builder().name(name).price(price).build();
        ProductEntity newProduct = productRepository.save(product);
        return toProduct(newProduct);

//        Product newProduct = new Product(idCounter.incrementAndGet(), name, price);
//        productMap.put(newProduct.id(), newProduct);
    }

    private Product toProduct(ProductEntity entity) {
        return new Product(entity.getId(), entity.getName(), entity.getPrice());
    }

}

package com.practice.product_service.service;

import com.practice.product_service.data.entity.ProductEntity;
import com.practice.product_service.data.repository.ProductRepository;
import com.practice.product_service.exception.ProductNotFoundException;
import com.practice.product_service.model.Product;
import com.practice.product_service.web.dto.PriceUpdateRequest;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductService {

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
    }

    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id).map(this::toProduct);
    }

    public Product createProductAndSave(String name, BigDecimal price) {

        ProductEntity product = ProductEntity.builder().name(name).price(price).build();
        ProductEntity newProduct = productRepository.save(product);
        return toProduct(newProduct);
    }

    @Transactional
    public void updatePrices(List<PriceUpdateRequest> requestList) {
        for (PriceUpdateRequest update : requestList) {
            ProductEntity entity = productRepository.findById(update.id())
                    .orElseThrow(() -> new ProductNotFoundException(update.id()));
            entity.setPrice(update.newPrice());
//            productRepository.save(entity);
        }
    }

    public void updatePricesFromController(List<PriceUpdateRequest> updates) {
        updatePrices(updates);   // calling our OWN method
    }

    private Product toProduct(ProductEntity entity) {
        return new Product(entity.getId(), entity.getName(), entity.getPrice());
    }

    private ProductEntity toProductEntity(Product product) {
        return ProductEntity.builder().id(product.id()).price(product.price()).build();
    }

}

package com.practice.product_service.web;

import com.practice.product_service.exception.ProductNotFoundException;
import com.practice.product_service.model.Product;
import com.practice.product_service.service.ProductService;
import com.practice.product_service.web.dto.CreateProductRequest;
import com.practice.product_service.web.dto.PriceUpdateRequest;
import com.practice.product_service.exception.PriceUpdateException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductCatalogController {

    private final ProductService productService;

    @GetMapping("/{id}")
    public Product getProductById(@PathVariable Long id) {
        return productService
                .getProductById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
    }

    @GetMapping
    public List<Product> getAllProducts() {
        return productService.getProductList();
    }

    @PostMapping
    public ResponseEntity<Product> createProduct(@Valid @RequestBody CreateProductRequest request) {
        Product newProduct = productService.createProductAndSave(request.name(), request.price());
        return ResponseEntity.created(URI.create("/products/%d".formatted(newProduct.id()))).body(newProduct);
    }

    @PutMapping("/prices")
    public ResponseEntity<Void> updatePrice(@Valid @RequestBody List<PriceUpdateRequest> request) throws PriceUpdateException {
        productService.updatePrices(request);
        return ResponseEntity.noContent().build();
    }

}

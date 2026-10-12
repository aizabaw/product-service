package com.practice.product_service.data.repository;

import com.practice.product_service.data.entity.ProductEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
class ProductRepositoryTest {

    @Autowired
    TestEntityManager entityManager;

    @Autowired
    ProductRepository productRepository;

    @Test
    void save_assignsId() {
        ProductEntity saved = productRepository.save(ProductEntity.builder()
                .name("spoon")
                .price(new BigDecimal("20"))
                .build());

        assertNotNull(saved.getId());
    }

    @Test
    void findById_returnsSavedProduct() {
        ProductEntity saved = productRepository.save(ProductEntity.builder()
                .name("spoon")
                .price(new BigDecimal("20"))
                .build());

        entityManager.flush();
        entityManager.clear();

        ProductEntity found = productRepository.findById(saved.getId()).orElseThrow();

        assertEquals("spoon", found.getName());
        assertEquals(0, new BigDecimal("20").compareTo(found.getPrice()));
    }

    @Test
    void findById_returnsEmptyWhenMissing() {
        Optional<ProductEntity> found = productRepository.findById(999L);
        assertTrue(found.isEmpty());
    }

    @Test
    void price_isStoredWithTwoDecimals() {
        ProductEntity saved = productRepository.save(ProductEntity.builder()
                .name("spoon")
                .price(new BigDecimal("20"))
                .build());

        entityManager.flush();
        entityManager.clear();

        ProductEntity found = productRepository.findById(saved.getId()).orElseThrow();

        assertEquals(0, found.getPrice().compareTo(new BigDecimal(20)));
        assertEquals(2, found.getPrice().scale());
    }

}


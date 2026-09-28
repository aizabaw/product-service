package com.practice.product_service.model;

import java.math.BigDecimal;

public record Product(Long id, String name, BigDecimal price) {};

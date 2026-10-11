package com.practice.product_service.web.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record PriceUpdateRequest (Long id,@NotNull @Positive BigDecimal price) {}

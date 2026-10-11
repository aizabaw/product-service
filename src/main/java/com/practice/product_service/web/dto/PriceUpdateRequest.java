package com.practice.product_service.web.dto;

import java.math.BigDecimal;

public record PriceUpdateRequest (Long id, BigDecimal newPrice) {}

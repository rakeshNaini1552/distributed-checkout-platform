package com.example.order.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderRequest {
    @NotBlank
    private String item;

    @NotNull
    @DecimalMin("0.01")
    private BigDecimal price;
}


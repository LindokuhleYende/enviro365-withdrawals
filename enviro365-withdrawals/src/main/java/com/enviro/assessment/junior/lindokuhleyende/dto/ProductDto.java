package com.enviro.assessment.junior.lindokuhleyende.dto;

import com.enviro.assessment.junior.lindokuhleyende.entity.enums.ProductType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDto {
    private Long id;
    private String productName;
    private ProductType type;
    private BigDecimal balance;
}

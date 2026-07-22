package com.enviro.assessment.junior.lindokuhleyende.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PortfolioResponseDto {
    private Long investorId;
    private String fullName;
    private String email;
    private int age;
    private BigDecimal totalBalance;
    private List<ProductDto> products;
}

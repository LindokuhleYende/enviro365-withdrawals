package com.enviro.assessment.junior.lindokuhleyende.dto;

import com.enviro.assessment.junior.lindokuhleyende.entity.enums.WithdrawalStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WithdrawalResponseDto {
    private Long id;
    private Long investorId;
    private String investorName;
    private Long productId;
    private String productName;
    private BigDecimal amountRequested;
    private BigDecimal balanceBeforeWithdrawal;
    private BigDecimal balanceAfterWithdrawal;
    private WithdrawalStatus status;
    private LocalDateTime requestDate;
    private String notes;
}

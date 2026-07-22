package com.enviro.assessment.junior.lindokuhleyende.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Payload submitted by an investor (or the UI on their behalf) to request a withdrawal.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WithdrawalRequestDto {

    @NotNull(message = "investorId is required")
    @Schema(example = "1")
    private Long investorId;

    @NotNull(message = "productId is required")
    @Schema(example = "1")
    private Long productId;

    @NotNull(message = "amount is required")
    @DecimalMin(value = "0.01", message = "amount must be greater than 0")
    @Schema(example = "1000.00")
    private BigDecimal amount;

    @Schema(example = "Partial withdrawal for medical expenses")
    private String notes;
}

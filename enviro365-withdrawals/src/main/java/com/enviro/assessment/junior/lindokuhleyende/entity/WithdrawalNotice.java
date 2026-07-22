package com.enviro.assessment.junior.lindokuhleyende.entity;

import com.enviro.assessment.junior.lindokuhleyende.entity.enums.WithdrawalStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "withdrawal_notices")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WithdrawalNotice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "investor_id", nullable = false)
    private Investor investor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amountRequested;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal balanceBeforeWithdrawal;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal balanceAfterWithdrawal;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private WithdrawalStatus status;

    @Column(nullable = false)
    private LocalDateTime requestDate;

    @Column(length = 500)
    private String notes;
}

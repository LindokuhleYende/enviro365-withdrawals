package com.enviro.assessment.junior.lindokuhleyende.service;

import com.enviro.assessment.junior.lindokuhleyende.dto.WithdrawalRequestDto;
import com.enviro.assessment.junior.lindokuhleyende.dto.WithdrawalResponseDto;
import com.enviro.assessment.junior.lindokuhleyende.entity.Investor;
import com.enviro.assessment.junior.lindokuhleyende.entity.Product;
import com.enviro.assessment.junior.lindokuhleyende.entity.WithdrawalNotice;
import com.enviro.assessment.junior.lindokuhleyende.entity.enums.ProductType;
import com.enviro.assessment.junior.lindokuhleyende.exception.InvalidWithdrawalException;
import com.enviro.assessment.junior.lindokuhleyende.exception.ResourceNotFoundException;
import com.enviro.assessment.junior.lindokuhleyende.repository.InvestorRepository;
import com.enviro.assessment.junior.lindokuhleyende.repository.ProductRepository;
import com.enviro.assessment.junior.lindokuhleyende.repository.WithdrawalNoticeRepository;
import com.enviro.assessment.junior.lindokuhleyende.service.impl.WithdrawalServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for WithdrawalServiceImpl focusing on the mandated business rules:
 *  1. Retirement withdrawals only allowed if age > 65
 *  2. Withdrawal must not exceed balance
 *  3. Withdrawal must not exceed 90% of balance
 */
@ExtendWith(MockitoExtension.class)
class WithdrawalServiceImplTest {

    @Mock
    private InvestorRepository investorRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private WithdrawalNoticeRepository withdrawalNoticeRepository;

    @InjectMocks
    private WithdrawalServiceImpl withdrawalService;

    private Investor youngInvestor;
    private Investor retiredInvestor;
    private Product retirementProduct;
    private Product discretionaryProduct;

    @BeforeEach
    void setUp() {
        youngInvestor = Investor.builder()
                .id(1L)
                .fullName("John Smith")
                .email("john.smith@example.com")
                .dateOfBirth(LocalDate.now().minusYears(40))
                .build();

        retiredInvestor = Investor.builder()
                .id(2L)
                .fullName("Mary Johnson")
                .email("mary.johnson@example.com")
                .dateOfBirth(LocalDate.now().minusYears(70))
                .build();

        retirementProduct = Product.builder()
                .id(10L)
                .productName("Retirement Annuity Fund")
                .type(ProductType.RETIREMENT)
                .balance(new BigDecimal("100000.00"))
                .investor(retiredInvestor)
                .build();

        discretionaryProduct = Product.builder()
                .id(20L)
                .productName("Discretionary Investment Plan")
                .type(ProductType.DISCRETIONARY)
                .balance(new BigDecimal("100000.00"))
                .investor(youngInvestor)
                .build();
    }

    @Nested
    @DisplayName("Retirement age rule")
    class RetirementAgeRule {

        @Test
        @DisplayName("rejects retirement withdrawal when investor age <= 65")
        void rejectsWhenNotOverRetirementAge() {
            Product youngRetirementProduct = Product.builder()
                    .id(11L)
                    .productName("Retirement Annuity Fund")
                    .type(ProductType.RETIREMENT)
                    .balance(new BigDecimal("100000.00"))
                    .investor(youngInvestor)
                    .build();

            when(investorRepository.findById(1L)).thenReturn(Optional.of(youngInvestor));
            when(productRepository.findById(11L)).thenReturn(Optional.of(youngRetirementProduct));

            WithdrawalRequestDto request = new WithdrawalRequestDto(1L, 11L, new BigDecimal("1000.00"), null);

            assertThatThrownBy(() -> withdrawalService.createWithdrawal(request))
                    .isInstanceOf(InvalidWithdrawalException.class)
                    .hasMessageContaining("Retirement withdrawals are only permitted");

            verify(withdrawalNoticeRepository, never()).save(any());
        }

        @Test
        @DisplayName("allows retirement withdrawal when investor age > 65 and within limits")
        void allowsWhenOverRetirementAge() {
            when(investorRepository.findById(2L)).thenReturn(Optional.of(retiredInvestor));
            when(productRepository.findById(10L)).thenReturn(Optional.of(retirementProduct));
            when(withdrawalNoticeRepository.save(any(WithdrawalNotice.class)))
                    .thenAnswer(inv -> {
                        WithdrawalNotice n = inv.getArgument(0);
                        n.setId(100L);
                        return n;
                    });

            WithdrawalRequestDto request = new WithdrawalRequestDto(2L, 10L, new BigDecimal("5000.00"), "Retirement drawdown");

            WithdrawalResponseDto response = withdrawalService.createWithdrawal(request);

            assertThat(response.getId()).isEqualTo(100L);
            assertThat(response.getBalanceAfterWithdrawal()).isEqualByComparingTo("95000.00");
            verify(withdrawalNoticeRepository).save(any(WithdrawalNotice.class));
        }

        @Test
        @DisplayName("non-retirement products are unaffected by the age rule")
        void nonRetirementProductsBypassAgeRule() {
            when(investorRepository.findById(1L)).thenReturn(Optional.of(youngInvestor));
            when(productRepository.findById(20L)).thenReturn(Optional.of(discretionaryProduct));
            when(withdrawalNoticeRepository.save(any(WithdrawalNotice.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            WithdrawalRequestDto request = new WithdrawalRequestDto(1L, 20L, new BigDecimal("1000.00"), null);

            assertThat(withdrawalService.createWithdrawal(request)).isNotNull();
        }
    }

    @Nested
    @DisplayName("Balance rules")
    class BalanceRules {

        @Test
        @DisplayName("rejects withdrawal that exceeds the available balance")
        void rejectsWhenExceedsBalance() {
            when(investorRepository.findById(1L)).thenReturn(Optional.of(youngInvestor));
            when(productRepository.findById(20L)).thenReturn(Optional.of(discretionaryProduct));

            WithdrawalRequestDto request = new WithdrawalRequestDto(1L, 20L, new BigDecimal("150000.00"), null);

            assertThatThrownBy(() -> withdrawalService.createWithdrawal(request))
                    .isInstanceOf(InvalidWithdrawalException.class)
                    .hasMessageContaining("exceeds the available balance");

            verify(withdrawalNoticeRepository, never()).save(any());
        }

        @Test
        @DisplayName("rejects withdrawal that exceeds 90% of the balance")
        void rejectsWhenExceedsNinetyPercentCap() {
            when(investorRepository.findById(1L)).thenReturn(Optional.of(youngInvestor));
            when(productRepository.findById(20L)).thenReturn(Optional.of(discretionaryProduct));

            // Balance is 100,000.00 -> 90% cap is 90,000.00
            WithdrawalRequestDto request = new WithdrawalRequestDto(1L, 20L, new BigDecimal("95000.00"), null);

            assertThatThrownBy(() -> withdrawalService.createWithdrawal(request))
                    .isInstanceOf(InvalidWithdrawalException.class)
                    .hasMessageContaining("90%");

            verify(withdrawalNoticeRepository, never()).save(any());
        }

        @Test
        @DisplayName("allows a withdrawal exactly at the 90% cap")
        void allowsWithdrawalAtExactlyNinetyPercent() {
            when(investorRepository.findById(1L)).thenReturn(Optional.of(youngInvestor));
            when(productRepository.findById(20L)).thenReturn(Optional.of(discretionaryProduct));
            when(withdrawalNoticeRepository.save(any(WithdrawalNotice.class)))
                    .thenAnswer(inv -> {
                        WithdrawalNotice n = inv.getArgument(0);
                        n.setId(101L);
                        return n;
                    });

            // Balance is 100,000.00 -> 90% cap is exactly 90,000.00
            WithdrawalRequestDto request = new WithdrawalRequestDto(1L, 20L, new BigDecimal("90000.00"), null);

            WithdrawalResponseDto response = withdrawalService.createWithdrawal(request);

            assertThat(response.getBalanceAfterWithdrawal()).isEqualByComparingTo("10000.00");
        }
    }

    @Nested
    @DisplayName("Not found and mismatch handling")
    class NotFoundHandling {

        @Test
        @DisplayName("throws ResourceNotFoundException when investor does not exist")
        void throwsWhenInvestorMissing() {
            when(investorRepository.findById(99L)).thenReturn(Optional.empty());

            WithdrawalRequestDto request = new WithdrawalRequestDto(99L, 20L, new BigDecimal("100.00"), null);

            assertThatThrownBy(() -> withdrawalService.createWithdrawal(request))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Investor not found");
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when product does not exist")
        void throwsWhenProductMissing() {
            when(investorRepository.findById(1L)).thenReturn(Optional.of(youngInvestor));
            when(productRepository.findById(999L)).thenReturn(Optional.empty());

            WithdrawalRequestDto request = new WithdrawalRequestDto(1L, 999L, new BigDecimal("100.00"), null);

            assertThatThrownBy(() -> withdrawalService.createWithdrawal(request))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Product not found");
        }

        @Test
        @DisplayName("throws InvalidWithdrawalException when product does not belong to investor")
        void throwsWhenProductBelongsToDifferentInvestor() {
            // discretionaryProduct belongs to youngInvestor (id 1), but we request as retiredInvestor (id 2)
            when(investorRepository.findById(2L)).thenReturn(Optional.of(retiredInvestor));
            when(productRepository.findById(20L)).thenReturn(Optional.of(discretionaryProduct));

            WithdrawalRequestDto request = new WithdrawalRequestDto(2L, 20L, new BigDecimal("100.00"), null);

            assertThatThrownBy(() -> withdrawalService.createWithdrawal(request))
                    .isInstanceOf(InvalidWithdrawalException.class)
                    .hasMessageContaining("does not belong to investor");
        }
    }
}

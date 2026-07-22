package com.enviro.assessment.junior.lindokuhleyende.service.impl;

import com.enviro.assessment.junior.lindokuhleyende.dto.WithdrawalRequestDto;
import com.enviro.assessment.junior.lindokuhleyende.dto.WithdrawalResponseDto;
import com.enviro.assessment.junior.lindokuhleyende.entity.Investor;
import com.enviro.assessment.junior.lindokuhleyende.entity.Product;
import com.enviro.assessment.junior.lindokuhleyende.entity.WithdrawalNotice;
import com.enviro.assessment.junior.lindokuhleyende.entity.enums.ProductType;
import com.enviro.assessment.junior.lindokuhleyende.entity.enums.WithdrawalStatus;
import com.enviro.assessment.junior.lindokuhleyende.exception.InvalidWithdrawalException;
import com.enviro.assessment.junior.lindokuhleyende.exception.ResourceNotFoundException;
import com.enviro.assessment.junior.lindokuhleyende.repository.InvestorRepository;
import com.enviro.assessment.junior.lindokuhleyende.repository.ProductRepository;
import com.enviro.assessment.junior.lindokuhleyende.repository.WithdrawalNoticeRepository;
import com.enviro.assessment.junior.lindokuhleyende.repository.WithdrawalNoticeSpecifications;
import com.enviro.assessment.junior.lindokuhleyende.service.WithdrawalService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WithdrawalServiceImpl implements WithdrawalService {

    /**
     * The maximum proportion of a product's balance that may be withdrawn in a single notice.
     */
    static final BigDecimal MAX_WITHDRAWAL_RATIO = new BigDecimal("0.90");

    /**
     * Minimum age (exclusive lower bound - must be strictly greater than this) for retirement withdrawals.
     */
    static final int MINIMUM_RETIREMENT_AGE = 65;

    private final InvestorRepository investorRepository;
    private final ProductRepository productRepository;
    private final WithdrawalNoticeRepository withdrawalNoticeRepository;

    @Override
    @Transactional
    public WithdrawalResponseDto createWithdrawal(WithdrawalRequestDto request) {
        Investor investor = investorRepository.findById(request.getInvestorId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Investor not found with id: " + request.getInvestorId()));

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product not found with id: " + request.getProductId()));

        if (!product.getInvestor().getId().equals(investor.getId())) {
            throw new InvalidWithdrawalException(
                    "Product " + product.getId() + " does not belong to investor " + investor.getId());
        }

        BigDecimal amount = request.getAmount();
        BigDecimal balance = product.getBalance();

        validateBusinessRules(investor, product, amount, balance);

        BigDecimal balanceAfter = balance.subtract(amount);

        WithdrawalNotice notice = WithdrawalNotice.builder()
                .investor(investor)
                .product(product)
                .amountRequested(amount)
                .balanceBeforeWithdrawal(balance)
                .balanceAfterWithdrawal(balanceAfter)
                .status(WithdrawalStatus.PENDING)
                .requestDate(LocalDateTime.now())
                .notes(request.getNotes())
                .build();

        WithdrawalNotice saved = withdrawalNoticeRepository.save(notice);

        return toResponseDto(saved);
    }

    /**
     * Applies the mandated business rules, in order, throwing InvalidWithdrawalException
     * with a specific message on the first rule violated:
     *  1. Retirement product withdrawals are only allowed if the investor's age > 65.
     *  2. The withdrawal amount must not exceed the product's current balance.
     *  3. The withdrawal amount must not exceed 90% of the product's current balance.
     */
    private void validateBusinessRules(Investor investor, Product product, BigDecimal amount, BigDecimal balance) {
        if (product.getType() == ProductType.RETIREMENT && investor.getAge() <= MINIMUM_RETIREMENT_AGE) {
            throw new InvalidWithdrawalException(
                    "Retirement withdrawals are only permitted for investors over the age of "
                            + MINIMUM_RETIREMENT_AGE + ". Investor is currently " + investor.getAge() + ".");
        }

        if (amount.compareTo(balance) > 0) {
            throw new InvalidWithdrawalException(
                    "Withdrawal amount (" + amount + ") exceeds the available balance (" + balance + ").");
        }

        BigDecimal maxAllowed = balance.multiply(MAX_WITHDRAWAL_RATIO).setScale(2, RoundingMode.HALF_UP);
        if (amount.compareTo(maxAllowed) > 0) {
            throw new InvalidWithdrawalException(
                    "Withdrawal amount (" + amount + ") exceeds the maximum allowed withdrawal of 90% of balance ("
                            + maxAllowed + ").");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<WithdrawalResponseDto> getHistory(Long investorId, Long productId, WithdrawalStatus status,
                                                    LocalDateTime from, LocalDateTime to) {
        var spec = WithdrawalNoticeSpecifications.withFilters(investorId, productId, status, from, to);
        return withdrawalNoticeRepository.findAll(spec).stream()
                .sorted((a, b) -> b.getRequestDate().compareTo(a.getRequestDate()))
                .map(this::toResponseDto)
                .toList();
    }

    private WithdrawalResponseDto toResponseDto(WithdrawalNotice notice) {
        return WithdrawalResponseDto.builder()
                .id(notice.getId())
                .investorId(notice.getInvestor().getId())
                .investorName(notice.getInvestor().getFullName())
                .productId(notice.getProduct().getId())
                .productName(notice.getProduct().getProductName())
                .amountRequested(notice.getAmountRequested())
                .balanceBeforeWithdrawal(notice.getBalanceBeforeWithdrawal())
                .balanceAfterWithdrawal(notice.getBalanceAfterWithdrawal())
                .status(notice.getStatus())
                .requestDate(notice.getRequestDate())
                .notes(notice.getNotes())
                .build();
    }
}

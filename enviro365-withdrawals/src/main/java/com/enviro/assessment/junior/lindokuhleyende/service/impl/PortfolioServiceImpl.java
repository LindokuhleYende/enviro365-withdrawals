package com.enviro.assessment.junior.lindokuhleyende.service.impl;

import com.enviro.assessment.junior.lindokuhleyende.dto.InvestorSummaryDto;
import com.enviro.assessment.junior.lindokuhleyende.dto.PortfolioResponseDto;
import com.enviro.assessment.junior.lindokuhleyende.dto.ProductDto;
import com.enviro.assessment.junior.lindokuhleyende.entity.Investor;
import com.enviro.assessment.junior.lindokuhleyende.entity.Product;
import com.enviro.assessment.junior.lindokuhleyende.exception.ResourceNotFoundException;
import com.enviro.assessment.junior.lindokuhleyende.repository.InvestorRepository;
import com.enviro.assessment.junior.lindokuhleyende.repository.ProductRepository;
import com.enviro.assessment.junior.lindokuhleyende.service.PortfolioService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PortfolioServiceImpl implements PortfolioService {

    private final InvestorRepository investorRepository;
    private final ProductRepository productRepository;

    @Override
    public List<InvestorSummaryDto> getAllInvestors() {
        return investorRepository.findAll().stream()
                .map(investor -> InvestorSummaryDto.builder()
                        .id(investor.getId())
                        .fullName(investor.getFullName())
                        .email(investor.getEmail())
                        .age(investor.getAge())
                        .build())
                .toList();
    }

    @Override
    public PortfolioResponseDto getPortfolio(Long investorId) {
        Investor investor = investorRepository.findById(investorId)
                .orElseThrow(() -> new ResourceNotFoundException("Investor not found with id: " + investorId));

        List<Product> products = productRepository.findByInvestorId(investorId);

        List<ProductDto> productDtos = products.stream()
                .map(p -> ProductDto.builder()
                        .id(p.getId())
                        .productName(p.getProductName())
                        .type(p.getType())
                        .balance(p.getBalance())
                        .build())
                .toList();

        BigDecimal totalBalance = products.stream()
                .map(Product::getBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return PortfolioResponseDto.builder()
                .investorId(investor.getId())
                .fullName(investor.getFullName())
                .email(investor.getEmail())
                .age(investor.getAge())
                .totalBalance(totalBalance)
                .products(productDtos)
                .build();
    }
}

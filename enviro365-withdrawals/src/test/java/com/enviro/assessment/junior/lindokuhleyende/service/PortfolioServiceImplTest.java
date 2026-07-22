package com.enviro.assessment.junior.lindokuhleyende.service;

import com.enviro.assessment.junior.lindokuhleyende.dto.PortfolioResponseDto;
import com.enviro.assessment.junior.lindokuhleyende.entity.Investor;
import com.enviro.assessment.junior.lindokuhleyende.entity.Product;
import com.enviro.assessment.junior.lindokuhleyende.entity.enums.ProductType;
import com.enviro.assessment.junior.lindokuhleyende.exception.ResourceNotFoundException;
import com.enviro.assessment.junior.lindokuhleyende.repository.InvestorRepository;
import com.enviro.assessment.junior.lindokuhleyende.repository.ProductRepository;
import com.enviro.assessment.junior.lindokuhleyende.service.impl.PortfolioServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PortfolioServiceImplTest {

    @Mock
    private InvestorRepository investorRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private PortfolioServiceImpl portfolioService;

    private Investor investor;

    @BeforeEach
    void setUp() {
        investor = Investor.builder()
                .id(1L)
                .fullName("Peter Ndlovu")
                .email("peter.ndlovu@example.com")
                .dateOfBirth(LocalDate.now().minusYears(36))
                .build();
    }

    @Test
    void getPortfolio_returnsCorrectTotalBalanceAndProducts() {
        Product p1 = Product.builder().id(1L).productName("Unit Trust Growth Fund")
                .type(ProductType.UNIT_TRUST).balance(new BigDecimal("90000.00")).investor(investor).build();
        Product p2 = Product.builder().id(2L).productName("Discretionary Investment Plan")
                .type(ProductType.DISCRETIONARY).balance(new BigDecimal("45000.00")).investor(investor).build();

        when(investorRepository.findById(1L)).thenReturn(Optional.of(investor));
        when(productRepository.findByInvestorId(1L)).thenReturn(List.of(p1, p2));

        PortfolioResponseDto result = portfolioService.getPortfolio(1L);

        assertThat(result.getInvestorId()).isEqualTo(1L);
        assertThat(result.getFullName()).isEqualTo("Peter Ndlovu");
        assertThat(result.getProducts()).hasSize(2);
        assertThat(result.getTotalBalance()).isEqualByComparingTo("135000.00");
    }

    @Test
    void getPortfolio_throwsWhenInvestorNotFound() {
        when(investorRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> portfolioService.getPortfolio(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Investor not found");
    }
}

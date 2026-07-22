package com.enviro.assessment.junior.lindokuhleyende.service;

import com.enviro.assessment.junior.lindokuhleyende.dto.InvestorSummaryDto;
import com.enviro.assessment.junior.lindokuhleyende.dto.PortfolioResponseDto;

import java.util.List;

public interface PortfolioService {

    List<InvestorSummaryDto> getAllInvestors();

    PortfolioResponseDto getPortfolio(Long investorId);
}

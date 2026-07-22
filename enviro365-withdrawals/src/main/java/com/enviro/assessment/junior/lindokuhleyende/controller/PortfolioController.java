package com.enviro.assessment.junior.lindokuhleyende.controller;

import com.enviro.assessment.junior.lindokuhleyende.dto.InvestorSummaryDto;
import com.enviro.assessment.junior.lindokuhleyende.dto.PortfolioResponseDto;
import com.enviro.assessment.junior.lindokuhleyende.service.PortfolioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/investors")
@RequiredArgsConstructor
@Tag(name = "Portfolio", description = "Endpoints for retrieving investor and portfolio information")
public class PortfolioController {

    private final PortfolioService portfolioService;

    @GetMapping
    @Operation(summary = "List all investors", description = "Returns a summary of every investor, used to populate UI selectors.")
    public ResponseEntity<List<InvestorSummaryDto>> getAllInvestors() {
        return ResponseEntity.ok(portfolioService.getAllInvestors());
    }

    @GetMapping("/{investorId}/portfolio")
    @Operation(summary = "Get investor portfolio", description = "Returns investor details plus all held products and total balance.")
    public ResponseEntity<PortfolioResponseDto> getPortfolio(@PathVariable Long investorId) {
        return ResponseEntity.ok(portfolioService.getPortfolio(investorId));
    }
}

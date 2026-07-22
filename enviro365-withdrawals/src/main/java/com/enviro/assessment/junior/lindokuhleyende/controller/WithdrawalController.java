package com.enviro.assessment.junior.lindokuhleyende.controller;

import com.enviro.assessment.junior.lindokuhleyende.dto.WithdrawalRequestDto;
import com.enviro.assessment.junior.lindokuhleyende.dto.WithdrawalResponseDto;
import com.enviro.assessment.junior.lindokuhleyende.entity.enums.WithdrawalStatus;
import com.enviro.assessment.junior.lindokuhleyende.service.WithdrawalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/withdrawals")
@RequiredArgsConstructor
@Tag(name = "Withdrawals", description = "Endpoints for creating and viewing withdrawal notices")
public class WithdrawalController {

    private final WithdrawalService withdrawalService;

    @PostMapping
    @Operation(summary = "Submit a withdrawal notice",
            description = "Validates the request against business rules (age, balance, 90% cap) before creating the notice.")
    public ResponseEntity<WithdrawalResponseDto> createWithdrawal(@Valid @RequestBody WithdrawalRequestDto request) {
        WithdrawalResponseDto response = withdrawalService.createWithdrawal(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Get withdrawal history",
            description = "Returns withdrawal notices, optionally filtered by investor, product, status and date range.")
    public ResponseEntity<List<WithdrawalResponseDto>> getHistory(
            @RequestParam(required = false) Long investorId,
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false) WithdrawalStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        return ResponseEntity.ok(withdrawalService.getHistory(investorId, productId, status, from, to));
    }
}

package com.enviro.assessment.junior.lindokuhleyende.service;

import com.enviro.assessment.junior.lindokuhleyende.dto.WithdrawalRequestDto;
import com.enviro.assessment.junior.lindokuhleyende.dto.WithdrawalResponseDto;
import com.enviro.assessment.junior.lindokuhleyende.entity.enums.WithdrawalStatus;

import java.time.LocalDateTime;
import java.util.List;

public interface WithdrawalService {

    WithdrawalResponseDto createWithdrawal(WithdrawalRequestDto request);

    List<WithdrawalResponseDto> getHistory(Long investorId, Long productId, WithdrawalStatus status,
                                            LocalDateTime from, LocalDateTime to);
}

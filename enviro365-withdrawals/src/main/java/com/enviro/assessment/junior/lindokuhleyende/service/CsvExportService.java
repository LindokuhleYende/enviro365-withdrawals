package com.enviro.assessment.junior.lindokuhleyende.service;

import com.enviro.assessment.junior.lindokuhleyende.entity.enums.WithdrawalStatus;

import java.time.LocalDateTime;

public interface CsvExportService {

    String exportWithdrawalsCsv(Long investorId, Long productId, WithdrawalStatus status,
                                 LocalDateTime from, LocalDateTime to);
}

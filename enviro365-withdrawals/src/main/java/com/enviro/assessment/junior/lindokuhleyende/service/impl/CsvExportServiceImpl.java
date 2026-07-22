package com.enviro.assessment.junior.lindokuhleyende.service.impl;

import com.enviro.assessment.junior.lindokuhleyende.entity.WithdrawalNotice;
import com.enviro.assessment.junior.lindokuhleyende.entity.enums.WithdrawalStatus;
import com.enviro.assessment.junior.lindokuhleyende.repository.WithdrawalNoticeRepository;
import com.enviro.assessment.junior.lindokuhleyende.repository.WithdrawalNoticeSpecifications;
import com.enviro.assessment.junior.lindokuhleyende.service.CsvExportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CsvExportServiceImpl implements CsvExportService {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final String[] HEADERS = {
            "Notice ID", "Investor Name", "Investor Email", "Product Name", "Product Type",
            "Amount Requested", "Balance Before", "Balance After", "Status", "Request Date", "Notes"
    };

    private final WithdrawalNoticeRepository withdrawalNoticeRepository;

    @Override
    public String exportWithdrawalsCsv(Long investorId, Long productId, WithdrawalStatus status,
                                        LocalDateTime from, LocalDateTime to) {
        var spec = WithdrawalNoticeSpecifications.withFilters(investorId, productId, status, from, to);
        List<WithdrawalNotice> notices = withdrawalNoticeRepository.findAll(spec);

        StringBuilder sb = new StringBuilder();
        sb.append(String.join(",", HEADERS)).append("\n");

        for (WithdrawalNotice n : notices) {
            sb.append(escape(n.getId()))
              .append(",").append(escape(n.getInvestor().getFullName()))
              .append(",").append(escape(n.getInvestor().getEmail()))
              .append(",").append(escape(n.getProduct().getProductName()))
              .append(",").append(escape(n.getProduct().getType()))
              .append(",").append(escape(n.getAmountRequested()))
              .append(",").append(escape(n.getBalanceBeforeWithdrawal()))
              .append(",").append(escape(n.getBalanceAfterWithdrawal()))
              .append(",").append(escape(n.getStatus()))
              .append(",").append(escape(n.getRequestDate().format(DATE_FORMAT)))
              .append(",").append(escape(n.getNotes()))
              .append("\n");
        }

        return sb.toString();
    }

    /**
     * Escapes a CSV field: wraps in quotes and doubles any internal quotes if the
     * value contains a comma, quote, or newline. Handles nulls safely.
     */
    private String escape(Object value) {
        if (value == null) {
            return "";
        }
        String str = value.toString();
        if (str.contains(",") || str.contains("\"") || str.contains("\n")) {
            return "\"" + str.replace("\"", "\"\"") + "\"";
        }
        return str;
    }
}

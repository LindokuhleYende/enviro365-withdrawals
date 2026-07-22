package com.enviro.assessment.junior.lindokuhleyende.controller;

import com.enviro.assessment.junior.lindokuhleyende.entity.enums.WithdrawalStatus;
import com.enviro.assessment.junior.lindokuhleyende.service.CsvExportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@Tag(name = "Reports", description = "Endpoints for exporting withdrawal statements")
public class ReportController {

    private final CsvExportService csvExportService;

    @GetMapping("/withdrawals/csv")
    @Operation(summary = "Export withdrawal statement as CSV",
            description = "Downloads a CSV file of withdrawal notices, optionally filtered by investor, product, status and date range.")
    public ResponseEntity<byte[]> exportWithdrawalsCsv(
            @RequestParam(required = false) Long investorId,
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false) WithdrawalStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {

        String csv = csvExportService.exportWithdrawalsCsv(investorId, productId, status, from, to);
        byte[] bytes = csv.getBytes(java.nio.charset.StandardCharsets.UTF_8);

        String filename = "withdrawal-statement-"
                + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss")) + ".csv";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv"));
        headers.setContentDisposition(ContentDisposition.attachment().filename(filename).build());

        return ResponseEntity.ok().headers(headers).body(bytes);
    }
}

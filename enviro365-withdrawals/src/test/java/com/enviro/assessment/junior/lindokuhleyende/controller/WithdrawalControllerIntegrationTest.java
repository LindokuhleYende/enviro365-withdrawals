package com.enviro.assessment.junior.lindokuhleyende.controller;

import com.enviro.assessment.junior.lindokuhleyende.dto.WithdrawalRequestDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * End-to-end tests exercising the full Spring context (controller -> service -> repository -> H2)
 * using the data.sql seed data:
 *  - Investor 1 (John Smith) is ~40s, holds a DISCRETIONARY product (id 1) with balance 250000
 *    and a RETIREMENT product (id 2) with balance 500000.
 *  - Investor 2 (Mary Johnson) is 65+, holds a RETIREMENT product (id 3) with balance 800000.
 */
@SpringBootTest
@AutoConfigureMockMvc
class WithdrawalControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createWithdrawal_succeeds_forValidDiscretionaryWithdrawal() throws Exception {
        WithdrawalRequestDto request = new WithdrawalRequestDto(1L, 1L, new BigDecimal("1000.00"), "Test withdrawal");

        mockMvc.perform(post("/api/withdrawals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.amountRequested").value(1000.00));
    }

    @Test
    void createWithdrawal_rejectsRetirementWithdrawal_forInvestorUnder65() throws Exception {
        // Investor 1 (John Smith, ~40s) attempting to withdraw from RETIREMENT product id 2
        WithdrawalRequestDto request = new WithdrawalRequestDto(1L, 2L, new BigDecimal("1000.00"), null);

        mockMvc.perform(post("/api/withdrawals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Retirement withdrawals")));
    }

    @Test
    void createWithdrawal_rejectsAmountExceedingBalance() throws Exception {
        WithdrawalRequestDto request = new WithdrawalRequestDto(1L, 1L, new BigDecimal("999999.00"), null);

        mockMvc.perform(post("/api/withdrawals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("exceeds the available balance")));
    }

    @Test
    void createWithdrawal_rejectsAmountExceedingNinetyPercentCap() throws Exception {
        // Product 1 balance is 250000 -> 90% cap is 225000
        WithdrawalRequestDto request = new WithdrawalRequestDto(1L, 1L, new BigDecimal("240000.00"), null);

        mockMvc.perform(post("/api/withdrawals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("90%")));
    }

    @Test
    void createWithdrawal_returnsValidationError_whenAmountMissing() throws Exception {
        String invalidJson = "{\"investorId\":1,\"productId\":1}";

        mockMvc.perform(post("/api/withdrawals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"));
    }

    @Test
    void getPortfolio_returns404_forUnknownInvestor() throws Exception {
        mockMvc.perform(get("/api/investors/9999/portfolio"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getPortfolio_returnsInvestorAndProducts() throws Exception {
        mockMvc.perform(get("/api/investors/2/portfolio"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("Mary Johnson"))
                .andExpect(jsonPath("$.products").isArray());
    }

    @Test
    void exportCsv_returnsCsvContentType() throws Exception {
        mockMvc.perform(get("/api/reports/withdrawals/csv").param("investorId", "1"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("text/csv"));
    }
}

package com.enviro.assessment.junior.lindokuhleyende;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the Enviro365 Investments Withdrawal Notice Automation System.
 *
 * This Spring Boot application exposes REST APIs that allow investors to:
 *  - View their investment portfolios
 *  - Submit withdrawal notices (validated against business rules)
 *  - View withdrawal history
 *  - Export withdrawal statements as CSV
 */
@SpringBootApplication
public class Enviro365WithdrawalsApplication {

    public static void main(String[] args) {
        SpringApplication.run(Enviro365WithdrawalsApplication.class, args);
    }
}

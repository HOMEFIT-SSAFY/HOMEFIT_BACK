package com.happyhome.loan.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record LoanCalculationRequest(
        @NotNull @DecimalMin("1") BigDecimal amount,
        @Min(1) @Max(50) int years,
        @NotNull @DecimalMin("0.0") BigDecimal rate,
        @NotNull LoanRepaymentType repaymentType
) {
}

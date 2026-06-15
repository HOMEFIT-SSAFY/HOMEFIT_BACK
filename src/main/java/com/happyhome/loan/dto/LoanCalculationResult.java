package com.happyhome.loan.dto;

import java.math.BigDecimal;

public record LoanCalculationResult(
        BigDecimal monthlyPayment,
        BigDecimal firstMonthPayment,
        BigDecimal lastMonthPayment,
        BigDecimal totalPayment,
        BigDecimal totalInterest,
        BigDecimal annualRate,
        int months,
        String repaymentTypeName
) {
}

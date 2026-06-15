package com.happyhome.loan.dto;

import java.math.BigDecimal;

public record LoanRateOption(
        String optionId,
        String productCode,
        String repaymentTypeCode,
        String repaymentTypeName,
        String rateTypeCode,
        String rateTypeName,
        String mortgageTypeCode,
        String mortgageTypeName,
        BigDecimal rateMin,
        BigDecimal rateMax,
        BigDecimal rateAvg
) {
}

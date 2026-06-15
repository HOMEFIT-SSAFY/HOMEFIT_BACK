package com.happyhome.loan.dto;

import java.util.List;

public record LoanProduct(
        LoanType loanType,
        String companyCode,
        String companyName,
        String productCode,
        String productName,
        String joinWay,
        String loanIncidentalExpense,
        String earlyRepaymentFee,
        String delinquencyRate,
        String loanLimit,
        String disclosureStartDay,
        String disclosureEndDay,
        String submittedAt,
        List<LoanRateOption> options
) {
}

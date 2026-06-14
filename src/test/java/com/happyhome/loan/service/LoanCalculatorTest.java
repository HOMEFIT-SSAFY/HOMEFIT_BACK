package com.happyhome.loan.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.happyhome.loan.dto.LoanCalculationRequest;
import com.happyhome.loan.dto.LoanCalculationResult;
import com.happyhome.loan.dto.LoanRepaymentType;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class LoanCalculatorTest {

    private final LoanCalculator calculator = new LoanCalculator();

    @Test
    void calculatesEqualPaymentMonthlyAmount() {
        LoanCalculationRequest request = new LoanCalculationRequest(
                BigDecimal.valueOf(300_000_000L),
                30,
                BigDecimal.valueOf(4.2),
                LoanRepaymentType.EQUAL_PAYMENT
        );

        LoanCalculationResult result = calculator.calculate(request);

        assertThat(result.monthlyPayment()).isEqualByComparingTo("1467052");
        assertThat(result.totalPayment()).isEqualByComparingTo("528138720");
        assertThat(result.totalInterest()).isEqualByComparingTo("228138720");
    }

    @Test
    void calculatesEqualPrincipalFirstAndLastMonth() {
        LoanCalculationRequest request = new LoanCalculationRequest(
                BigDecimal.valueOf(120_000_000L),
                10,
                BigDecimal.valueOf(3.6),
                LoanRepaymentType.EQUAL_PRINCIPAL
        );

        LoanCalculationResult result = calculator.calculate(request);

        assertThat(result.monthlyPayment()).isEqualByComparingTo("1360000");
        assertThat(result.firstMonthPayment()).isEqualByComparingTo("1360000");
        assertThat(result.lastMonthPayment()).isEqualByComparingTo("1003000");
        assertThat(result.totalInterest()).isEqualByComparingTo("21780000");
    }

    @Test
    void calculatesBulletPaymentMonthlyInterestAndFinalPayment() {
        LoanCalculationRequest request = new LoanCalculationRequest(
                BigDecimal.valueOf(50_000_000L),
                2,
                BigDecimal.valueOf(4.8),
                LoanRepaymentType.BULLET
        );

        LoanCalculationResult result = calculator.calculate(request);

        assertThat(result.monthlyPayment()).isEqualByComparingTo("200000");
        assertThat(result.lastMonthPayment()).isEqualByComparingTo("50200000");
        assertThat(result.totalPayment()).isEqualByComparingTo("54800000");
        assertThat(result.totalInterest()).isEqualByComparingTo("4800000");
    }
}

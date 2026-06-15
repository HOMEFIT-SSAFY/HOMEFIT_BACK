package com.happyhome.loan.service;

import com.happyhome.loan.dto.LoanCalculationRequest;
import com.happyhome.loan.dto.LoanCalculationResult;
import java.math.BigDecimal;
import java.math.RoundingMode;
import org.springframework.stereotype.Service;

@Service
public class LoanCalculator {

    private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);
    private static final BigDecimal TWELVE = BigDecimal.valueOf(12);

    public LoanCalculationResult calculate(LoanCalculationRequest request) {
        int months = request.years() * 12;
        BigDecimal monthlyRate = request.rate().divide(ONE_HUNDRED, 12, RoundingMode.HALF_UP)
                .divide(TWELVE, 12, RoundingMode.HALF_UP);

        return switch (request.repaymentType()) {
            case EQUAL_PAYMENT -> equalPayment(request, months, monthlyRate);
            case EQUAL_PRINCIPAL -> equalPrincipal(request, months, monthlyRate);
            case BULLET -> bullet(request, months, monthlyRate);
        };
    }

    private LoanCalculationResult equalPayment(LoanCalculationRequest request, int months, BigDecimal monthlyRate) {
        BigDecimal monthlyPayment;
        if (monthlyRate.compareTo(BigDecimal.ZERO) == 0) {
            monthlyPayment = request.amount().divide(BigDecimal.valueOf(months), 0, RoundingMode.HALF_UP);
        } else {
            double rate = monthlyRate.doubleValue();
            double factor = Math.pow(1 + rate, months);
            double payment = request.amount().doubleValue() * rate * factor / (factor - 1);
            monthlyPayment = BigDecimal.valueOf(payment).setScale(0, RoundingMode.HALF_UP);
        }
        BigDecimal totalPayment = monthlyPayment.multiply(BigDecimal.valueOf(months));
        return result(request, months, monthlyPayment, monthlyPayment, monthlyPayment, totalPayment);
    }

    private LoanCalculationResult equalPrincipal(LoanCalculationRequest request, int months, BigDecimal monthlyRate) {
        BigDecimal principalPerMonth = request.amount().divide(BigDecimal.valueOf(months), 0, RoundingMode.HALF_UP);
        BigDecimal firstInterest = request.amount().multiply(monthlyRate).setScale(0, RoundingMode.HALF_UP);
        BigDecimal lastBalance = principalPerMonth;
        BigDecimal lastInterest = lastBalance.multiply(monthlyRate).setScale(0, RoundingMode.HALF_UP);
        BigDecimal totalInterest = request.amount().multiply(monthlyRate)
                .multiply(BigDecimal.valueOf(months + 1))
                .divide(BigDecimal.valueOf(2), 0, RoundingMode.HALF_UP);
        BigDecimal totalPayment = request.amount().add(totalInterest);
        BigDecimal firstPayment = principalPerMonth.add(firstInterest);
        BigDecimal lastPayment = principalPerMonth.add(lastInterest);
        return result(request, months, firstPayment, firstPayment, lastPayment, totalPayment);
    }

    private LoanCalculationResult bullet(LoanCalculationRequest request, int months, BigDecimal monthlyRate) {
        BigDecimal monthlyInterest = request.amount().multiply(monthlyRate).setScale(0, RoundingMode.HALF_UP);
        BigDecimal totalInterest = monthlyInterest.multiply(BigDecimal.valueOf(months));
        BigDecimal totalPayment = request.amount().add(totalInterest);
        BigDecimal lastPayment = request.amount().add(monthlyInterest);
        return result(request, months, monthlyInterest, monthlyInterest, lastPayment, totalPayment);
    }

    private LoanCalculationResult result(
            LoanCalculationRequest request,
            int months,
            BigDecimal monthlyPayment,
            BigDecimal firstMonthPayment,
            BigDecimal lastMonthPayment,
            BigDecimal totalPayment
    ) {
        return new LoanCalculationResult(
                monthlyPayment,
                firstMonthPayment,
                lastMonthPayment,
                totalPayment,
                totalPayment.subtract(request.amount()),
                request.rate(),
                months,
                request.repaymentType().label()
        );
    }
}

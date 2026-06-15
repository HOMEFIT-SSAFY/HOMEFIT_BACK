package com.happyhome.loan.service;

import com.happyhome.loan.dto.LoanProduct;
import com.happyhome.loan.dto.LoanRateOption;
import com.happyhome.loan.dto.LoanType;
import java.math.BigDecimal;
import java.util.List;

public final class SampleLoanProducts {

    private SampleLoanProducts() {
    }

    public static List<LoanProduct> products(LoanType type) {
        return type == LoanType.RENT_HOUSE ? rentHouse() : mortgage();
    }

    public static List<LoanProduct> mortgage() {
        return List.of(new LoanProduct(
                LoanType.MORTGAGE,
                "0010001",
                "행복은행",
                "M001",
                "주거래아파트론",
                "영업점, 인터넷, 모바일",
                "인지세 고객/은행 각 50%",
                "3년 이내 상환금액의 1.2%",
                "최고 연 15%",
                "담보평가 및 심사 기준에 따름",
                "20260601",
                "",
                "202606140930",
                List.of(
                        new LoanRateOption("M001-0", "M001", "D", "원리금분할상환", "F", "고정금리", "A", "아파트",
                                BigDecimal.valueOf(3.8), BigDecimal.valueOf(5.1), BigDecimal.valueOf(4.25)),
                        new LoanRateOption("M001-1", "M001", "D", "원금분할상환", "V", "변동금리", "A", "아파트",
                                BigDecimal.valueOf(3.6), BigDecimal.valueOf(5.3), BigDecimal.valueOf(4.15))
                )
        ));
    }

    public static List<LoanProduct> rentHouse() {
        return List.of(new LoanProduct(
                LoanType.RENT_HOUSE,
                "0010002",
                "든든은행",
                "R001",
                "전세보증금대출",
                "영업점, 모바일",
                "보증료 별도",
                "2년 이내 상환금액의 0.7%",
                "최고 연 15%",
                "최대 5억원",
                "20260601",
                "",
                "202606140930",
                List.of(
                        new LoanRateOption("R001-0", "R001", "B", "만기일시상환", "V", "변동금리", "", "",
                                BigDecimal.valueOf(3.4), BigDecimal.valueOf(4.8), BigDecimal.valueOf(4.05)),
                        new LoanRateOption("R001-1", "R001", "D", "원리금분할상환", "F", "고정금리", "", "",
                                BigDecimal.valueOf(3.9), BigDecimal.valueOf(5.0), BigDecimal.valueOf(4.35))
                )
        ));
    }
}

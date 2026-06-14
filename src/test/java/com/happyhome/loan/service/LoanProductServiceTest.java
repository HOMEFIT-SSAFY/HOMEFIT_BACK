package com.happyhome.loan.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.happyhome.loan.dto.LoanProduct;
import com.happyhome.loan.dto.LoanType;
import com.happyhome.openapi.FinLifeLoanApiClient;
import java.util.List;
import org.junit.jupiter.api.Test;

class LoanProductServiceTest {

    @Test
    void exposesMortgageProductsWithSelectableRateOptions() {
        LoanProductService service = new LoanProductService(new StubClient());

        List<LoanProduct> products = service.products(LoanType.MORTGAGE, 1);

        assertThat(products).hasSize(1);
        assertThat(products.get(0).productName()).isEqualTo("주거래아파트론");
        assertThat(products.get(0).options())
                .extracting(option -> option.repaymentTypeName() + "/" + option.rateTypeName() + "/" + option.mortgageTypeName())
                .containsExactly(
                        "원리금분할상환/고정금리/아파트",
                        "원금분할상환/변동금리/아파트"
                );
        assertThat(products.get(0).options().get(0).rateMin()).isEqualByComparingTo("3.8");
        assertThat(products.get(0).options().get(0).optionId()).isEqualTo("M001-0");
    }

    private static class StubClient extends FinLifeLoanApiClient {

        StubClient() {
            super(null);
        }

        @Override
        public List<LoanProduct> products(LoanType type, int page) {
            return List.of(SampleLoanProducts.mortgage().get(0));
        }
    }
}

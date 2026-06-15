package com.happyhome.openapi;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.happyhome.config.OpenApiProperties;
import com.happyhome.loan.dto.LoanProduct;
import com.happyhome.loan.dto.LoanRateOption;
import com.happyhome.loan.dto.LoanType;
import com.happyhome.loan.service.SampleLoanProducts;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class FinLifeLoanApiClient {

    private static final String BANK_GROUP_CODE = "050000";

    private final OpenApiProperties properties;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestClient restClient;

    public FinLifeLoanApiClient(OpenApiProperties properties) {
        this.properties = properties;
        this.restClient = RestClient.create();
    }

    public List<LoanProduct> products(LoanType type, int page) {
        if (properties == null || !OpenApiUri.hasText(properties.getFinlife().getAuth())) {
            return SampleLoanProducts.products(type);
        }
        try {
            String body = restClient.get()
                    .uri(OpenApiUri.build(url(type), Map.of(
                            "auth", properties.getFinlife().getAuth(),
                            "topFinGrpNo", BANK_GROUP_CODE,
                            "pageNo", page
                    )))
                    .retrieve()
                    .body(String.class);
            List<LoanProduct> products = parse(type, body);
            return products.isEmpty() ? SampleLoanProducts.products(type) : products;
        } catch (Exception e) {
            return SampleLoanProducts.products(type);
        }
    }

    private String url(LoanType type) {
        return type == LoanType.RENT_HOUSE
                ? properties.getFinlife().getRentHouseLoanUrl()
                : properties.getFinlife().getMortgageLoanUrl();
    }

    private List<LoanProduct> parse(LoanType type, String body) throws Exception {
        JsonNode result = objectMapper.readTree(body).path("result");
        Map<String, ProductBuilder> products = new LinkedHashMap<>();
        for (JsonNode node : result.path("baseList")) {
            ProductBuilder builder = new ProductBuilder(type, node);
            products.put(builder.productCode, builder);
        }
        int index = 0;
        for (JsonNode node : result.path("optionList")) {
            String productCode = text(node, "fin_prdt_cd");
            ProductBuilder builder = products.get(productCode);
            if (builder != null) {
                builder.options.add(option(type, node, index++));
            }
        }
        return products.values().stream().map(ProductBuilder::build).toList();
    }

    private LoanRateOption option(LoanType type, JsonNode node, int index) {
        String productCode = text(node, "fin_prdt_cd");
        String mortgageTypeCode = type == LoanType.MORTGAGE ? text(node, "mrtg_type") : "";
        String mortgageTypeName = type == LoanType.MORTGAGE ? text(node, "mrtg_type_nm") : "";
        return new LoanRateOption(
                productCode + "-" + index,
                productCode,
                text(node, "rpay_type"),
                text(node, "rpay_type_nm"),
                text(node, "lend_rate_type"),
                text(node, "lend_rate_type_nm"),
                mortgageTypeCode,
                mortgageTypeName,
                decimal(node, "lend_rate_min"),
                decimal(node, "lend_rate_max"),
                decimal(node, "lend_rate_avg")
        );
    }

    private static BigDecimal decimal(JsonNode node, String field) {
        String value = text(node, field);
        return value.isBlank() ? null : new BigDecimal(value);
    }

    private static String text(JsonNode node, String field) {
        return node.path(field).asText("");
    }

    private static class ProductBuilder {
        private final LoanType loanType;
        private final String companyCode;
        private final String companyName;
        private final String productCode;
        private final String productName;
        private final String joinWay;
        private final String loanIncidentalExpense;
        private final String earlyRepaymentFee;
        private final String delinquencyRate;
        private final String loanLimit;
        private final String disclosureStartDay;
        private final String disclosureEndDay;
        private final String submittedAt;
        private final List<LoanRateOption> options = new ArrayList<>();

        ProductBuilder(LoanType loanType, JsonNode node) {
            this.loanType = loanType;
            this.companyCode = text(node, "fin_co_no");
            this.companyName = text(node, "kor_co_nm");
            this.productCode = text(node, "fin_prdt_cd");
            this.productName = text(node, "fin_prdt_nm");
            this.joinWay = text(node, "join_way");
            this.loanIncidentalExpense = text(node, "loan_inci_expn");
            this.earlyRepaymentFee = text(node, "erly_rpay_fee");
            this.delinquencyRate = text(node, "dly_rate");
            this.loanLimit = text(node, "loan_lmt");
            this.disclosureStartDay = text(node, "dcls_strt_day");
            this.disclosureEndDay = text(node, "dcls_end_day");
            this.submittedAt = text(node, "fin_co_subm_day");
        }

        LoanProduct build() {
            return new LoanProduct(
                    loanType,
                    companyCode,
                    companyName,
                    productCode,
                    productName,
                    joinWay,
                    loanIncidentalExpense,
                    earlyRepaymentFee,
                    delinquencyRate,
                    loanLimit,
                    disclosureStartDay,
                    disclosureEndDay,
                    submittedAt,
                    List.copyOf(options)
            );
        }
    }
}

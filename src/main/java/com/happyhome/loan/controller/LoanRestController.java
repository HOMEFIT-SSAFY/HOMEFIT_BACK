package com.happyhome.loan.controller;

import com.happyhome.loan.dto.LoanCalculationRequest;
import com.happyhome.loan.dto.LoanCalculationResult;
import com.happyhome.loan.dto.LoanProduct;
import com.happyhome.loan.dto.LoanType;
import com.happyhome.loan.dto.PropertyLoanAnalysisRequest;
import com.happyhome.loan.dto.PropertyLoanAnalysisResult;
import com.happyhome.house.service.HouseDealService;
import com.happyhome.member.service.FinancialProfileService;
import com.happyhome.loan.service.LoanCalculator;
import com.happyhome.loan.service.LoanProductService;
import com.happyhome.loan.service.PropertyLoanAnalysisService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/api/loans")
@Tag(name = "Loan Calculator", description = "주택담보대출/전세자금대출 상품 조회 및 월 납입액 계산 API")
public class LoanRestController {

    private final LoanProductService productService;
    private final LoanCalculator calculator;
    private final PropertyLoanAnalysisService propertyLoanAnalysisService;
    private final HouseDealService houseDealService;
    private final FinancialProfileService financialProfileService;

    public LoanRestController(
            LoanProductService productService,
            LoanCalculator calculator,
            PropertyLoanAnalysisService propertyLoanAnalysisService,
            HouseDealService houseDealService,
            FinancialProfileService financialProfileService
    ) {
        this.productService = productService;
        this.calculator = calculator;
        this.propertyLoanAnalysisService = propertyLoanAnalysisService;
        this.houseDealService = houseDealService;
        this.financialProfileService = financialProfileService;
    }

    @Operation(summary = "대출 상품 조회", description = "금감원 금융상품 한눈에 API의 상품 기본정보와 금리 옵션정보를 묶어 조회합니다.")
    @GetMapping("/products")
    public List<LoanProduct> products(
            @RequestParam(defaultValue = "MORTGAGE") LoanType type,
            @RequestParam(defaultValue = "1") int page
    ) {
        return productService.products(type, page);
    }

    @Operation(summary = "월 납입액 계산", description = "대출금, 기간, 연 금리, 상환방식으로 예상 납입액을 계산합니다.")
    @PostMapping("/calculate")
    public LoanCalculationResult calculate(@Valid @RequestBody LoanCalculationRequest request) {
        return calculator.calculate(request);
    }

    @PostMapping("/property-analysis")
    public ResponseEntity<PropertyLoanAnalysisResult> propertyAnalysis(
            @Valid @RequestBody PropertyLoanAnalysisRequest request,
            Authentication authentication
    ) {
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return financialProfileService.findByUserId(authentication.getName())
                .flatMap(profile -> houseDealService.findByNo(request.dealNo())
                        .map(deal -> propertyLoanAnalysisService.analyze(deal, profile, request)))
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}

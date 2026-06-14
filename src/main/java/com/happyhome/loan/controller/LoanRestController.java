package com.happyhome.loan.controller;

import com.happyhome.loan.dto.LoanCalculationRequest;
import com.happyhome.loan.dto.LoanCalculationResult;
import com.happyhome.loan.dto.LoanProduct;
import com.happyhome.loan.dto.LoanType;
import com.happyhome.loan.service.LoanCalculator;
import com.happyhome.loan.service.LoanProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/loans")
@Tag(name = "Loan Calculator", description = "주택담보대출/전세자금대출 상품 조회 및 월 납입액 계산 API")
public class LoanRestController {

    private final LoanProductService productService;
    private final LoanCalculator calculator;

    public LoanRestController(LoanProductService productService, LoanCalculator calculator) {
        this.productService = productService;
        this.calculator = calculator;
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
}

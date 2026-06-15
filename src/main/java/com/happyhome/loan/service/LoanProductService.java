package com.happyhome.loan.service;

import com.happyhome.loan.dto.LoanProduct;
import com.happyhome.loan.dto.LoanType;
import com.happyhome.openapi.FinLifeLoanApiClient;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class LoanProductService {

    private final FinLifeLoanApiClient client;

    public LoanProductService(FinLifeLoanApiClient client) {
        this.client = client;
    }

    public List<LoanProduct> products(LoanType type, int page) {
        return client.products(type, Math.max(page, 1));
    }
}

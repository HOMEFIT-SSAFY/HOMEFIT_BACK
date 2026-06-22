package com.happyhome.batch.mapper;

import com.happyhome.loan.dto.LoanProduct;
import com.happyhome.loan.dto.LoanRateOption;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface LoanProductBatchMapper {
    void upsertProduct(LoanProduct product);

    void deleteOptionsByProductCode(String productCode);

    void insertOption(LoanRateOption option);
}

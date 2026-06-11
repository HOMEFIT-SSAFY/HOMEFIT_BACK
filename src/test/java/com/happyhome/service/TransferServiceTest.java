package com.happyhome.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.happyhome.transfer.dto.TransferDto;
import com.happyhome.transfer.dto.TransferRequest;
import com.happyhome.transfer.dto.TransferSearchCondition;
import com.happyhome.transfer.service.TransferService;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:happyhome-transfer-test;MODE=MySQL;DATABASE_TO_LOWER=TRUE;CASE_INSENSITIVE_IDENTIFIERS=TRUE",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.datasource.driver-class-name=org.h2.Driver"
})
class TransferServiceTest {

    @Autowired
    private TransferService transferService;

    @Test
    void createSearchUpdateAndDeleteTransfer() {
        TransferRequest request = request("Sunny room transfer", "available");
        request.getImageUrls().add("https://example.com/room.jpg");

        TransferDto created = transferService.create(request, "ssafy");

        assertThat(created.getTransferId()).isNotNull();
        assertThat(created.getImageUrls()).containsExactly("https://example.com/room.jpg");

        TransferSearchCondition condition = new TransferSearchCondition();
        condition.setKeyword("Sunny");
        condition.setStatus("available");

        assertThat(transferService.findAll(condition))
                .extracting(TransferDto::getTransferId)
                .contains(created.getTransferId());

        TransferDto detail = transferService.findById(created.getTransferId(), true).orElseThrow();
        assertThat(detail.getViewCount()).isEqualTo(1);

        TransferRequest update = request("Updated room transfer", "done");
        update.getImageUrls().add("https://example.com/updated.jpg");
        TransferDto updated = transferService.update(created.getTransferId(), update, "other").orElseThrow();

        assertThat(updated.getTitle()).isEqualTo("Updated room transfer");
        assertThat(updated.getWriterId()).isEqualTo("ssafy");
        assertThat(updated.getImageUrls()).containsExactly("https://example.com/updated.jpg");

        transferService.delete(created.getTransferId());

        assertThat(transferService.findById(created.getTransferId(), false)).isEmpty();
    }

    private TransferRequest request(String title, String status) {
        TransferRequest request = new TransferRequest();
        request.setTitle(title);
        request.setContent("Lease takeover details");
        request.setStatus(status);
        request.setAddress("Seoul Gangnam");
        request.setDetailAddress("101");
        request.setFloor("5F");
        request.setExclusiveArea(new BigDecimal("23.40"));
        request.setDepositAmount(1000);
        request.setMonthlyRentAmount(70);
        request.setMaintenanceFee(8);
        request.setTransferFee(0);
        request.setContractEndDate(LocalDate.of(2027, 1, 1));
        request.setMoveInDate(LocalDate.of(2026, 7, 1));
        request.setContactPhone("010-1234-5678");
        return request;
    }
}

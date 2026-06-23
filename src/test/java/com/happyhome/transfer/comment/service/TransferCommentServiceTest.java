package com.happyhome.transfer.comment.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.happyhome.transfer.comment.dto.TransferCommentDto;
import com.happyhome.transfer.comment.dto.TransferCommentRequest;
import com.happyhome.transfer.dto.TransferRequest;
import com.happyhome.transfer.service.TransferService;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.AccessDeniedException;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:happyhome-transfer-comment-test;MODE=MySQL;DATABASE_TO_LOWER=TRUE;CASE_INSENSITIVE_IDENTIFIERS=TRUE",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.datasource.driver-class-name=org.h2.Driver"
})
class TransferCommentServiceTest {

    @Autowired
    private TransferService transferService;

    @Autowired
    private TransferCommentService transferCommentService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void createListUpdateAndDeleteComment() {
        jdbcTemplate.update("""
                INSERT INTO members (user_id, password, name, email, phone)
                VALUES ('ssafy', 'password', 'SSAFY', 'ssafy@example.com', '010-0000-0000')
                """);
        int transferId = transferService.create(transferRequest(), "ssafy").getTransferId();

        TransferCommentDto created = transferCommentService.create(transferId, request("first comment"), "ssafy");

        assertThat(created.getCommentId()).isNotNull();
        assertThat(created.getTransferId()).isEqualTo(transferId);
        assertThat(created.getWriterId()).isEqualTo("ssafy");
        assertThat(created.getContent()).isEqualTo("first comment");

        assertThat(transferCommentService.findByTransferId(transferId))
                .extracting(TransferCommentDto::getContent)
                .containsExactly("first comment");

        assertThatThrownBy(() -> transferCommentService.update(created.getCommentId(), request("blocked"), "other"))
                .isInstanceOf(AccessDeniedException.class);

        TransferCommentDto updated = transferCommentService.update(
                created.getCommentId(),
                request("updated comment"),
                "ssafy"
        ).orElseThrow();

        assertThat(updated.getContent()).isEqualTo("updated comment");

        assertThatThrownBy(() -> transferCommentService.delete(created.getCommentId(), "other"))
                .isInstanceOf(AccessDeniedException.class);

        transferCommentService.delete(created.getCommentId(), "ssafy");

        assertThat(transferCommentService.findByTransferId(transferId)).isEmpty();
    }

    private TransferCommentRequest request(String content) {
        TransferCommentRequest request = new TransferCommentRequest();
        request.setContent(content);
        return request;
    }

    private TransferRequest transferRequest() {
        TransferRequest request = new TransferRequest();
        request.setTitle("Comment target transfer");
        request.setContent("Lease takeover details");
        request.setStatus("available");
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

package com.happyhome.rental.dao;

import static org.assertj.core.api.Assertions.assertThat;

import com.happyhome.rental.dto.RentalDetail;
import com.happyhome.rental.dto.RentalSupply;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:happyhome-rental-notice-mapper-test;MODE=MySQL;DATABASE_TO_LOWER=TRUE;CASE_INSENSITIVE_IDENTIFIERS=TRUE",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.datasource.driver-class-name=org.h2.Driver"
})
class RentalNoticeMapperTest {

    @Autowired
    private RentalNoticeMapper rentalNoticeMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void findsLhDetailAndSuppliesBySelectedNoticeId() {
        insertNotice("SEOUL-001", "Seoul Notice", "Seoul");
        insertNotice("BUSAN-001", "Busan Notice", "Busan");
        insertDetail("SEOUL-001", "Seoul contract address", "Seoul detail", "2026-07-01", "2026-07-02", "02-1111-2222");
        insertDetail("BUSAN-001", "Busan contract address", "Busan detail", "2026-08-01", "2026-08-02", "051-1111-2222");
        insertSupply("SEOUL-001", "Seoul usage", "Seoul supply address", "39A", "10000000", "A", "10");
        insertSupply("BUSAN-001", "Busan usage", "Busan supply address", "46B", "20000000", "B", "20");

        RentalDetail detail = rentalNoticeMapper.findDetailByNoticeId("BUSAN-001").orElseThrow();
        List<RentalSupply> supplies = rentalNoticeMapper.findSuppliesByNoticeId("BUSAN-001");

        assertThat(detail).isEqualTo(new RentalDetail(
                "Busan contract address",
                "Busan detail",
                "2026-08-01",
                "2026-08-02",
                "051-1111-2222"
        ));
        assertThat(supplies).containsExactly(new RentalSupply(
                "Busan usage",
                "Busan supply address",
                "46B",
                "20000000",
                "B",
                "20"
        ));
    }

    private void insertNotice(String noticeId, String title, String regionName) {
        jdbcTemplate.update("""
                INSERT INTO rental_notice_cache (
                    notice_id, title, region_name, notice_type, detail_type, status,
                    notice_date, close_date, detail_url, ccr_cnnt_sys_ds_cd,
                    upp_ais_tp_cd, ais_tp_cd, spl_inf_tp_cd, source
                ) VALUES (?, ?, ?, 'notice type', 'detail type', 'open',
                    '2026-06-01', '2026-06-30', 'https://example.com', '01',
                    '02', '03', '04', 'test')
                """, noticeId, title, regionName);
    }

    private void insertDetail(
            String noticeId,
            String contractAddress,
            String contractDetailAddress,
            String applyStartDate,
            String applyEndDate,
            String contact
    ) {
        jdbcTemplate.update("""
                INSERT INTO lh_notice_details (
                    notice_id, contract_address, contract_detail_address,
                    apply_start_date, apply_end_date, contact
                ) VALUES (?, ?, ?, ?, ?, ?)
                """, noticeId, contractAddress, contractDetailAddress, applyStartDate, applyEndDate, contact);
    }

    private void insertSupply(
            String noticeId,
            String usage,
            String address,
            String area,
            String expectedAmount,
            String houseType,
            String householdCount
    ) {
        jdbcTemplate.update("""
                INSERT INTO lh_notice_supplies (
                    notice_id, `usage`, address, area, expected_amount, house_type, household_count
                ) VALUES (?, ?, ?, ?, ?, ?, ?)
                """, noticeId, usage, address, area, expectedAmount, houseType, householdCount);
    }
}

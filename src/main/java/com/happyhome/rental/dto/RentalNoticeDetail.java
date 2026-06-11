package com.happyhome.rental.dto;

import java.util.List;

public record RentalNoticeDetail(
        RentalNotice notice,
        RentalDetail detail,
        List<RentalSupply> supplies
) {
}


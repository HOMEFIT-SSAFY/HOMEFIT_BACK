package com.happyhome.rental.recommendation.dto;

import com.happyhome.rental.dto.RentalNotice;
import com.happyhome.rental.dto.RentalSupply;
import java.util.List;

public record RentalRecommendation(
        RentalNotice notice,
        int score,
        List<String> reasons,
        List<RentalSupply> supplies
) {
}

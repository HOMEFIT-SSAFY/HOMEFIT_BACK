package com.happyhome.commercial.service;

import com.happyhome.commercial.dto.CommercialPlace;
import com.happyhome.commercial.dto.CommercialSummary;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class CommercialSummaryService {

    public CommercialSummary summarize(List<CommercialPlace> places) {
        int food = 0;
        int cafe = 0;
        int medical = 0;
        int convenience = 0;
        int life = 0;
        for (CommercialPlace place : places) {
            String text = (place.largeCategory() + " " + place.middleCategory() + " " + place.name()).toLowerCase();
            if (containsAny(text, "음식", "식당", "한식", "중식", "일식", "분식")) {
                food++;
            }
            if (containsAny(text, "카페", "커피", "다방")) {
                cafe++;
            }
            if (containsAny(text, "의료", "병원", "약국", "의원")) {
                medical++;
            }
            if (containsAny(text, "편의", "마트", "슈퍼", "편의점")) {
                convenience++;
            }
            if (!containsAny(text, "음식", "식당", "카페", "커피", "의료", "병원", "약국", "편의", "마트", "슈퍼")) {
                life++;
            }
        }
        return new CommercialSummary(places.size(), food, cafe, medical, convenience, life);
    }

    private boolean containsAny(String value, String... keywords) {
        for (String keyword : keywords) {
            if (value.contains(keyword)) {
                return true;
            }
        }
        return false;
    }
}

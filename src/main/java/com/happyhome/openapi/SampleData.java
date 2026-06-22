package com.happyhome.openapi;

import com.happyhome.commercial.dto.CommercialPlace;
import com.happyhome.rental.dto.RentalDetail;
import com.happyhome.rental.dto.RentalNotice;
import com.happyhome.rental.dto.RentalSupply;
import com.happyhome.traffic.dto.TrafficEvent;
import java.util.List;

public final class SampleData {

    private SampleData() {
    }

    public static List<RentalNotice> rentalNotices() {
        return List.of(
                new RentalNotice("SAMPLE-001", "서울 청년 매입임대 입주자 모집", "서울", "임대주택", "청년매입임대", "공고중", "2026.05.10", "2026.05.28", "https://apply.lh.or.kr", "01", "01", "10", "010", "sample"),
                new RentalNotice("SAMPLE-002", "경기 신혼부부 전세임대 모집", "경기", "임대주택", "신혼부부전세임대", "공고중", "2026.05.08", "2026.05.30", "https://apply.lh.or.kr", "01", "01", "07", "010", "sample"),
                new RentalNotice("SAMPLE-003", "부산 행복주택 예비입주자 모집", "부산", "임대주택", "행복주택", "접수예정", "2026.05.01", "2026.06.15", "https://apply.lh.or.kr", "01", "01", "22", "010", "sample")
        );
    }

    public static List<RentalSupply> supplies() {
        return List.of(
                new RentalSupply(
                        "청년 1형",
                        "서울 관악구 봉천동",
                        "",
                        "26㎡",
                        "보증금 200만원 / 월 18만원",
                        "",
                        "원룸형",
                        "18",
                        "",
                        "서울 관악구 봉천동",
                        mapUrl("서울 관악구 봉천동")
                ),
                new RentalSupply(
                        "신혼부부형",
                        "서울 관악구 신림동",
                        "",
                        "30㎡",
                        "보증금 300만원 / 월 21만원",
                        "",
                        "투룸형",
                        "12",
                        "",
                        "서울 관악구 신림동",
                        mapUrl("서울 관악구 신림동")
                )
        );
    }

    private static String mapUrl(String address) {
        return "https://map.naver.com/v5/search/" + java.net.URLEncoder.encode(address, java.nio.charset.StandardCharsets.UTF_8);
    }

    public static RentalDetail detail() {
        return new RentalDetail("LH 서울지역본부", "서울특별시 강남구", "2026.05.21", "2026.05.28", "1600-1004");
    }

    public static List<CommercialPlace> places(double longitude, double latitude) {
        return List.of(
                new CommercialPlace("음식", "한식", "청년식당", "관악구 봉천동 청년식당", longitude + 0.001, latitude + 0.001),
                new CommercialPlace("음식", "카페", "봉천커피", "관악구 봉천동 봉천커피", longitude - 0.001, latitude + 0.001),
                new CommercialPlace("의료", "약국", "중앙약국", "관악구 봉천동 중앙약국", longitude + 0.001, latitude - 0.001),
                new CommercialPlace("생활", "마트", "동네마트", "관악구 봉천동 동네마트", longitude - 0.001, latitude - 0.001),
                new CommercialPlace("생활", "편의점", "세븐편의점", "관악구 봉천동 편의점", longitude + 0.002, latitude)
        );
    }

    public static List<TrafficEvent> trafficEvents(double longitude, double latitude) {
        return List.of(
                new TrafficEvent("공사", "차로 공사로 정체 예상", "봉천로", longitude + 0.002, latitude + 0.002)
        );
    }
}

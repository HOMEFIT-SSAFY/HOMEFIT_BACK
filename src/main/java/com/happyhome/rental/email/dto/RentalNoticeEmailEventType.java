package com.happyhome.rental.email.dto;

public enum RentalNoticeEmailEventType {
    APPLY_OPEN("LH 공고 접수 시작"),
    APPLY_ACTIVE("LH 공고 접수 진행 중"),
    CLOSING_SOON_D3("LH 공고 마감 3일 전"),
    CLOSING_SOON_D2("LH 공고 마감 2일 전"),
    CLOSING_SOON_D1("LH 공고 마감 하루 전"),
    CLOSING_SOON_D0("LH 공고 마감일");

    private final String label;

    RentalNoticeEmailEventType(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }
}

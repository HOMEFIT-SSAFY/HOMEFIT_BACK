package com.happyhome.rental.email.dto;

public enum RentalNoticeEmailEventType {
    APPLY_OPEN("LH 공고 접수 시작"),
    APPLY_ACTIVE("LH 공고 접수 진행 중"),
    CLOSING_SOON("LH 공고 마감 임박");

    private final String label;

    RentalNoticeEmailEventType(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }
}

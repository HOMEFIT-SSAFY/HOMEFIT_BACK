package com.happyhome.loan.dto;

public enum LoanRepaymentType {
    EQUAL_PAYMENT("원리금균등"),
    EQUAL_PRINCIPAL("원금균등"),
    BULLET("만기일시");

    private final String label;

    LoanRepaymentType(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }
}

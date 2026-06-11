package com.happyhome.region.dto;

public class RegionOptionDto {

    private final String value;
    private final String label;

    public RegionOptionDto(String value, String label) {
        this.value = value;
        this.label = label;
    }

    public String getValue() {
        return value;
    }

    public String getLabel() {
        return label;
    }
}

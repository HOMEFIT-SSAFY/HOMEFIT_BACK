package com.happyhome.house.dto;

import java.math.BigDecimal;

public class HouseDeal {

    private int no;
    private String aptSeq;
    private String aptName;
    private String sidoName;
    private String gugunName;
    private String dongName;
    private String umdName;
    private String jibun;
    private String roadName;
    private Integer buildYear;
    private String latitude;
    private String longitude;
    private String aptDong;
    private String floor;
    private Integer dealYear;
    private Integer dealMonth;
    private Integer dealDay;
    private BigDecimal exclusiveArea;
    private String dealAmount;

    public int getNo() {
        return no;
    }

    public void setNo(int no) {
        this.no = no;
    }

    public String getAptSeq() {
        return aptSeq;
    }

    public void setAptSeq(String aptSeq) {
        this.aptSeq = aptSeq;
    }

    public String getAptName() {
        return aptName;
    }

    public void setAptName(String aptName) {
        this.aptName = aptName;
    }

    public String getSidoName() {
        return sidoName;
    }

    public void setSidoName(String sidoName) {
        this.sidoName = sidoName;
    }

    public String getGugunName() {
        return gugunName;
    }

    public void setGugunName(String gugunName) {
        this.gugunName = gugunName;
    }

    public String getDongName() {
        return dongName;
    }

    public void setDongName(String dongName) {
        this.dongName = dongName;
    }

    public String getUmdName() {
        return umdName;
    }

    public void setUmdName(String umdName) {
        this.umdName = umdName;
    }

    public String getJibun() {
        return jibun;
    }

    public void setJibun(String jibun) {
        this.jibun = jibun;
    }

    public String getRoadName() {
        return roadName;
    }

    public void setRoadName(String roadName) {
        this.roadName = roadName;
    }

    public Integer getBuildYear() {
        return buildYear;
    }

    public void setBuildYear(Integer buildYear) {
        this.buildYear = buildYear;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getAptDong() {
        return aptDong;
    }

    public void setAptDong(String aptDong) {
        this.aptDong = aptDong;
    }

    public String getFloor() {
        return floor;
    }

    public void setFloor(String floor) {
        this.floor = floor;
    }

    public Integer getDealYear() {
        return dealYear;
    }

    public void setDealYear(Integer dealYear) {
        this.dealYear = dealYear;
    }

    public Integer getDealMonth() {
        return dealMonth;
    }

    public void setDealMonth(Integer dealMonth) {
        this.dealMonth = dealMonth;
    }

    public Integer getDealDay() {
        return dealDay;
    }

    public void setDealDay(Integer dealDay) {
        this.dealDay = dealDay;
    }

    public BigDecimal getExclusiveArea() {
        return exclusiveArea;
    }

    public void setExclusiveArea(BigDecimal exclusiveArea) {
        this.exclusiveArea = exclusiveArea;
    }

    public String getDealAmount() {
        return dealAmount;
    }

    public void setDealAmount(String dealAmount) {
        this.dealAmount = dealAmount;
    }

    public String getAddress() {
        StringBuilder builder = new StringBuilder();
        append(builder, sidoName);
        append(builder, gugunName);
        append(builder, dongName != null ? dongName : umdName);
        append(builder, jibun);
        return builder.toString();
    }

    public String getDealDate() {
        if (dealYear == null || dealMonth == null || dealDay == null) {
            return "";
        }
        return String.format("%04d-%02d-%02d", dealYear, dealMonth, dealDay);
    }

    private void append(StringBuilder builder, String value) {
        if (value == null || value.isBlank()) {
            return;
        }
        if (builder.length() > 0) {
            builder.append(' ');
        }
        builder.append(value.trim());
    }
}


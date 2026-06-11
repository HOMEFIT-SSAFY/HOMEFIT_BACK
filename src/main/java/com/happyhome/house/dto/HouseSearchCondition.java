package com.happyhome.house.dto;

import org.springframework.util.StringUtils;

public class HouseSearchCondition {

    private String keyword;
    private String sidoName;
    private String gugunName;
    private String dongName;
    private Integer dealYear;
    private String lawdCd;
    private String dealYmd;
    private String mode = "search";
    private int limit = 20;

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
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

    public Integer getDealYear() {
        return dealYear;
    }

    public void setDealYear(Integer dealYear) {
        this.dealYear = dealYear;
    }

    public String getLawdCd() {
        return lawdCd;
    }

    public void setLawdCd(String lawdCd) {
        this.lawdCd = lawdCd;
    }

    public String getDealYmd() {
        return dealYmd;
    }

    public void setDealYmd(String dealYmd) {
        this.dealYmd = dealYmd;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public int getSafeLimit() {
        if (limit <= 0) {
            return 20;
        }
        return Math.min(limit, 500);
    }

    public boolean hasSearchCondition() {
        return "search".equals(mode) && (StringUtils.hasText(keyword)
                || StringUtils.hasText(sidoName)
                || StringUtils.hasText(gugunName)
                || StringUtils.hasText(dongName)
                || dealYear != null
                || StringUtils.hasText(lawdCd)
                || StringUtils.hasText(dealYmd));
    }

    public boolean isRegionSelectMode() {
        return "region".equals(mode);
    }

    public Integer getDealMonthFromYmd() {
        if (!StringUtils.hasText(dealYmd) || dealYmd.trim().length() < 6) {
            return null;
        }
        return Integer.parseInt(dealYmd.trim().substring(4, 6));
    }

    public Integer getDealYearFromYmd() {
        if (!StringUtils.hasText(dealYmd) || dealYmd.trim().length() < 4) {
            return null;
        }
        return Integer.parseInt(dealYmd.trim().substring(0, 4));
    }
}

package com.happyhome.property.service;

import com.happyhome.batch.dto.NoticeLHResult;
import com.happyhome.openapi.RtmsOpenApiClient;
import com.happyhome.property.dao.PropertyDealMapper;
import com.happyhome.property.dto.PropertyDeal;
import com.happyhome.property.dto.PropertyDealType;
import com.happyhome.property.dto.PropertyType;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class PropertyDealSyncService {

    private static final DateTimeFormatter DEAL_YMD_FORMATTER = DateTimeFormatter.ofPattern("yyyyMM");

    private final RtmsOpenApiClient rtmsOpenApiClient;
    private final PropertyDealMapper propertyDealMapper;

    public PropertyDealSyncService(RtmsOpenApiClient rtmsOpenApiClient, PropertyDealMapper propertyDealMapper) {
        this.rtmsOpenApiClient = rtmsOpenApiClient;
        this.propertyDealMapper = propertyDealMapper;
    }

    public NoticeLHResult syncRecent(int months) {
        List<String> lawdCodes = propertyDealMapper.findLawdCodes();
        if (lawdCodes.isEmpty()) {
            lawdCodes = List.of("11680", "11110");
        }
        return sync(lawdCodes, recentDealYmds(months));
    }

    public NoticeLHResult syncRecent(List<String> lawdCodes, int months) {
        List<String> targetLawdCodes = lawdCodes == null || lawdCodes.isEmpty()
                ? propertyDealMapper.findLawdCodes()
                : lawdCodes;
        if (targetLawdCodes.isEmpty()) {
            targetLawdCodes = List.of("11680", "11110");
        }
        return sync(targetLawdCodes, recentDealYmds(months));
    }

    public NoticeLHResult sync(List<String> lawdCodes, List<String> dealYmds) {
        int fetchedCount = 0;
        int savedCount = 0;
        List<String> errors = new ArrayList<>();

        if (!rtmsOpenApiClient.isConfigured()) {
            return new NoticeLHResult("FAILED", 0, 0, List.of("OPENAPI_DATA_SERVICE_KEY is not configured."));
        }

        for (String lawdCd : lawdCodes) {
            for (String dealYmd : dealYmds) {
                for (PropertyType propertyType : List.of(PropertyType.OFFICETEL, PropertyType.ONEROOM)) {
                    for (PropertyDealType dealType : PropertyDealType.values()) {
                        List<PropertyDeal> deals = rtmsOpenApiClient.fetch(propertyType, dealType, lawdCd, dealYmd);
                        fetchedCount += deals.size();
                        for (PropertyDeal deal : deals) {
                            try {
                                propertyDealMapper.upsert(deal);
                                savedCount++;
                            } catch (Exception exception) {
                                errors.add(deal.sourceId() + ": " + exception.getMessage());
                            }
                        }
                    }
                }
            }
        }

        String status = errors.isEmpty() ? "SUCCESS" : (savedCount == 0 ? "FAILED" : "PARTIAL_FAILED");
        return new NoticeLHResult(status, fetchedCount, savedCount, errors);
    }

    private List<String> recentDealYmds(int months) {
        int safeMonths = Math.min(Math.max(months, 1), 24);
        List<String> dealYmds = new ArrayList<>();
        YearMonth current = YearMonth.now();
        for (int index = 0; index < safeMonths; index++) {
            dealYmds.add(current.minusMonths(index).format(DEAL_YMD_FORMATTER));
        }
        return dealYmds;
    }
}

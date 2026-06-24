package com.happyhome.batch.dto;

import com.happyhome.transport.dto.BusStopSyncResult;

public record OpenApiBatchResult(
        NoticeLHResult lhNotices,
        NoticeLHResult loanProducts,
        BusStopSyncResult busStops,
        NoticeLHResult propertyDeals
) {
}

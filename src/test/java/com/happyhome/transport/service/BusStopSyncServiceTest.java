package com.happyhome.transport.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.happyhome.openapi.BusOpenApiClient;
import com.happyhome.transport.dao.BusStopMapper;
import com.happyhome.transport.dto.BusStopStatus;
import com.happyhome.transport.dto.BusStopSyncResult;
import org.junit.jupiter.api.Test;

class BusStopSyncServiceTest {

    private final BusOpenApiClient busOpenApiClient = mock(BusOpenApiClient.class);
    private final BusStopMapper busStopMapper = mock(BusStopMapper.class);
    private final BusStopSyncService busStopSyncService = new BusStopSyncService(busOpenApiClient, busStopMapper);

    @Test
    void syncFailsClearlyWhenBusApiKeyIsMissing() {
        when(busOpenApiClient.isConfigured()).thenReturn(false);

        BusStopSyncResult result = busStopSyncService.syncAll(null);

        assertThat(result.status()).isEqualTo("FAILED");
        assertThat(result.cityCount()).isZero();
        assertThat(result.errors()).containsExactly("OPENAPI_BUS_SERVICE_KEY is not configured.");
        verify(busOpenApiClient, never()).cityCodes();
    }

    @Test
    void statusExplainsMissingConfigurationBeforeDataReadiness() {
        when(busOpenApiClient.isConfigured()).thenReturn(false);
        when(busStopMapper.countBusStops()).thenReturn(0);

        BusStopStatus status = busStopSyncService.status();

        assertThat(status.busApiConfigured()).isFalse();
        assertThat(status.busStopCount()).isZero();
        assertThat(status.busDataReady()).isFalse();
        assertThat(status.message()).contains("OPENAPI_BUS_SERVICE_KEY");
    }

    @Test
    void statusReportsLoadedBusData() {
        when(busOpenApiClient.isConfigured()).thenReturn(true);
        when(busStopMapper.countBusStops()).thenReturn(42);

        BusStopStatus status = busStopSyncService.status();

        assertThat(status.busApiConfigured()).isTrue();
        assertThat(status.busStopCount()).isEqualTo(42);
        assertThat(status.busDataReady()).isTrue();
        assertThat(status.message()).isEqualTo("Bus stop data is ready.");
    }
}

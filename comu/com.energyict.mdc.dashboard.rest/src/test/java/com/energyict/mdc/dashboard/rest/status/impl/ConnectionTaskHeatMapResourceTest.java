package com.energyict.mdc.dashboard.rest.status.impl;

import com.elster.jupiter.devtools.ExtjsFilter;
import com.energyict.mdc.dashboard.ConnectionTaskDeviceTypeHeatMap;
import com.energyict.mdc.dashboard.ConnectionTaskHeatMapRow;
import com.energyict.mdc.dashboard.Counter;
import com.energyict.mdc.dashboard.impl.ComSessionSuccessIndicatorOverviewImpl;
import com.energyict.mdc.dashboard.impl.ConnectionTaskHeatMapRowImpl;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.data.tasks.history.ComSession;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.Response;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ConnectionTaskHeatMapResourceTest extends DashboardRESTJerseyTest {

    @Test
    public void testBadRequestWhenFilterIsMissing() throws Exception {
        Response response = target("/connectionheatmap").request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

    }

    @Test
    public void testConnectionHeatMapJsonBinding() throws Exception {
        ConnectionTaskDeviceTypeHeatMap heatMap = createDeviceTypeHeatMap();
        when(dashboardService.getConnectionsDeviceTypeHeatMap()).thenReturn(heatMap);

        Map<String, Object> map = target("/connectionheatmap").queryParam("filter", ExtjsFilter.filter("breakdown", "deviceTypes")).request().get(Map.class);

        assertThat(map).containsKey("heatMap");
        assertThat(map).containsKey("breakdown");
    }

    private ConnectionTaskDeviceTypeHeatMap createDeviceTypeHeatMap() {
        ConnectionTaskDeviceTypeHeatMap heatMap = mock(ConnectionTaskDeviceTypeHeatMap.class);
        List<ConnectionTaskHeatMapRow<DeviceType>> rows = new ArrayList<>();
        ComSessionSuccessIndicatorOverviewImpl counters = new ComSessionSuccessIndicatorOverviewImpl(103L);
        counters.add(createCounter(ComSession.SuccessIndicator.Broken, 100L));
        counters.add(createCounter(ComSession.SuccessIndicator.SetupError, 101L));
        counters.add(createCounter(ComSession.SuccessIndicator.Success, 102L));
        long id=1;
        for (String name: Arrays.asList("deviceType1", "deviceType2", "deviceType3")) {
            DeviceType deviceType = mock(DeviceType.class);
            when(deviceType.getName()).thenReturn(name);
            when(deviceType.getId()).thenReturn(id++);
            rows.add(new ConnectionTaskHeatMapRowImpl(deviceType, counters));
        }
        when(heatMap.iterator()).thenReturn(rows.iterator());
        return heatMap;
    }

    private <C> Counter<C> createCounter(C status, Long count) {
        Counter<C> counter = mock(Counter.class);
        when(counter.getCount()).thenReturn(count);
        when(counter.getCountTarget()).thenReturn(status);
        return counter;
    }
}
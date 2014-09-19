package com.energyict.mdc.dashboard.rest.status.impl;

import com.energyict.mdc.dashboard.CommunicationTaskHeatMap;
import com.energyict.mdc.dashboard.CommunicationTaskHeatMapRow;
import com.energyict.mdc.dashboard.Counter;
import com.energyict.mdc.dashboard.impl.ComCommandCompletionCodeOverviewImpl;
import com.energyict.mdc.dashboard.impl.CommunicationTaskHeatMapRowImpl;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.data.tasks.history.CompletionCode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CommunicationTaskHeatMapResourceTest extends DashboardApplicationJerseyTest {

    @Test
    public void testConnectionHeatMapJsonBinding() throws Exception {
        CommunicationTaskHeatMap heatMap = createHeatMap();
        when(dashboardService.getCommunicationTasksHeatMap()).thenReturn(heatMap);

        Map<String, Object> map = target("/communicationheatmap").request().get(Map.class);

        assertThat(map).containsKey("heatMap");
    }

    @Test
    public void testConnectionHeatMapSortings() throws Exception {
        CommunicationTaskHeatMap heatMap = createHeatMap();
        when(dashboardService.getCommunicationTasksHeatMap()).thenReturn(heatMap);

        CommunicationHeatMapInfo map = target("/communicationheatmap").request().get(CommunicationHeatMapInfo.class);
        assertThat(map.heatMap).isSortedAccordingTo(new Comparator<HeatMapRowInfo>() {
            @Override
            public int compare(HeatMapRowInfo o1, HeatMapRowInfo o2) {
                return o1.displayValue.compareTo(o2.displayValue);
            }
        });

        for (HeatMapRowInfo heatMapRowInfo : map.heatMap) {
            assertThat(heatMapRowInfo.data).isSortedAccordingTo(new CompletionCodeTaskCounterInfoComparator());
        }

    }

    private CommunicationTaskHeatMap createHeatMap() {
        CommunicationTaskHeatMap heatMap = mock(CommunicationTaskHeatMap.class);
        List<CommunicationTaskHeatMapRow> rows = new ArrayList<>();
        ComCommandCompletionCodeOverviewImpl counters = new ComCommandCompletionCodeOverviewImpl();
        counters.add(createCounter(CompletionCode.ConnectionError, 101L));
        counters.add(createCounter(CompletionCode.ConfigurationError, 100L));
        counters.add(createCounter(CompletionCode.ConfigurationWarning, 6L));
        counters.add(createCounter(CompletionCode.IOError, 102L));
        counters.add(createCounter(CompletionCode.Ok, 1000L));
        counters.add(createCounter(CompletionCode.ProtocolError, 1L));
        counters.add(createCounter(CompletionCode.TimeError, 7L));
        counters.add(createCounter(CompletionCode.UnexpectedError, 0L));
        long id=1;
        for (String name: Arrays.asList("deviceType2", "deviceType1", "deviceType3")) {
            DeviceType deviceType = mock(DeviceType.class);
            when(deviceType.getName()).thenReturn(name);
            when(deviceType.getId()).thenReturn(id++);
            rows.add(new CommunicationTaskHeatMapRowImpl(deviceType, counters));
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
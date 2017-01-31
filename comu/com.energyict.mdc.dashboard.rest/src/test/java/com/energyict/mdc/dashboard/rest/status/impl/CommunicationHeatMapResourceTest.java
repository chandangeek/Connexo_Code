/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.dashboard.rest.status.impl;

import com.elster.jupiter.devtools.ExtjsFilter;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.QueryEndDeviceGroup;
import com.energyict.mdc.dashboard.CommunicationTaskHeatMap;
import com.energyict.mdc.dashboard.CommunicationTaskHeatMapRow;
import com.energyict.mdc.dashboard.Counter;
import com.energyict.mdc.dashboard.impl.ComCommandCompletionCodeOverviewImpl;
import com.energyict.mdc.dashboard.impl.CommunicationTaskHeatMapRowImpl;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.data.tasks.history.CompletionCode;

import com.jayway.jsonpath.JsonModel;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CommunicationHeatMapResourceTest extends DashboardApplicationJerseyTest {

    @Test
    public void testCommunicationHeatMapJsonBinding() throws Exception {
        CommunicationTaskHeatMap heatMap = createHeatMap();
        when(dashboardService.getCommunicationTasksHeatMap()).thenReturn(heatMap);

        String response = target("/communicationheatmap").request().get(String.class);
        JsonModel model = JsonModel.model(response);

        assertThat(model.<Object>get("$.heatMap")).isNotNull();
        assertThat(model.<Object>get("$.breakdown")).isEqualTo("deviceTypes");
        assertThat(model.<Object>get("$.alias")).isEqualTo("deviceTypes");
        assertThat(model.<List>get("$.heatMap")).isNotEmpty();
        assertThat(model.<List>get("$.heatMap[*].displayValue")).containsExactly("deviceType1", "deviceType2", "deviceType3");
    }

    @Test
    public void testCommunicationHeatMapJsonBindingWithDeviceGroup() throws Exception {
        CommunicationTaskHeatMap heatMap = createHeatMap();
        QueryEndDeviceGroup endDeviceGroup = mock(QueryEndDeviceGroup.class);
        Optional<EndDeviceGroup> endDeviceGroupOptional = Optional.of(endDeviceGroup);
        when(meteringGroupsService.findEndDeviceGroup(19L)).thenReturn(endDeviceGroupOptional);
        when(dashboardService.getCommunicationTasksHeatMap(endDeviceGroup)).thenReturn(heatMap);

        String response = target("/communicationheatmap").queryParam("filter", ExtjsFilter.filter("deviceGroup", 19L)).request().get(String.class);
        JsonModel model = JsonModel.model(response);

        assertThat(model.<Object>get("$.heatMap")).isNotNull();
        assertThat(model.<Object>get("$.breakdown")).isEqualTo("deviceTypes");
        assertThat(model.<Object>get("$.alias")).isEqualTo("deviceTypes");
        assertThat(model.<List>get("$.heatMap[*].displayValue")).containsExactly("deviceType1", "deviceType2", "deviceType3");
    }

    @Test
    public void testCommunicationHeatMapJsonBindingWithUnknownDeviceGroup() throws Exception {
        when(meteringGroupsService.findEndDeviceGroup(anyInt())).thenReturn(Optional.<EndDeviceGroup>empty());
        Response response = target("/communicationheatmap").queryParam("filter", ExtjsFilter.filter("deviceGroup", -1L)).request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
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
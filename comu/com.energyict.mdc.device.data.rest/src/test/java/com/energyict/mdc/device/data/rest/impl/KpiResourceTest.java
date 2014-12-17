package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.metering.groups.QueryEndDeviceGroup;
import com.energyict.mdc.common.rest.QueryParameters;
import com.energyict.mdc.common.services.Finder;
import com.energyict.mdc.device.data.kpi.DataCollectionKpi;
import com.jayway.jsonpath.JsonModel;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.TemporalAmount;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by bvn on 12/12/14.
 */
public class KpiResourceTest extends DeviceDataRestApplicationJerseyTest {

    @Test
    public void testGetAllKpis() throws Exception {
        long kpiId = 71L;
        DataCollectionKpi kpiMock = mockKpi(kpiId, mockDeviceGroup("end device group", 1L));
        Finder<DataCollectionKpi> finder = mockFinder(Arrays.asList(kpiMock));
        when(dataCollectionKpiService.dataCollectionKpiFinder()).thenReturn(finder);
        String response = target("/kpis").queryParam("start",0).queryParam("limit",10).request().get(String.class);
        JsonModel model = JsonModel.model(response);
        assertThat(model.<Integer>get("total")).isEqualTo(1);
        assertThat(model.<List>get("kpis")).hasSize(1);
    }

    @Test
    public void testGetKpiById() throws Exception {
        long kpiId = 71L;
        DataCollectionKpi kpiMock = mockKpi(kpiId, mockDeviceGroup("end device group bis", 2));
        when(dataCollectionKpiService.findDataCollectionKpi(71L)).thenReturn(Optional.of(kpiMock));
        String response = target("/kpis/71").request().get(String.class);
        JsonModel model = JsonModel.model(response);
        assertThat(model.<Integer>get("id")).isEqualTo(71);
        assertThat(model.<Integer>get("deviceGroup.id")).isEqualTo(2);
        assertThat(model.<String>get("deviceGroup.name")).isEqualTo("end device group bis");
        assertThat(model.<Integer>get("frequency.every.count")).isEqualTo(900);
        assertThat(model.<String>get("frequency.every.timeUnit")).isEqualTo("seconds");
        assertThat(model.<Double>get("communicationTarget")).isEqualTo(15.8);
        assertThat(model.<Double>get("connectionTarget")).isNull();
        assertThat(model.<Instant>get("latestCalculationDate")).isNull();
    }

    private DataCollectionKpi mockKpi(long kpiId, QueryEndDeviceGroup endDeviceGroup) {
        DataCollectionKpi kpiMock = mock(DataCollectionKpi.class);
        when(kpiMock.getDeviceGroup()).thenReturn(endDeviceGroup);
        when(kpiMock.getId()).thenReturn(kpiId);
        when(kpiMock.comTaskExecutionKpiCalculationIntervalLength()).thenReturn(Optional.<TemporalAmount>empty());
        when(kpiMock.connectionSetupKpiCalculationIntervalLength()).thenReturn(Optional.of(Duration.ofMinutes(15)));
        when(kpiMock.getStaticCommunicationKpiTarget()).thenReturn(Optional.of(BigDecimal.valueOf(15.8)));
        when(kpiMock.getStaticConnectionKpiTarget()).thenReturn(Optional.empty());
        return kpiMock;
    }

    private QueryEndDeviceGroup mockDeviceGroup(String name, long id) {
        QueryEndDeviceGroup endDeviceGroup = mock(QueryEndDeviceGroup.class);
        when(endDeviceGroup.getName()).thenReturn(name);
        when(endDeviceGroup.getId()).thenReturn(id);
        return endDeviceGroup;
    }

    private <T> Finder<T> mockFinder(List<T> list) {
        Finder<T> finder = mock(Finder.class);

        when(finder.paged(anyInt(), anyInt())).thenReturn(finder);
        when(finder.sorted(anyString(), any(Boolean.class))).thenReturn(finder);
        when(finder.from(any(QueryParameters.class))).thenReturn(finder);
        when(finder.defaultSortColumn(anyString())).thenReturn(finder);
        when(finder.find()).thenReturn(list);
        when(finder.stream()).thenReturn(list.stream());
        return finder;
    }

}

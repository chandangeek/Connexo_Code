package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.metering.groups.QueryEndDeviceGroup;
import com.energyict.mdc.common.rest.QueryParameters;
import com.energyict.mdc.common.services.Finder;
import com.energyict.mdc.device.data.kpi.DataCollectionKpi;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.temporal.TemporalAmount;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.Test;

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
        DataCollectionKpi mock = mock(DataCollectionKpi.class);
        QueryEndDeviceGroup endDeviceGroup = mock(QueryEndDeviceGroup.class);
        when(endDeviceGroup.getName()).thenReturn("end device group");
        when(endDeviceGroup.getMRID()).thenReturn("ZAFP001");
        when(mock.getDeviceGroup()).thenReturn(endDeviceGroup);
        when(mock.comTaskExecutionKpiCalculationIntervalLength()).thenReturn(Optional.<TemporalAmount>empty());
        when(mock.connectionSetupKpiCalculationIntervalLength()).thenReturn(Optional.of(Duration.ofMinutes(15)));
        when(mock.getStaticCommunicationKpiTarget()).thenReturn(Optional.of(BigDecimal.valueOf(15.8)));
        when(mock.getStaticConnectionKpiTarget()).thenReturn(Optional.empty());
        when(mock.getStaticConnectionKpiTarget()).thenReturn(Optional.empty());
        Finder<DataCollectionKpi> finder = mockFinder(Arrays.asList(mock));
        when(dataCollectionKpiService.dataCollectionKpiFinder()).thenReturn(finder);
        String response = target("/kpis").queryParam("start",0).queryParam("limit",10).request().get(String.class);
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

package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.QueryEndDeviceGroup;
import com.energyict.mdc.common.rest.QueryParameters;
import com.energyict.mdc.common.rest.TimeDurationInfo;
import com.energyict.mdc.common.services.Finder;
import com.energyict.mdc.device.data.kpi.DataCollectionKpi;
import com.energyict.mdc.device.data.kpi.DataCollectionKpiService;
import com.energyict.mdc.device.data.kpi.rest.DataCollectionKpiInfo;
import com.energyict.mdc.device.data.kpi.rest.LongIdWithNameInfo;
import com.energyict.mdc.scheduling.rest.TemporalExpressionInfo;
import com.jayway.jsonpath.JsonModel;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.TemporalAmount;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
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
        assertThat(model.<Instant>get("latestCalculationDate")).isEqualTo(10000000);
    }

    @Test
    public void testDeleteKpi() throws Exception {
        long kpiId = 71L;
        DataCollectionKpi kpiMock = mockKpi(kpiId, mockDeviceGroup("end device group bis", 2));
        when(dataCollectionKpiService.findDataCollectionKpi(71L)).thenReturn(Optional.of(kpiMock));
        Response response = target("/kpis/71").request().delete();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(kpiMock).delete();
    }

    @Test
    public void testCreateKpiForDataCommunication() throws Exception {
        DataCollectionKpiInfo info = new DataCollectionKpiInfo();
        info.communicationTarget=BigDecimal.valueOf(99.9);
        info.deviceGroup=new LongIdWithNameInfo(101L, "some group");
        info.frequency = new TemporalExpressionInfo();
        info.frequency.every = new TimeDurationInfo();
        info.frequency.every.timeUnit="minutes";
        info.frequency.every.count=15;
        Optional<EndDeviceGroup> endDeviceGroupOptional = Optional.of(mock(EndDeviceGroup.class));
        when(meteringGroupService.findEndDeviceGroup(101L)).thenReturn(endDeviceGroupOptional);
        DataCollectionKpiService.DataCollectionKpiBuilder kpiBuilder = mock(DataCollectionKpiService.DataCollectionKpiBuilder.class);
        DataCollectionKpiService.KpiTargetBuilder connectionKpiTargetBuilder = mock(DataCollectionKpiService.KpiTargetBuilder.class);
        DataCollectionKpiService.KpiTargetBuilder communicationKpiTargetBuilder = mock(DataCollectionKpiService.KpiTargetBuilder.class);
        when(kpiBuilder.calculateComTaskExecutionKpi(anyObject())).thenReturn(communicationKpiTargetBuilder);
        when(kpiBuilder.calculateConnectionSetupKpi(anyObject())).thenReturn(connectionKpiTargetBuilder);
        long kpiId = 71L;
        DataCollectionKpi kpiMock = mockKpi(kpiId, mockDeviceGroup("end device group bis", 2));
        when(kpiBuilder.save()).thenReturn(kpiMock);
        when(dataCollectionKpiService.newDataCollectionKpi(endDeviceGroupOptional.get())).thenReturn(kpiBuilder);

        Response response = target("/kpis/").request().post(Entity.json(info));

        assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());
        verify(dataCollectionKpiService).newDataCollectionKpi(endDeviceGroupOptional.get());
        verify(kpiBuilder).calculateComTaskExecutionKpi(Duration.ofMinutes(15));
        verify(kpiBuilder, never()).calculateConnectionSetupKpi(anyObject());
        verify(communicationKpiTargetBuilder).expectingAsMaximum(BigDecimal.valueOf(99.9));
        verify(connectionKpiTargetBuilder, never()).expectingAsMaximum(anyObject());
        verify(kpiBuilder).save();
    }

    @Test
    public void testCreateKpiForDataCoConnection() throws Exception {
        DataCollectionKpiInfo info = new DataCollectionKpiInfo();
        info.connectionTarget=BigDecimal.valueOf(99.1);
        info.deviceGroup=new LongIdWithNameInfo(102L, "some group");
        info.frequency = new TemporalExpressionInfo();
        info.frequency.every = new TimeDurationInfo();
        info.frequency.every.timeUnit="hours";
        info.frequency.every.count=1;
        Optional<EndDeviceGroup> endDeviceGroupOptional = Optional.of(mock(EndDeviceGroup.class));
        when(meteringGroupService.findEndDeviceGroup(102L)).thenReturn(endDeviceGroupOptional);
        DataCollectionKpiService.DataCollectionKpiBuilder kpiBuilder = mock(DataCollectionKpiService.DataCollectionKpiBuilder.class);
        DataCollectionKpiService.KpiTargetBuilder connectionKpiTargetBuilder = mock(DataCollectionKpiService.KpiTargetBuilder.class);
        DataCollectionKpiService.KpiTargetBuilder communicationKpiTargetBuilder = mock(DataCollectionKpiService.KpiTargetBuilder.class);
        when(kpiBuilder.calculateComTaskExecutionKpi(anyObject())).thenReturn(communicationKpiTargetBuilder);
        when(kpiBuilder.calculateConnectionSetupKpi(anyObject())).thenReturn(connectionKpiTargetBuilder);
        long kpiId = 71L;
        DataCollectionKpi kpiMock = mockKpi(kpiId, mockDeviceGroup("end device group bis", 2));
        when(kpiBuilder.save()).thenReturn(kpiMock);
        when(dataCollectionKpiService.newDataCollectionKpi(endDeviceGroupOptional.get())).thenReturn(kpiBuilder);

        Response response = target("/kpis/").request().post(Entity.json(info));

        assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());
        verify(dataCollectionKpiService).newDataCollectionKpi(endDeviceGroupOptional.get());
        verify(kpiBuilder).calculateConnectionSetupKpi(Duration.ofHours(1));
        verify(kpiBuilder, never()).calculateComTaskExecutionKpi(anyObject());
        verify(connectionKpiTargetBuilder).expectingAsMaximum(BigDecimal.valueOf(99.1));
        verify(communicationKpiTargetBuilder, never()).expectingAsMaximum(anyObject());
        verify(kpiBuilder).save();
    }

    @Test
    public void testUpdateKpiWithConnectionKpi() throws Exception {
        DataCollectionKpiInfo info = new DataCollectionKpiInfo();
        info.connectionTarget=BigDecimal.valueOf(99.1);
        info.deviceGroup=new LongIdWithNameInfo(102L, "some group");
        info.frequency = new TemporalExpressionInfo();
        info.frequency.every = new TimeDurationInfo();
        info.frequency.every.timeUnit="minutes";
        info.frequency.every.count=15;
        long kpiId = 71L;
        DataCollectionKpi kpiMock = mockKpi(kpiId, mockDeviceGroup("end device group bis", 2),Duration.ofMinutes(15), null, BigDecimal.valueOf(50.0));
        when(dataCollectionKpiService.findDataCollectionKpi(kpiId)).thenReturn(Optional.of(kpiMock));

        Response response = target("/kpis/71").request().put(Entity.json(info));

        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(kpiMock).calculateConnectionKpi(BigDecimal.valueOf(99.1));
        verify(kpiMock, never()).calculateComTaskExecutionKpi(anyObject());
    }

    @Test
    public void testUpdateKpiWithCommunicationKpiAndRepeatExistingConnectionKpi() throws Exception {
        DataCollectionKpiInfo info = new DataCollectionKpiInfo();
        info.communicationTarget=BigDecimal.valueOf(99.1);
        info.connectionTarget=BigDecimal.valueOf(50.0);
        info.deviceGroup=new LongIdWithNameInfo(102L, "some group");
        info.frequency = new TemporalExpressionInfo();
        info.frequency.every = new TimeDurationInfo();
        info.frequency.every.timeUnit="minutes";
        info.frequency.every.count=15;
        long kpiId = 71L;
        DataCollectionKpi kpiMock = mockKpi(kpiId, mockDeviceGroup("end device group bis", 2), Duration.ofMinutes(15), BigDecimal.valueOf(50.0), null);
        when(dataCollectionKpiService.findDataCollectionKpi(kpiId)).thenReturn(Optional.of(kpiMock));

        Response response = target("/kpis/71").request().put(Entity.json(info));

        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(kpiMock).calculateComTaskExecutionKpi(BigDecimal.valueOf(99.1));
        verify(kpiMock, never()).calculateConnectionKpi(anyObject());
    }

    @Test
    public void testUpdateKpiWithCommunicationKpi() throws Exception {
        DataCollectionKpiInfo info = new DataCollectionKpiInfo();
        info.communicationTarget=BigDecimal.valueOf(99.1);
        info.deviceGroup=new LongIdWithNameInfo(102L, "some group");
        info.frequency = new TemporalExpressionInfo();
        info.frequency.every = new TimeDurationInfo();
        info.frequency.every.timeUnit="minutes";
        info.frequency.every.count=15;
        long kpiId = 71L;
        DataCollectionKpi kpiMock = mockKpi(kpiId, mockDeviceGroup("end device group bis", 2), Duration.ofMinutes(15), BigDecimal.valueOf(50.0), null);
        when(dataCollectionKpiService.findDataCollectionKpi(kpiId)).thenReturn(Optional.of(kpiMock));

        Response response = target("/kpis/71").request().put(Entity.json(info));

        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(kpiMock).calculateComTaskExecutionKpi(BigDecimal.valueOf(99.1));
        verify(kpiMock, never()).calculateConnectionKpi(anyObject());
    }

    private DataCollectionKpi mockKpi(long kpiId, QueryEndDeviceGroup endDeviceGroup) {
        DataCollectionKpi kpiMock = mock(DataCollectionKpi.class);
        when(kpiMock.getDeviceGroup()).thenReturn(endDeviceGroup);
        when(kpiMock.getId()).thenReturn(kpiId);
        when(kpiMock.comTaskExecutionKpiCalculationIntervalLength()).thenReturn(Optional.<TemporalAmount>empty());
        when(kpiMock.connectionSetupKpiCalculationIntervalLength()).thenReturn(Optional.of(Duration.ofMinutes(15)));
        when(kpiMock.getStaticCommunicationKpiTarget()).thenReturn(Optional.of(BigDecimal.valueOf(15.8)));
        when(kpiMock.getStaticConnectionKpiTarget()).thenReturn(Optional.empty());
        when(kpiMock.getLatestCalculation()).thenReturn(Optional.of(Instant.ofEpochMilli(10000000L)));
        return kpiMock;
    }

    private DataCollectionKpi mockKpi(long kpiId, QueryEndDeviceGroup endDeviceGroup, TemporalAmount frequency, BigDecimal staticConnectionTarget, BigDecimal staticCommunicationTarget) {
        DataCollectionKpi kpiMock = mock(DataCollectionKpi.class);
        when(kpiMock.getDeviceGroup()).thenReturn(endDeviceGroup);
        when(kpiMock.getId()).thenReturn(kpiId);
        if (staticCommunicationTarget!=null) {
            when(kpiMock.comTaskExecutionKpiCalculationIntervalLength()).thenReturn(Optional.ofNullable(frequency));
            when(kpiMock.getStaticCommunicationKpiTarget()).thenReturn(Optional.of(staticCommunicationTarget));
            when(kpiMock.calculatesComTaskExecutionKpi()).thenReturn(true);
        } else {
            when(kpiMock.comTaskExecutionKpiCalculationIntervalLength()).thenReturn(Optional.empty());
            when(kpiMock.getStaticCommunicationKpiTarget()).thenReturn(Optional.empty());
            when(kpiMock.calculatesComTaskExecutionKpi()).thenReturn(false);
        }
        if (staticConnectionTarget!=null) {
            when(kpiMock.connectionSetupKpiCalculationIntervalLength()).thenReturn(Optional.ofNullable(frequency));
            when(kpiMock.getStaticConnectionKpiTarget()).thenReturn(Optional.of(staticConnectionTarget));
            when(kpiMock.calculatesConnectionSetupKpi()).thenReturn(true);
        } else {
            when(kpiMock.connectionSetupKpiCalculationIntervalLength()).thenReturn(Optional.empty());
            when(kpiMock.getStaticConnectionKpiTarget()).thenReturn(Optional.empty());
            when(kpiMock.calculatesConnectionSetupKpi()).thenReturn(false);
        }
        when(kpiMock.getLatestCalculation()).thenReturn(Optional.of(Instant.ofEpochMilli(10000000L)));
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

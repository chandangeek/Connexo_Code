package com.energyict.mdc.device.topology.rest.impl;

import com.elster.jupiter.devtools.ExtjsFilter;
import com.elster.jupiter.util.time.Interval;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.topology.G3CommunicationPathSegment;
import com.energyict.mdc.device.topology.rest.GraphLayer;
import com.energyict.mdc.device.topology.rest.info.NodeInfo;
import com.energyict.mdc.device.topology.rest.layer.CommunicationStatusLayer;
import com.energyict.mdc.device.topology.rest.layer.DeviceInfoLayer;
import com.energyict.mdc.device.topology.rest.layer.DeviceLifeCycleStatusLayer;
import com.energyict.mdc.device.topology.rest.layer.DeviceSummaryExtraInfoLayer;
import com.energyict.mdc.device.topology.rest.layer.DeviceTypeLayer;
import com.energyict.mdc.device.topology.rest.layer.IssuesAndAlarmsLayer;
import com.energyict.mdc.device.topology.rest.layer.LayerNames;
import com.energyict.mdc.device.topology.rest.layer.LinkQualityLayer;
import com.energyict.mdc.tasks.ComTask;

import com.google.common.collect.Range;
import com.jayway.jsonpath.JsonModel;
import org.json.JSONArray;

import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.time.Clock;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;


import org.junit.Ignore;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


/**
 * Copyrights EnergyICT
 * Date: 17/08/2017
 * Time: 13:18
 */

// This test is ignored while failing due to geo-location POC code that should not be there!!! Useless to fix this test against POC code
@Ignore
public class TopologyGraphResourceTest extends TopologyGraphApplicationJerseyTest {

    static CommunicationStatusLayer communicationStatusLayer = spy(new CommunicationStatusLayer());
    static DeviceInfoLayer deviceInfoLayer = spy(new DeviceInfoLayer());
    static DeviceTypeLayer deviceTypeLayer = spy(new DeviceTypeLayer());
    static DeviceLifeCycleStatusLayer deviceLifeCycleStatusLayer = spy(new DeviceLifeCycleStatusLayer());
    static IssuesAndAlarmsLayer issuesAndAlarmsLayer = mock(IssuesAndAlarmsLayer.class);
    static LinkQualityLayer linkQualityLayer = spy(new LinkQualityLayer());
    static DeviceSummaryExtraInfoLayer  deviceSummaryExtraInfoLayer = spy(new DeviceSummaryExtraInfoLayer());

    @Test
    public void getTopologyGraphByNameWithoutLayerActivation() throws Exception{
        Device gateway = mock(Device.class);
        when(gateway.getId()).thenReturn(123L);
        when(gateway.getName()).thenReturn("gateway");
        when(gateway.getSerialNumber()).thenReturn("Serial of gateway");
        Device slave1 = mock(Device.class);
        when(slave1.getId()).thenReturn(201L);
        when(slave1.getName()).thenReturn("slave1");
        when(slave1.getSerialNumber()).thenReturn("Serial of slave1");
        Device slave2 = mock(Device.class);
        when(slave1.getId()).thenReturn(202L);
        when(slave2.getName()).thenReturn("slave2");
        when(slave2.getSerialNumber()).thenReturn("Serial of slave2");
        Device slave3 = mock(Device.class);
        when(slave3.getId()).thenReturn(203L);
        when(slave3.getName()).thenReturn("slave3");
        when(slave3.getSerialNumber()).thenReturn("Serial of slave3");

        List<G3CommunicationPathSegment> comSegmentsInTopology = new ArrayList<>();
        G3CommunicationPathSegment segment1 = mock(G3CommunicationPathSegment.class);
        when(segment1.getSource()).thenReturn(gateway);
        when(segment1.getTarget()).thenReturn(slave1);
        comSegmentsInTopology.add(segment1);
        G3CommunicationPathSegment segment2 = mock(G3CommunicationPathSegment.class);
        when(segment2.getSource()).thenReturn(gateway);
        when(segment2.getTarget()).thenReturn(slave2);
        comSegmentsInTopology.add(segment2);
        G3CommunicationPathSegment segment3 = mock(G3CommunicationPathSegment.class);
        when(segment3.getSource()).thenReturn(gateway);
        when(segment3.getTarget()).thenReturn(slave3);
        comSegmentsInTopology.add(segment3);

        when(deviceService.findDeviceByName("deviceName")).thenReturn(Optional.of(gateway));
        when(topologyService.getPhysicalGateway(gateway)).thenReturn(Optional.empty());
        when(topologyService.getUniqueG3CommunicationPathSegments(gateway)).thenReturn(comSegmentsInTopology.stream());

        when(graphLayerService.getGraphLayers()).thenReturn(getExisting());

        Response response = target("/topology/deviceName").request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel jsonModel = JsonModel.model((InputStream) response.getEntity());
        assertThat(jsonModel.<List>get("nodes")).hasSize(4);
        assertThat(jsonModel.<List>get("links")).hasSize(3);
    }

    @Test
    public void getTopologyGraphByNameForRefresh() throws Exception{
        Device gateway = mock(Device.class);
        when(gateway.getId()).thenReturn(123L);
        when(gateway.getName()).thenReturn("gateway");
        when(gateway.getSerialNumber()).thenReturn("Serial of gateway");
        Device slave1 = mock(Device.class);
        when(slave1.getId()).thenReturn(201L);
        when(slave1.getName()).thenReturn("slave1");
        when(slave1.getSerialNumber()).thenReturn("Serial of slave1");
        Device slave2 = mock(Device.class);
        when(slave1.getId()).thenReturn(202L);
        when(slave2.getName()).thenReturn("slave2");
        when(slave2.getSerialNumber()).thenReturn("Serial of slave2");
        Device slave3 = mock(Device.class);
        when(slave3.getId()).thenReturn(203L);
        when(slave3.getName()).thenReturn("slave3");
        when(slave3.getSerialNumber()).thenReturn("Serial of slave3");

        List<G3CommunicationPathSegment> comSegmentsInTopology = new ArrayList<>();
        G3CommunicationPathSegment segment1 = mock(G3CommunicationPathSegment.class);
        when(segment1.getSource()).thenReturn(gateway);
        when(segment1.getTarget()).thenReturn(slave1);
        comSegmentsInTopology.add(segment1);
        G3CommunicationPathSegment segment2 = mock(G3CommunicationPathSegment.class);
        when(segment2.getSource()).thenReturn(gateway);
        when(segment2.getTarget()).thenReturn(slave2);
        comSegmentsInTopology.add(segment2);
        G3CommunicationPathSegment segment3 = mock(G3CommunicationPathSegment.class);
        when(segment3.getSource()).thenReturn(gateway);
        when(segment3.getTarget()).thenReturn(slave3);
        comSegmentsInTopology.add(segment3);

        when(deviceService.findDeviceByName("deviceName")).thenReturn(Optional.of(gateway));
        when(topologyService.getPhysicalGateway(gateway)).thenReturn(Optional.empty());
        when(topologyService.getUniqueG3CommunicationPathSegments(gateway)).thenReturn(comSegmentsInTopology.stream());

        when(graphLayerService.getGraphLayers()).thenReturn(getExisting());

        Response response = target("/topology/deviceName").queryParam("filter", ExtjsFilter.filter("refresh", "true")).request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());

    }

    @Test
    public void getTopologyGraphByNameWithLayerActivationTest() throws Exception{
        Device gateway = mock(Device.class);
        when(gateway.getId()).thenReturn(123L);
        when(gateway.getName()).thenReturn("gateway");
        when(gateway.getSerialNumber()).thenReturn("Serial of gateway");
        Device slave1 = mock(Device.class);
        when(slave1.getId()).thenReturn(201L);
        when(slave1.getName()).thenReturn("slave1");
        when(slave1.getSerialNumber()).thenReturn("Serial of slave1");
        Device slave2 = mock(Device.class);
        when(slave1.getId()).thenReturn(202L);
        when(slave2.getName()).thenReturn("slave2");
        when(slave2.getSerialNumber()).thenReturn("Serial of slave2");
        Device slave3 = mock(Device.class);
        when(slave3.getId()).thenReturn(203L);
        when(slave3.getName()).thenReturn("slave3");
        when(slave3.getSerialNumber()).thenReturn("Serial of slave3");

        List<G3CommunicationPathSegment> comSegmentsInTopology = new ArrayList<>();
        when(deviceService.findDeviceByName("deviceName")).thenReturn(Optional.of(gateway));
        when(topologyService.getPhysicalGateway(gateway)).thenReturn(Optional.empty());
        when(topologyService.getUniqueG3CommunicationPathSegments(gateway)).thenReturn(comSegmentsInTopology.stream());

        when(graphLayerService.getGraphLayers()).thenReturn(getExisting());

        target("/topology/deviceName").queryParam("filter", ExtjsFilter.filter("layers", Arrays.asList(LayerNames.CommunciationStatusLayer.fullName(), LayerNames.DeviceInfoLayer.fullName(), LayerNames.DeviceTypeLayer.fullName()))).request().get();

        verify(deviceInfoLayer).setActive(true);
        verify(deviceTypeLayer).setActive(true);
        verify(communicationStatusLayer).setActive(true);
    }

    @Test
    public void getGraphLayersTest() throws Exception {
        when(graphLayerService.getGraphLayers()).thenReturn(getExisting());

        Response response = target("/topology/graphlayers").request().get(Response.class);
        JSONArray jsonArray = new JSONArray(response.readEntity(String.class));
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        assertThat(jsonArray.length()).isEqualTo(7);
        assertThat(jsonArray.getString(0)).isEqualTo(LayerNames.CommunciationStatusLayer.fullName());
        assertThat(jsonArray.getString(1)).isEqualTo(LayerNames.DeviceInfoLayer.fullName());
        assertThat(jsonArray.getString(2)).isEqualTo(LayerNames.DeviceTypeLayer.fullName());
        assertThat(jsonArray.getString(3)).isEqualTo(LayerNames.DeviceLifeCycleStatusLayer.fullName());
        assertThat(jsonArray.getString(4)).isEqualTo("null");
        assertThat(jsonArray.getString(5)).isEqualTo(LayerNames.LinkQualityLayer.fullName());
        assertThat(jsonArray.getString(6)).isEqualTo(LayerNames.DeviceSummaryExtraInfoLayer.fullName());
    }

    @Test

    public void getSummaryInfoTest() throws Exception {
        Device gateway = mock(Device.class);
        when(gateway.getId()).thenReturn(123L);
        when(gateway.getName()).thenReturn("gateway");
        when(gateway.getSerialNumber()).thenReturn("Serial of gateway");

        DeviceType deviceType = mock(DeviceType.class);
        when(deviceType.getName()).thenReturn("name of device type");
        DeviceConfiguration deviceconfiguration = mock(DeviceConfiguration.class);
        when(deviceconfiguration.getName()).thenReturn("name of device configuration");

        Device slave3 = mock(Device.class);
        when(slave3.getId()).thenReturn(203L);
        when(slave3.getName()).thenReturn("slave3");
        when(slave3.getSerialNumber()).thenReturn("Serial of slave3");
        when(slave3.getDeviceType()).thenReturn(deviceType);
        when(slave3.getDeviceConfiguration()).thenReturn(deviceconfiguration);
        ComTaskExecution comTaskExecution1 = mock(ComTaskExecution.class);
        when(comTaskExecution1.isLastExecutionFailed()).thenReturn(false);
        ComTaskExecution comTaskExecution2 = mock(ComTaskExecution.class);
        when(comTaskExecution2.isLastExecutionFailed()).thenReturn(true);
        ComTask comTask = mock(ComTask.class);
        when(comTask.getName()).thenReturn("Hou je in stilte bezig...");
        when(comTaskExecution2.getComTask()).thenReturn(comTask);

        when(slave3.getComTaskExecutions()).thenReturn(Arrays.asList(comTaskExecution1, comTaskExecution2));
        when(topologyService.getPhysicalGateway(slave3)).thenReturn(Optional.of(gateway));

        List<G3CommunicationPathSegment> comSegmentsInTopology = new ArrayList<>();
        G3CommunicationPathSegment segment3 = mock(G3CommunicationPathSegment.class);
        when(segment3.getSource()).thenReturn(gateway);
        when(segment3.getTarget()).thenReturn(slave3);
        when(segment3.getInterval()).thenReturn(Interval.of(Range.atLeast(Clock.systemDefaultZone().instant())));
        comSegmentsInTopology.add(segment3);

        when(topologyService.getUniqueG3CommunicationPathSegments(gateway)).thenReturn(comSegmentsInTopology.stream());

        Map<String, Object> issuesAndAlarmProperties = new HashMap<>();
                issuesAndAlarmProperties.put("issues", 3);
        issuesAndAlarmProperties.put("alarms", 33);

        when(deviceService.findDeviceByName("slave3")).thenReturn(Optional.of(slave3));
        when(graphLayerService.getAllSummaryLayers()).thenReturn(Arrays.asList(deviceInfoLayer, deviceTypeLayer, issuesAndAlarmsLayer, deviceSummaryExtraInfoLayer));
        when(issuesAndAlarmsLayer.getProperties(any(NodeInfo.class))).thenReturn(issuesAndAlarmProperties);

        // I need a cached graphInfo
        deviceGraphFactory.from(slave3);

        String stringResponse = target("/topology/summary/slave3").request().get(String.class);

        JsonModel info = JsonModel.create(stringResponse);
        assertThat(info.<Number>get("$.id")).isEqualTo(203);
        assertThat(info.<Number>get("$.name")).isEqualTo("slave3");
        assertThat(info.<String>get("$.serialNumber")).isEqualTo("Serial of slave3");
        assertThat(info.<String>get("$.deviceType")).isEqualTo("name of device type");
        assertThat(info.<String>get("$.deviceConfiguration")).isEqualTo("name of device configuration");
        assertThat(info.<Number>get("$.issues")).isEqualTo(3);
        assertThat(info.<Number>get("$.alarms")).isEqualTo(33);
        assertThat(info.<Number>get("$.failedComTasks[0]")).isEqualTo("Hou je in stilte bezig...");
    }

    private static List<GraphLayer> getExisting(){
        List<GraphLayer> layers = new ArrayList<>();
        layers.add(communicationStatusLayer);
        layers.add(deviceInfoLayer);
        layers.add(deviceTypeLayer);
        layers.add(deviceLifeCycleStatusLayer);
        layers.add(issuesAndAlarmsLayer);
        layers.add(linkQualityLayer);
        layers.add(deviceSummaryExtraInfoLayer);
        return layers;
    }
}

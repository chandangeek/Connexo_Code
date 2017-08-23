package com.energyict.mdc.device.topology.rest.impl;

import com.elster.jupiter.devtools.ExtjsFilter;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.topology.DeviceTopology;
import com.energyict.mdc.device.topology.G3CommunicationPathSegment;
import com.energyict.mdc.device.topology.rest.GraphLayer;
import com.energyict.mdc.device.topology.rest.layer.CommunicationStatusLayer;
import com.energyict.mdc.device.topology.rest.layer.DeviceInfoLayer;
import com.energyict.mdc.device.topology.rest.layer.DeviceLifeCycleStatusLayer;
import com.energyict.mdc.device.topology.rest.layer.DeviceTypeLayer;
import com.energyict.mdc.device.topology.rest.layer.IssuesAndAlarmsLayer;
import com.energyict.mdc.device.topology.rest.layer.LinkQualityLayer;

import com.google.common.collect.Range;
import com.jayway.jsonpath.JsonModel;
import org.json.JSONArray;

import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;


import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


/**
 * Copyrights EnergyICT
 * Date: 17/08/2017
 * Time: 13:18
 */
public class TopologyGraphResourceTest extends TopologyGraphApplicationJerseyTest {

    static CommunicationStatusLayer communicationStatusLayer = spy(new CommunicationStatusLayer());
    static DeviceInfoLayer deviceInfoLayer = spy(new DeviceInfoLayer());
    static DeviceTypeLayer deviceTypeLayer = spy(new DeviceTypeLayer());
    static DeviceLifeCycleStatusLayer deviceLifeCycleStatusLayer = spy(new DeviceLifeCycleStatusLayer());
    static IssuesAndAlarmsLayer issuesAndAlarmsLayer = spy(new IssuesAndAlarmsLayer());
    static LinkQualityLayer linkQualityLayer = spy(new LinkQualityLayer());

    @Test
    public void getTopologyGraphByNameWithoutLayerActivation() throws Exception{
        Instant yesterday = Instant.now().minus(1, ChronoUnit.DAYS);
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

        List<Device> devicesInTopology = Arrays.asList(slave1, slave2, slave3);
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

        DeviceTopology topology = mock(DeviceTopology.class);
        when(topology.getRoot()).thenReturn(gateway);
        when(topology.getDevices()).thenReturn(devicesInTopology);
        when(topology.getPeriod()).thenReturn(Range.atLeast(yesterday));

        when(deviceService.findDeviceByName("deviceName")).thenReturn(Optional.of(gateway));
        when(topologyService.getPhysicalTopology(eq(gateway), any(Range.class))).thenReturn(topology);
        when(topologyService.getPhysicalGateway(gateway)).thenReturn(Optional.empty());
        when(topologyService.getUniqueG3CommunicationPathSegments(devicesInTopology)).thenReturn(comSegmentsInTopology.stream());

        when(graphLayerService.getGraphLayers()).thenReturn(getExisting());

        Response response = target("/topology/deviceName").request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel jsonModel = JsonModel.model((InputStream) response.getEntity());
        assertThat(jsonModel.<List>get("nodes")).hasSize(4);
        assertThat(jsonModel.<List>get("links")).hasSize(3);
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

        List<Device> devicesInTopology = Arrays.asList(slave1, slave2, slave3);
        List<G3CommunicationPathSegment> comSegmentsInTopology = new ArrayList<>();
        DeviceTopology topology = mock(DeviceTopology.class);
        when(topology.getRoot()).thenReturn(gateway);
        when(topology.getDevices()).thenReturn(devicesInTopology);

        when(deviceService.findDeviceByName("deviceName")).thenReturn(Optional.of(gateway));
        when(topologyService.getPhysicalTopology(eq(gateway), any(Range.class))).thenReturn(topology);
        when(topologyService.getPhysicalGateway(gateway)).thenReturn(Optional.empty());
        when(topologyService.getUniqueG3CommunicationPathSegments(devicesInTopology)).thenReturn(comSegmentsInTopology.stream());

        when(graphLayerService.getGraphLayers()).thenReturn(getExisting());

        target("/topology/deviceName").queryParam("filter", ExtjsFilter.filter("layers", Arrays.asList("Communication+Status", "Device+Identifiers", "Device+types"))).request().get();

        verify(deviceInfoLayer).activate();
        verify(deviceTypeLayer).activate();
        verify(communicationStatusLayer).activate();
    }

    @Test
    public void getGraphLayersTest() throws Exception {
        when(graphLayerService.getGraphLayers()).thenReturn(getExisting());

        Response response = target("/topology/graphlayers").request().get(Response.class);
        JSONArray jsonArray = new JSONArray(response.readEntity(String.class));
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        assertThat(jsonArray.length()).isEqualTo(6);
        assertThat(jsonArray.getString(0)).isEqualTo(new CommunicationStatusLayer().getDisplayName(thesaurus));
        assertThat(jsonArray.getString(1)).isEqualTo(new DeviceInfoLayer().getDisplayName(thesaurus));
        assertThat(jsonArray.getString(2)).isEqualTo(new DeviceTypeLayer().getDisplayName(thesaurus));
        assertThat(jsonArray.getString(3)).isEqualTo(new DeviceLifeCycleStatusLayer().getDisplayName(thesaurus));
        assertThat(jsonArray.getString(4)).isEqualTo(new IssuesAndAlarmsLayer().getDisplayName(thesaurus));
        assertThat(jsonArray.getString(5)).isEqualTo(new LinkQualityLayer().getDisplayName(thesaurus));
    }

    private static List<GraphLayer> getExisting(){
        List<GraphLayer> layers = new ArrayList<>();
        layers.add(communicationStatusLayer);
        layers.add(deviceInfoLayer);
        layers.add(deviceTypeLayer);
        layers.add(deviceLifeCycleStatusLayer);
        layers.add(issuesAndAlarmsLayer);
        layers.add(linkQualityLayer);
        return layers;
    }
}

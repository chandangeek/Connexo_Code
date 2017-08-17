package com.energyict.mdc.device.topology.rest.impl;

import com.energyict.mdc.device.topology.rest.GraphLayer;
import com.energyict.mdc.device.topology.rest.GraphLayerService;
import com.energyict.mdc.device.topology.rest.layer.CommunicationStatusLayer;
import com.energyict.mdc.device.topology.rest.layer.DeviceInfoLayer;
import com.energyict.mdc.device.topology.rest.layer.DeviceLifeCycleStatusLayer;
import com.energyict.mdc.device.topology.rest.layer.DeviceTypeLayer;
import com.energyict.mdc.device.topology.rest.layer.IssuesAndAlarmsLayer;
import com.energyict.mdc.device.topology.rest.layer.LinkQualityLayer;

import com.jayway.jsonpath.JsonModel;
import org.json.JSONArray;
import org.json.JSONString;

import javax.naming.StringRefAddr;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;

/**
 * Copyrights EnergyICT
 * Date: 17/08/2017
 * Time: 13:18
 */
public class TopologyGraphResourceTest extends TopologyGraphApplicationJerseyTest {

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

    @Test
    public void activateGraphLayersTest() throws Exception {
        List<GraphLayer> layers = getExisting();
        when(graphLayerService.getGraphLayers()).thenReturn(layers);

        String[] layerNames= new String[] {layers.get(2).getDisplayName(thesaurus),
                                     layers.get(3).getDisplayName(thesaurus),
                                     layers.get(4).getDisplayName(thesaurus),
                                     layers.get(5).getDisplayName(thesaurus)};

        Response response = target("/topology/graphlayers").request().put(Entity.json(layerNames));

        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(layers.get(2)).activate();
        verify(layers.get(3)).activate();
        verify(layers.get(4)).activate();
        verify(layers.get(5)).activate();
    }

    private static List<GraphLayer> getExisting(){
        List<GraphLayer> layers = new ArrayList<>();
        layers.add(spy(new CommunicationStatusLayer()));
        layers.add(spy(new DeviceInfoLayer()));
        layers.add(spy(new DeviceTypeLayer()));
        layers.add(spy(new DeviceLifeCycleStatusLayer()));
        layers.add(spy(new IssuesAndAlarmsLayer()));
        layers.add(spy(new LinkQualityLayer()));
        return layers;
    }
}

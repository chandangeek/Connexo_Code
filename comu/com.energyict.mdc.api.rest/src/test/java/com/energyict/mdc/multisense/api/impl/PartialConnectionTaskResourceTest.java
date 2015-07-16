package com.energyict.mdc.multisense.api.impl;

import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.PartialConnectionTask;
import com.jayway.jsonpath.JsonModel;
import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.List;
import javax.ws.rs.core.Response;
import net.minidev.json.JSONArray;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Created by bvn on 7/16/15.
 */
public class PartialConnectionTaskResourceTest extends MultisensePublicApiJerseyTest {

    @Before
    public void setup() {
        DeviceType deviceType = mockDeviceType(112L, "device type");
        DeviceConfiguration deviceConfiguration = mockDeviceConfiguration(113L, "Default configuration", deviceType);
        when(deviceType.getConfigurations()).thenReturn(Arrays.asList(deviceConfiguration));
        PartialConnectionTask partialConnectionTask1 = mockPartialInboundConnectionTask(114L, "partial conn task 114", deviceConfiguration);
        PartialConnectionTask partialConnectionTask2 = mockPartialInboundConnectionTask(124L, "partial conn task 124", deviceConfiguration);
        PartialConnectionTask partialConnectionTask3 = mockPartialOutboundConnectionTask(134L, "partial conn task 134", deviceConfiguration);
        when(deviceConfiguration.getPartialConnectionTasks()).thenReturn(Arrays.asList(partialConnectionTask1, partialConnectionTask2, partialConnectionTask3));
    }

    @Test
    public void testPartialConnectionTaskFields() throws Exception {
        Response response = target("/devicetypes/112/deviceconfigurations/113/connectionmethods/fields").request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel jsonModel = JsonModel.create((ByteArrayInputStream) response.getEntity());
        assertThat(jsonModel.<JSONArray>get("$")).containsOnly("id", "name", "link", "direction", "comWindow", "rescheduleRetryDelay", "nextExecutionSpecs",
                "connectionType", "comPortPool", "isDefault", "connectionStrategy", "allowSimultaneousConnections", "properties");
    }

    @Test
    public void testGetPartialConnectionTasks() throws Exception {
        Response response = target("/devicetypes/112/deviceconfigurations/113/connectionmethods").queryParam("start", 0).queryParam("limit", 5).request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel jsonModel = JsonModel.create((ByteArrayInputStream) response.getEntity());
        assertThat(jsonModel.<List>get("$.link")).hasSize(1);
        assertThat(jsonModel.<String>get("$.link[0].params.rel")).isEqualTo("current");
        assertThat(jsonModel.<String>get("$.link[0].href")).isEqualTo("http://localhost:9998/devicetypes/112/deviceconfigurations/113/connectionmethods?start=0&limit=5");
        assertThat(jsonModel.<List>get("$.data")).hasSize(3);
        assertThat(jsonModel.<Integer>get("$.data[0].id")).isEqualTo(114);
        assertThat(jsonModel.<String>get("$.data[0].name")).isEqualTo("partial conn task 114");
        assertThat(jsonModel.<String>get("$.data[0].link.params.rel")).isEqualTo("self");
        assertThat(jsonModel.<String>get("$.data[0].link.href")).isEqualTo("http://localhost:9998/devicetypes/112/deviceconfigurations/113/connectionmethods/114");
        assertThat(jsonModel.<Integer>get("$.data[2].id")).isEqualTo(134);
        assertThat(jsonModel.<String>get("$.data[2].name")).isEqualTo("partial conn task 134");
        assertThat(jsonModel.<String>get("$.data[2].link.params.rel")).isEqualTo("self");
        assertThat(jsonModel.<String>get("$.data[2].link.href")).isEqualTo("http://localhost:9998/devicetypes/112/deviceconfigurations/113/connectionmethods/134");
    }

    @Test
    public void testGetPartialInboundConnectionTask() throws Exception {
        Response response = target("/devicetypes/112/deviceconfigurations/113/connectionmethods/124").request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel jsonModel = JsonModel.create((ByteArrayInputStream) response.getEntity());
        assertThat(jsonModel.<Integer>get("$.id")).isEqualTo(124);
        assertThat(jsonModel.<String>get("$.name")).isEqualTo("partial conn task 124");
        assertThat(jsonModel.<String>get("$.link.params.rel")).isEqualTo("self");
        assertThat(jsonModel.<String>get("$.link.href")).isEqualTo("http://localhost:9998/devicetypes/112/deviceconfigurations/113/connectionmethods/124");
        assertThat(jsonModel.<String>get("$.direction")).isEqualTo("Inbound");
        assertThat(jsonModel.<String>get("$.connectionType")).isEqualTo("inbound pluggeable class");
        assertThat(jsonModel.<Integer>get("$.comPortPool.id")).isEqualTo(65);
        assertThat(jsonModel.<String>get("$.comPortPool.link.href")).isEqualTo("http://localhost:9998/comportpools/65");
        assertThat(jsonModel.<String>get("$.comPortPool.link.params.rel")).isEqualTo("related");
        assertThat(jsonModel.<Boolean>get("$.isDefault")).isEqualTo(false);
        assertThat(jsonModel.<List>get("$.properties")).hasSize(1);
        assertThat(jsonModel.<String>get("$.properties[0].key")).isEqualTo("string.property");
        assertThat(jsonModel.<String>get("$.properties[0].propertyValueInfo.defaultValue")).isEqualTo("default");
        assertThat(jsonModel.<String>get("$.properties[0].propertyTypeInfo.simplePropertyType")).isEqualTo("TEXT");
        assertThat(jsonModel.<Boolean>get("$.properties[0].required")).isEqualTo(true);

    }

    @Test
    public void testGetPartialOutboundConnectionTask() throws Exception {
        Response response = target("/devicetypes/112/deviceconfigurations/113/connectionmethods/134").request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel jsonModel = JsonModel.create((ByteArrayInputStream) response.getEntity());
        assertThat(jsonModel.<Integer>get("$.id")).isEqualTo(134);
        assertThat(jsonModel.<String>get("$.name")).isEqualTo("partial conn task 134");
        assertThat(jsonModel.<String>get("$.link.params.rel")).isEqualTo("self");
        assertThat(jsonModel.<String>get("$.link.href")).isEqualTo("http://localhost:9998/devicetypes/112/deviceconfigurations/113/connectionmethods/134");
        assertThat(jsonModel.<String>get("$.direction")).isEqualTo("Outbound");
        assertThat(jsonModel.<String>get("$.connectionType")).isEqualTo("outbound pluggeable class");
        assertThat(jsonModel.<Integer>get("$.comPortPool.id")).isEqualTo(165);
        assertThat(jsonModel.<String>get("$.comPortPool.link.href")).isEqualTo("http://localhost:9998/comportpools/165");
        assertThat(jsonModel.<String>get("$.comPortPool.link.params.rel")).isEqualTo("related");
        assertThat(jsonModel.<Boolean>get("$.isDefault")).isEqualTo(false);
        assertThat(jsonModel.<List>get("$.properties")).hasSize(1);
        assertThat(jsonModel.<String>get("$.properties[0].key")).isEqualTo("string.property");
        assertThat(jsonModel.<String>get("$.properties[0].propertyValueInfo.defaultValue")).isEqualTo("default");
        assertThat(jsonModel.<String>get("$.properties[0].propertyTypeInfo.simplePropertyType")).isEqualTo("TEXT");
        assertThat(jsonModel.<Boolean>get("$.properties[0].required")).isEqualTo(true);
        assertThat(jsonModel.<Integer>get("$.comWindow.start")).isEqualTo(7200000);
        assertThat(jsonModel.<Integer>get("$.comWindow.end")).isEqualTo(14400000);
        assertThat(jsonModel.<Boolean>get("$.allowSimultaneousConnections")).isEqualTo(true);
        assertThat(jsonModel.<Integer>get("$.rescheduleRetryDelay.count")).isEqualTo(60);
        assertThat(jsonModel.<String>get("$.rescheduleRetryDelay.timeUnit")).isEqualTo("minutes");
    }

    @Test
    public void testGetPartialConnectionTaskWithFieldSelection() throws Exception {
        Response response = target("/devicetypes/112/deviceconfigurations/113/connectionmethods/124").queryParam("fields", "name").request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel jsonModel = JsonModel.create((ByteArrayInputStream) response.getEntity());
        assertThat(jsonModel.<Integer>get("$.id")).isNull();
        assertThat(jsonModel.<Integer>get("$.link")).isNull();
        assertThat(jsonModel.<String>get("$.name")).isEqualTo("partial conn task 124");
        assertThat(jsonModel.<String>get("$.direction")).isNull();
    }

}

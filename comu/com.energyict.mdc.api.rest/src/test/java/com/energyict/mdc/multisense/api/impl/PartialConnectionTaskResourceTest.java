package com.energyict.mdc.multisense.api.impl;

import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.PartialConnectionTask;
import com.energyict.mdc.device.config.PartialInboundConnectionTask;
import com.jayway.jsonpath.JsonModel;
import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.List;
import javax.ws.rs.core.Response;
import net.minidev.json.JSONArray;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
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
        when(deviceConfiguration.getPartialConnectionTasks()).thenReturn(Arrays.asList(partialConnectionTask1, partialConnectionTask2));
    }

    @Test
    public void testPartialConnectionTaskFields() throws Exception {
        Response response = target("/devicetypes/112/deviceconfigurations/113/connectionmethods/fields").request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel jsonModel = JsonModel.create((ByteArrayInputStream) response.getEntity());
        assertThat(jsonModel.<JSONArray>get("$")).containsOnly("id", "name", "link", "direction");
    }

    @Test
    public void testGetPartialConnectionTasks() throws Exception {
        Response response = target("/devicetypes/112/deviceconfigurations/113/connectionmethods").queryParam("start", 0).queryParam("limit", 5).request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel jsonModel = JsonModel.create((ByteArrayInputStream) response.getEntity());
        assertThat(jsonModel.<List>get("$.link")).hasSize(1);
        assertThat(jsonModel.<String>get("$.link[0].params.rel")).isEqualTo("current");
        assertThat(jsonModel.<String>get("$.link[0].href")).isEqualTo("http://localhost:9998/devicetypes/112/deviceconfigurations/113/connectionmethods?start=0&limit=5");
        assertThat(jsonModel.<List>get("$.data")).hasSize(2);
        assertThat(jsonModel.<Integer>get("$.data[0].id")).isEqualTo(114);
        assertThat(jsonModel.<String>get("$.data[0].name")).isEqualTo("partial conn task 114");
        assertThat(jsonModel.<String>get("$.data[0].link.params.rel")).isEqualTo("self");
        assertThat(jsonModel.<String>get("$.data[0].link.href")).isEqualTo("http://localhost:9998/devicetypes/112/deviceconfigurations/113/connectionmethods/114");
        assertThat(jsonModel.<Integer>get("$.data[1].id")).isEqualTo(124);
        assertThat(jsonModel.<String>get("$.data[1].name")).isEqualTo("partial conn task 124");
        assertThat(jsonModel.<String>get("$.data[1].link.params.rel")).isEqualTo("self");
        assertThat(jsonModel.<String>get("$.data[1].link.href")).isEqualTo("http://localhost:9998/devicetypes/112/deviceconfigurations/113/connectionmethods/124");
    }

    @Test
    public void testGetPartialConnectionTask() throws Exception {
        Response response = target("/devicetypes/112/deviceconfigurations/113/connectionmethods/124").request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel jsonModel = JsonModel.create((ByteArrayInputStream) response.getEntity());
        assertThat(jsonModel.<Integer>get("$.id")).isEqualTo(124);
        assertThat(jsonModel.<String>get("$.name")).isEqualTo("partial conn task 124");
        assertThat(jsonModel.<String>get("$.direction")).isEqualTo("Inbound");
        assertThat(jsonModel.<String>get("$.link.params.rel")).isEqualTo("self");
        assertThat(jsonModel.<String>get("$.link.href")).isEqualTo("http://localhost:9998/devicetypes/112/deviceconfigurations/113/connectionmethods/124");
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

    private PartialInboundConnectionTask mockPartialInboundConnectionTask(long id, String name, DeviceConfiguration deviceConfig) {
        PartialInboundConnectionTask mock = mock(PartialInboundConnectionTask.class);
        when(mock.getName()).thenReturn(name);
        when(mock.getId()).thenReturn(id);
        when(mock.getConfiguration()).thenReturn(deviceConfig);
        return mock;
    }
}

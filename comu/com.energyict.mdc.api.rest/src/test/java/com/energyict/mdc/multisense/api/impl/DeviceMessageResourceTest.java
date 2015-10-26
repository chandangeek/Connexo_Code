package com.energyict.mdc.multisense.api.impl;

import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessage;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageStatus;
import com.jayway.jsonpath.JsonModel;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DeviceMessageResourceTest extends MultisensePublicApiJerseyTest {

    private final Instant now = Instant.now();

    @Before
    public void setup() {
        DeviceType deviceType = mockDeviceType(1L, "device type");
        DeviceConfiguration deviceConfiguration = mockDeviceConfiguration(2L, "Default", deviceType);
        Device mockDevice = mockDevice("X01", "1001", deviceConfiguration);
        DeviceMessage deviceMessage = mock(DeviceMessage.class);
        when(deviceMessage.getId()).thenReturn(31L);
        when(deviceMessage.getStatus()).thenReturn(DeviceMessageStatus.CONFIRMED);
        when(deviceMessage.getSentDate()).thenReturn(Optional.of(now));
        when(deviceMessage.getDevice()).thenReturn(mockDevice);
        when(mockDevice.getMessages()).thenReturn(Arrays.asList(deviceMessage));
    }

    @Test
    public void testAllGetDeviceMessagesPaged() throws Exception {
        Response response = target("/devices/X01/messages").queryParam("start",0).queryParam("limit",10).request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel model = JsonModel.model((InputStream) response.getEntity());
        assertThat(model.<List>get("link")).hasSize(1);
        assertThat(model.<String>get("link[0].params.rel")).isEqualTo("current");
        assertThat(model.<String>get("link[0].params.title")).isEqualTo("current page");
        assertThat(model.<String>get("link[0].href")).isEqualTo("http://localhost:9998/devices/X01/messages?start=0&limit=10");
        assertThat(model.<List>get("data")).hasSize(1);
        assertThat(model.<Integer>get("data[0].id")).isEqualTo(31);
        assertThat(model.<String>get("data[0].status")).isEqualTo("Confirmed");
        assertThat(model.<Instant>get("data[0].sentDate")).isEqualTo(now.toEpochMilli());
        assertThat(model.<String>get("data[0].link.params.rel")).isEqualTo(LinkInfo.REF_SELF);
        assertThat(model.<String>get("data[0].link.href")).isEqualTo("http://localhost:9998/devices/X01/messages/31");
    }

    @Test
    public void testGetSingleDeviceMessageWithFields() throws Exception {
        Response response = target("/devices/X01/messages/31").queryParam("fields","id,status").request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel model = JsonModel.model((InputStream) response.getEntity());
        assertThat(model.<Integer>get("$.id")).isEqualTo(31);
        assertThat(model.<String>get("$.link")).isNull();
        assertThat(model.<String>get("$.device")).isNull();
        assertThat(model.<String>get("$.status")).isEqualTo("Confirmed");
        assertThat(model.<Instant>get("$.sendDate")).isNull();
    }

    @Test
    public void testDeviceMessageFields() throws Exception {
        Response response = target("/devices/x/messages").request("application/json").method("PROPFIND", Response.class);
        JsonModel model = JsonModel.model((InputStream) response.getEntity());
        assertThat(model.<List>get("$")).hasSize(5);
        assertThat(model.<List<String>>get("$")).containsOnly("id", "link", "status", "sentDate", "device");
    }
}

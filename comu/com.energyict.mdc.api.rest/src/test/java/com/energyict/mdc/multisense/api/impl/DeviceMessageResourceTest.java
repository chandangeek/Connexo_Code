package com.energyict.mdc.multisense.api.impl;

import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessage;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageCategory;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpec;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageStatus;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;
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

    public static final long id = 31L;
    private final Instant now = Instant.now();

    @Before
    public void setup() {
        DeviceType deviceType = mockDeviceType(1L, "device type");
        DeviceConfiguration deviceConfiguration = mockDeviceConfiguration(2L, "Default", deviceType);
        Device mockDevice = mockDevice("X01", "1001", deviceConfiguration);
        DeviceMessageCategory category = mockDeviceMessageCategory(33, "category");
        DeviceMessageSpec messageSpec = mockDeviceMessageSpec(DeviceMessageId.CLOCK_SET_DST, "dst");
        when(messageSpec.getCategory()).thenReturn(category);
        DeviceMessage deviceMessage = mockDeviceMessage(mockDevice, messageSpec, now);
        when(mockDevice.getMessages()).thenReturn(Arrays.asList(deviceMessage));
    }

    protected DeviceMessage mockDeviceMessage(Device mockDevice, DeviceMessageSpec specification, Object now) {
        DeviceMessage deviceMessage = mock(DeviceMessage.class);
        when(deviceMessage.getId()).thenReturn(id);
        when(deviceMessage.getStatus()).thenReturn(DeviceMessageStatus.CONFIRMED);
        when(deviceMessage.getSentDate()).thenReturn(Optional.of(this.now));
        when(deviceMessage.getDevice()).thenReturn(mockDevice);
        when(deviceMessage.getSpecification()).thenReturn(specification);
        return deviceMessage;
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
        assertThat(model.<String>get("data[0].link.params.rel")).isEqualTo(Relation.REF_SELF.rel());
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
    public void testGetSingleDeviceMessageAllFields() throws Exception {
        Response response = target("/devices/X01/messages/31").request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel model = JsonModel.model((InputStream) response.getEntity());
        assertThat(model.<Integer>get("$.id")).isEqualTo(31);
        assertThat(model.<String>get("$.link.href")).isEqualTo("http://localhost:9998/devices/X01/messages/31");
        assertThat(model.<Integer>get("$.device.id")).isEqualTo("X01".hashCode());
        assertThat(model.<String>get("$.device.link.href")).isEqualTo("http://localhost:9998/devices/X01");
        assertThat(model.<String>get("$.status")).isEqualTo("Confirmed");
        assertThat(model.<Instant>get("$.sendDate")).isNull();
    }

    @Test
    public void testDeviceMessageFields() throws Exception {
        Response response = target("/devices/x/messages").request("application/json").method("PROPFIND", Response.class);
        JsonModel model = JsonModel.model((InputStream) response.getEntity());
        assertThat(model.<List>get("$")).hasSize(12);
        assertThat(model.<List<String>>get("$")).containsOnly("creationDate","device","id","link","messageSpecification","deviceMessageAttributes","protocolInfo",
                "releaseDate","sentDate","status","trackingId","user");
    }
}

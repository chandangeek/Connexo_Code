package com.energyict.mdc.multisense.api.impl;

import com.elster.jupiter.devtools.tests.FakeBuilder;
import com.elster.jupiter.rest.api.util.v1.hypermedia.LinkInfo;
import com.elster.jupiter.rest.api.util.v1.hypermedia.Relation;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessage;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageCategory;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpec;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;
import com.energyict.mdc.upl.messages.DeviceMessageStatus;
import com.jayway.jsonpath.JsonModel;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DeviceMessageResourceTest extends MultisensePublicApiJerseyTest {

    public static final long id = 31L;
    private final Instant now = Instant.now();
    private Device mockDevice;
    private DeviceMessageSpec messageSpec;

    @Before
    public void setup() {
        DeviceType deviceType = mockDeviceType(1L, "device type", 3333L);
        DeviceConfiguration deviceConfiguration = mockDeviceConfiguration(2L, "Default", deviceType, 3333L);
        mockDevice = mockDevice("X01", "1001", deviceConfiguration, 13333L);
        DeviceMessageCategory category = mockDeviceMessageCategory(33, "category");
        messageSpec = mockDeviceMessageSpec(DeviceMessageId.CLOCK_SET_DST, "dst");
        when(messageSpec.getCategory()).thenReturn(category);
        DeviceMessage deviceMessage = mockDeviceMessage(id, mockDevice, messageSpec, 1313L);
        when(mockDevice.getMessages()).thenReturn(Arrays.asList(deviceMessage));
    }

    protected DeviceMessage mockDeviceMessage(long id, Device mockDevice, DeviceMessageSpec specification, long version) {
        DeviceMessage deviceMessage = mock(DeviceMessage.class);
        when(deviceMessage.getId()).thenReturn(id);
        when(deviceMessage.getStatus()).thenReturn(DeviceMessageStatus.CONFIRMED);
        when(deviceMessage.getSentDate()).thenReturn(Optional.of(this.now));
        when(deviceMessage.getDevice()).thenReturn(mockDevice);
        when(deviceMessage.getSpecification()).thenReturn(specification);
        when(deviceMessage.getVersion()).thenReturn(version);
        when(deviceMessageService.findAndLockDeviceMessageByIdAndVersion(id, version)).thenReturn(Optional.of(deviceMessage));
        when(deviceMessageService.findDeviceMessageById(id)).thenReturn(Optional.of(deviceMessage));
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
        assertThat(model.<Integer>get("$.device.id")).isNotNull();
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
    public void testUpdateDeviceMessage() throws Exception {
        DeviceMessageInfo info = new DeviceMessageInfo();
        info.version = 1313L;
        info.protocolInfo = "xxx";
        info.device = new LinkInfo();
        info.device.version = 13333L;
        info.releaseDate = now;
        info.messageSpecification = new LinkInfo();
        info.messageSpecification.id = 15009L;

        Response response = target("/devices/X01/messages/31").request().put(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
    }

    @Test
    public void testUpdateDeviceMessageWithoutDeviceVersion() throws Exception {
        DeviceMessageInfo info = new DeviceMessageInfo();
        info.version = 1313L;
        info.protocolInfo = "xxx";
        info.device = new LinkInfo();
        info.device.version = null;
        info.releaseDate = now;
        info.messageSpecification = new LinkInfo();
        info.messageSpecification.id = 15009L;

        Response response = target("/devices/X01/messages/31").request().put(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void testUpdateDeviceMessageWithMessage() throws Exception {
        DeviceMessageInfo info = new DeviceMessageInfo();
        info.version = 1313L;
        info.protocolInfo = "xxx";
        info.device = new LinkInfo();
        info.device.version = 13333L;
        info.releaseDate = now;
        info.messageSpecification = null;

        Response response = target("/devices/X01/messages/31").request().put(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void testUpdateDeviceMessageWithoutVersion() throws Exception {
        DeviceMessageInfo info = new DeviceMessageInfo();
        info.version = null;
        info.protocolInfo = "xxx";
        info.device = new LinkInfo();
        info.device.version = 13333L;
        info.releaseDate = now;
        info.messageSpecification = new LinkInfo();
        info.messageSpecification.id = 15009L;

        Response response = target("/devices/X01/messages/31").request().put(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void testCreateDeviceMessage() throws Exception {
        DeviceMessageInfo info = new DeviceMessageInfo();
        info.protocolInfo = "xxx";
        info.device = new LinkInfo();
        info.device.version = 13333L;
        info.releaseDate = now;
        info.messageSpecification = new LinkInfo();
        info.messageSpecification.id = 15009L;

        DeviceMessage deviceMessage = mockDeviceMessage(100L, mockDevice, messageSpec, 2233L);
        Device.DeviceMessageBuilder builder = FakeBuilder.initBuilderStub(deviceMessage, Device.DeviceMessageBuilder.class);
        when(mockDevice.newDeviceMessage(any(DeviceMessageId.class))).thenReturn(builder);

        Response response = target("/devices/X01/messages/").request().post(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());
        assertThat(response.getHeaderString("location")).isEqualTo("http://localhost:9998/devices/X01/messages/100");
    }

    @Test
    public void testCreateDeviceMessageWithoutDeviceVersion() throws Exception {
        DeviceMessageInfo info = new DeviceMessageInfo();
        info.protocolInfo = "xxx";
        info.device = new LinkInfo();
        info.device.version = null;
        info.releaseDate = now;
        info.messageSpecification = new LinkInfo();
        info.messageSpecification.id = 15009L;

        DeviceMessage deviceMessage = mockDeviceMessage(100L, mockDevice, messageSpec, 2233L);
        Device.DeviceMessageBuilder builder = FakeBuilder.initBuilderStub(deviceMessage, Device.DeviceMessageBuilder.class);
        when(mockDevice.newDeviceMessage(any(DeviceMessageId.class))).thenReturn(builder);

        Response response = target("/devices/X01/messages/").request().post(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void testCreateDeviceMessageWithoutDeviceMessage() throws Exception {
        DeviceMessageInfo info = new DeviceMessageInfo();
        info.protocolInfo = "xxx";
        info.device = new LinkInfo();
        info.device.version = 13333L;
        info.releaseDate = now;
        info.messageSpecification = null;

        DeviceMessage deviceMessage = mockDeviceMessage(100L, mockDevice, messageSpec, 2233L);
        Device.DeviceMessageBuilder builder = FakeBuilder.initBuilderStub(deviceMessage, Device.DeviceMessageBuilder.class);
        when(mockDevice.newDeviceMessage(any(DeviceMessageId.class))).thenReturn(builder);

        Response response = target("/devices/X01/messages/").request().post(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void testDeviceMessageFields() throws Exception {
        Response response = target("/devices/x/messages").request("application/json").method("PROPFIND", Response.class);
        JsonModel model = JsonModel.model((InputStream) response.getEntity());
        assertThat(model.<List>get("$")).hasSize(13);
        assertThat(model.<List<String>>get("$")).containsOnly("version", "creationDate","device","id","link","messageSpecification","deviceMessageAttributes","protocolInfo",
                "releaseDate","sentDate","status","trackingId","user");
    }
}

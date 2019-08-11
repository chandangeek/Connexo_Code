/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.multisense.api.impl;

import com.elster.jupiter.devtools.tests.FakeBuilder;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.ValueFactory;
import com.energyict.mdc.common.device.config.ComTaskEnablement;
import com.energyict.mdc.common.device.config.DeviceConfiguration;
import com.energyict.mdc.common.device.config.DeviceType;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.protocol.DeviceMessage;
import com.energyict.mdc.common.protocol.DeviceMessageCategory;
import com.energyict.mdc.common.protocol.DeviceMessageId;
import com.energyict.mdc.common.protocol.DeviceMessageSpec;
import com.energyict.mdc.common.tasks.ComTask;
import com.energyict.mdc.common.tasks.ComTaskExecution;
import com.energyict.mdc.common.tasks.ComTaskExecutionBuilder;
import com.energyict.mdc.common.tasks.MessagesTask;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageAttribute;
import com.energyict.mdc.protocol.api.firmware.BaseFirmwareVersion;

import com.jayway.jsonpath.JsonModel;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DeviceContactorResourceTest extends MultisensePublicApiJerseyTest {

    private final Instant now = Instant.now();

    private Device mockDevice;

    @Before
    public void setup() {
        DeviceType deviceType = mockDeviceType(1L, "device type", 3333L);
        DeviceConfiguration deviceConfiguration = mockDeviceConfiguration(2L, "Default", deviceType, 3333L);
        DeviceMessageSpec arm = mockDeviceMessageSpec(DeviceMessageId.CONTACTOR_ARM, "arm");
        DeviceMessageSpec connect = mockDeviceMessageSpec(DeviceMessageId.CONTACTOR_CLOSE, "close");
        DeviceMessageSpec disconnect = mockDeviceMessageSpec(DeviceMessageId.CONTACTOR_OPEN, "open");
        DeviceMessageSpec connectWithActivationDate = mockDeviceMessageSpec(DeviceMessageId.CONTACTOR_CLOSE_WITH_ACTIVATION_DATE, "close_with_activation_date");
        DeviceMessageCategory category = mockDeviceMessageCategory(33, "category", arm, connect, disconnect, connectWithActivationDate);
        MessagesTask messagesTask = mockMessagesTask(3, category);
        ComTask comTask = mockComTask(4, "com task", 3333L, messagesTask);
        ComTaskEnablement comTaskEnablement = mockComTaskEnablement(comTask, deviceConfiguration, 3333L);
        when(deviceConfiguration.getComTaskEnablements()).thenReturn(Collections.singletonList(comTaskEnablement));
        mockDevice = mockDevice("X01", "1001", deviceConfiguration, 3333L);
        DeviceMessageSpec messageSpec = mockDeviceMessageSpec(DeviceMessageId.CLOCK_SET_DST, "dst");
        when(messageSpec.getCategory()).thenReturn(category);
        DeviceMessage deviceMessage = mockDeviceMessage(31L, mockDevice, messageSpec, Optional.of(now), 3333L);
        when(mockDevice.getMessages()).thenReturn(Arrays.asList(deviceMessage));
    }

    @Test
    public void testUpdateContactor() throws Exception {
        ContactorInfo info = new ContactorInfo();
        info.status = Status.connected;
        DeviceMessageSpec messageSpec = mockDeviceMessageSpec(DeviceMessageId.CONTACTOR_CLOSE, "fire in the hole");
        DeviceMessage deviceMessage = mockDeviceMessage(111L, mockDevice, messageSpec, Optional.of(now), 3333L);
        Device.DeviceMessageBuilder deviceMessageBuilder = FakeBuilder.initBuilderStub(deviceMessage, Device.DeviceMessageBuilder.class);
        when(mockDevice.newDeviceMessage(DeviceMessageId.CONTACTOR_CLOSE)).thenReturn(deviceMessageBuilder);
        ComTaskExecution comtaskExecution = mock(ComTaskExecution.class);
        ComTaskExecutionBuilder comTaskExecutionBuilder = FakeBuilder.initBuilderStub(comtaskExecution, ComTaskExecutionBuilder.class);
        when(mockDevice.newAdHocComTaskExecution(any())).thenReturn(comTaskExecutionBuilder);

        Response response = target("/devices/X01/contacter").request().put(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(Response.Status.ACCEPTED.getStatusCode());
        JsonModel model = JsonModel.model((InputStream) response.getEntity());
        assertThat(model.<Object>get("link")).isNotNull();
        assertThat(model.<String>get("link.href")).isEqualTo("http://localhost:9998/devices/X01/messages/111");
    }

    @Test
    public void testUpdateContactorWithActivationDate() throws Exception {
        ContactorInfo info = new ContactorInfo();
        info.status = Status.connected;
        info.activationDate = Instant.now();

        DeviceMessageSpec messageSpec = mockDeviceMessageSpec(DeviceMessageId.CONTACTOR_CLOSE_WITH_ACTIVATION_DATE, "fire in the hole");

        PropertySpec firmwareVersionPropertySpec = mock(PropertySpec.class);
        ValueFactory valueFactory1 = mock(ValueFactory.class);
        when(valueFactory1.getValueType()).thenReturn(BaseFirmwareVersion.class);
        when(firmwareVersionPropertySpec.getValueFactory()).thenReturn(valueFactory1);
        when(firmwareVersionPropertySpec.getName()).thenReturn("FirmwareDeviceMessage.upgrade.userfile");

        PropertySpec activationDatePropertySpec = mock(PropertySpec.class);
        ValueFactory valueFactory = mock(ValueFactory.class);
        when(valueFactory.getValueType()).thenReturn(Date.class);
        when(activationDatePropertySpec.getValueFactory()).thenReturn(valueFactory);
        when(activationDatePropertySpec.getName()).thenReturn("FirmwareDeviceMessage.upgrade.activationdate");

        when(messageSpec.getPropertySpecs()).thenReturn(Arrays.asList(firmwareVersionPropertySpec, activationDatePropertySpec));
        DeviceMessage deviceMessage = mockDeviceMessage(111L, mockDevice, messageSpec, Optional.of(now), 3333L);

        DeviceMessageAttribute firmwareVersionAttribute = mock(DeviceMessageAttribute.class);
        when(firmwareVersionAttribute.getSpecification()).thenReturn(firmwareVersionPropertySpec);
        when(firmwareVersionAttribute.getDeviceMessage()).thenReturn(deviceMessage);
        when(firmwareVersionAttribute.getName()).thenReturn("FirmwareDeviceMessage.upgrade.userfile");

        DeviceMessageAttribute activationDateAttribute = mock(DeviceMessageAttribute.class);
        when(activationDateAttribute.getSpecification()).thenReturn(activationDatePropertySpec);
        when(activationDateAttribute.getDeviceMessage()).thenReturn(deviceMessage);
        when(activationDateAttribute.getName()).thenReturn("FirmwareDeviceMessage.upgrade.activationdate");

        List<com.energyict.mdc.upl.messages.DeviceMessageAttribute> attributes = Arrays.asList(firmwareVersionAttribute, activationDateAttribute);
        doReturn(attributes).when(deviceMessage).getAttributes();

        Device.DeviceMessageBuilder deviceMessageBuilder = FakeBuilder.initBuilderStub(deviceMessage, Device.DeviceMessageBuilder.class);
        when(mockDevice.newDeviceMessage(DeviceMessageId.CONTACTOR_CLOSE_WITH_ACTIVATION_DATE)).thenReturn(deviceMessageBuilder);
        ComTaskExecution comtaskExecution = mock(ComTaskExecution.class);
        ComTaskExecutionBuilder comTaskExecutionBuilder = FakeBuilder.initBuilderStub(comtaskExecution, ComTaskExecutionBuilder.class);
        when(mockDevice.newAdHocComTaskExecution(any())).thenReturn(comTaskExecutionBuilder);

        Response response = target("/devices/X01/contacter").request().put(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(Response.Status.ACCEPTED.getStatusCode());
        JsonModel model = JsonModel.model((InputStream) response.getEntity());
        assertThat(model.<Object>get("link")).isNotNull();
        assertThat(model.<String>get("link.href")).isEqualTo("http://localhost:9998/devices/X01/messages/111");
    }

    @Test
    public void testUpdateContactorWithoutStatus() throws Exception {
        ContactorInfo info = new ContactorInfo();
        info.status = null; // no status

        Response response = target("/devices/X01/contacter").request().put(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }

}

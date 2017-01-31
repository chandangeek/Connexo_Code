/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.multisense.api.impl;

import com.elster.jupiter.devtools.tests.FakeBuilder;
import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ComTaskExecutionBuilder;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessage;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageCategory;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpec;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;
import com.energyict.mdc.tasks.ComTask;
import com.energyict.mdc.tasks.MessagesTask;

import com.jayway.jsonpath.JsonModel;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
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
        DeviceMessageCategory category = mockDeviceMessageCategory(33, "category", arm , connect, disconnect);
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
    public void testUpdateContactorWithoutStatus() throws Exception {
        ContactorInfo info = new ContactorInfo();
        info.status = null; // no status

        Response response = target("/devices/X01/contacter").request().put(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }

}

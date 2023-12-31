/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.events.datastorage;

import com.energyict.mdc.common.protocol.DeviceMessage;
import com.energyict.mdc.device.data.DeviceMessageService;
import com.energyict.mdc.engine.events.Category;
import com.energyict.mdc.engine.impl.events.AbstractComServerEventImpl;
import com.energyict.mdc.upl.messages.DeviceMessageStatus;
import com.energyict.mdc.upl.meterdata.identifiers.MessageIdentifier;

import java.time.Clock;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class UpdateDeviceMessageEventTest {

    @Mock
    private AbstractComServerEventImpl.ServiceProvider serviceProvider;
    @Mock
    private DeviceMessageService deviceMessageService;

    @Before
    public void initMocks(){
        when(serviceProvider.clock()).thenReturn(Clock.systemDefaultZone());
        when(serviceProvider.deviceMessageService()).thenReturn(this.deviceMessageService);
    }

    @Test
    public void testCategory(){
        DeviceMessage message = mock(DeviceMessage.class);
        when(message.getId()).thenReturn(184L);

        MessageIdentifier messageIdentifier = mock(MessageIdentifier.class);
        when(this.deviceMessageService.findDeviceMessageByIdentifier(messageIdentifier)).thenReturn(Optional.of(message));

        DeviceMessageStatus deviceMessageStatus = DeviceMessageStatus.SENT;
        String protocolInfo = "This is the protocol info";

        UpdateDeviceMessageEvent event = new UpdateDeviceMessageEvent(serviceProvider, messageIdentifier, deviceMessageStatus, protocolInfo);
        assertThat(event.getCategory()).isEqualTo(Category.COLLECTED_DATA_PROCESSING);
    }

    @Test
    public void testToString(){
        DeviceMessage message = mock(DeviceMessage.class);
        when(message.getId()).thenReturn(184L);

        MessageIdentifier messageIdentifier = mock(MessageIdentifier.class);
        when(this.deviceMessageService.findDeviceMessageByIdentifier(messageIdentifier)).thenReturn(Optional.of(message));

        DeviceMessageStatus deviceMessageStatus = DeviceMessageStatus.SENT;
        String protocolInfo = "This is the protocol info";

        UpdateDeviceMessageEvent event = new UpdateDeviceMessageEvent(serviceProvider, messageIdentifier, deviceMessageStatus, protocolInfo);

        // Business method
        String eventString = event.toString();

        // Asserts
        assertThat(eventString).matches("\\{.*\\}");
    }

    @Test
    public void testToStringNoDeviceIdentifier(){

        DeviceMessageStatus deviceMessageStatus = DeviceMessageStatus.SENT;
        String protocolInfo = "This is the protocol info";

        UpdateDeviceMessageEvent event = new UpdateDeviceMessageEvent(serviceProvider, null, deviceMessageStatus, protocolInfo);

        // Business method
        String eventString = event.toString();

        // Asserts
        assertThat(eventString).matches("\\{.*\\}");
    }

    @Test
    public void testToStringNoMessageStatus(){
        DeviceMessage message = mock(DeviceMessage.class);
        when(message.getId()).thenReturn(184L);

        MessageIdentifier messageIdentifier = mock(MessageIdentifier.class);
        when(this.deviceMessageService.findDeviceMessageByIdentifier(messageIdentifier)).thenReturn(Optional.of(message));

        String protocolInfo = "This is the protocol info";

        UpdateDeviceMessageEvent event = new UpdateDeviceMessageEvent(serviceProvider, messageIdentifier, null, protocolInfo);

        // Business method
        String eventString = event.toString();

        // Asserts
        assertThat(eventString).matches("\\{.*\\}");
    }

    @Test
    public void testToStringNoProtocolInfo(){
        DeviceMessage message = mock(DeviceMessage.class);
        when(message.getId()).thenReturn(184L);

        MessageIdentifier messageIdentifier = mock(MessageIdentifier.class);
        when(this.deviceMessageService.findDeviceMessageByIdentifier(messageIdentifier)).thenReturn(Optional.of(message));

        DeviceMessageStatus deviceMessageStatus = DeviceMessageStatus.SENT;

        UpdateDeviceMessageEvent event = new UpdateDeviceMessageEvent(serviceProvider, messageIdentifier, deviceMessageStatus, null);

        // Business method
        String eventString = event.toString();

        // Asserts
        assertThat(eventString).matches("\\{.*\\}");
    }

}

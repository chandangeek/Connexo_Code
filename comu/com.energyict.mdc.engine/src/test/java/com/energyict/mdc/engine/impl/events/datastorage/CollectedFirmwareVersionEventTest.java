/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.events.datastorage;

import com.energyict.mdc.engine.events.Category;
import com.energyict.mdc.engine.impl.events.AbstractComServerEventImpl;
import com.energyict.mdc.protocol.api.device.data.CollectedFirmwareVersion;
import com.energyict.mdc.protocol.api.device.data.identifiers.DeviceIdentifier;

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
public class CollectedFirmwareVersionEventTest {

    @Mock
    private AbstractComServerEventImpl.ServiceProvider serviceProvider;

    @Before
    public void initMocks(){
        when(serviceProvider.clock()).thenReturn(Clock.systemDefaultZone());
    }

    @Test
    public void testCategory(){
        CollectedFirmwareVersion firmwareVersion = mock(CollectedFirmwareVersion.class);

        CollectedFirmwareVersionEvent event = new CollectedFirmwareVersionEvent(serviceProvider, firmwareVersion);
        assertThat(event.getCategory()).isEqualTo(Category.COLLECTED_DATA_PROCESSING);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNoPayload(){
        // Business method
        new CollectedFirmwareVersionEvent(serviceProvider, null);
    }


    @Test
    public void testToStringWithoutVersionInfo(){
        DeviceIdentifier deviceIdentifier = mock(DeviceIdentifier.class);
        when(deviceIdentifier.toString()).thenReturn("My Device identifier");
        CollectedFirmwareVersion firmwareVersion = mock(CollectedFirmwareVersion.class);
        when(firmwareVersion.getDeviceIdentifier()).thenReturn(deviceIdentifier);
        when(firmwareVersion.getActiveMeterFirmwareVersion()).thenReturn(Optional.empty());
        when(firmwareVersion.getPassiveMeterFirmwareVersion()).thenReturn(Optional.empty());
        when(firmwareVersion.getActiveCommunicationFirmwareVersion()).thenReturn(Optional.empty());
        when(firmwareVersion.getPassiveCommunicationFirmwareVersion()).thenReturn(Optional.empty());

        CollectedFirmwareVersionEvent event = new CollectedFirmwareVersionEvent(serviceProvider, firmwareVersion);
        // Business method
        String eventString = event.toString();

        // Asserts
        assertThat(eventString).matches("\\{.*\\}");
    }

    @Test
    public void testToStringWithMeterFirmwareVersionInfo(){
        DeviceIdentifier deviceIdentifier = mock(DeviceIdentifier.class);
        when(deviceIdentifier.toString()).thenReturn("My Device identifier");
        CollectedFirmwareVersion firmwareVersion = mock(CollectedFirmwareVersion.class);
        when(firmwareVersion.getDeviceIdentifier()).thenReturn(deviceIdentifier);
        when(firmwareVersion.getActiveMeterFirmwareVersion()).thenReturn(Optional.of("V2"));
        when(firmwareVersion.getPassiveMeterFirmwareVersion()).thenReturn(Optional.of("V1"));
        when(firmwareVersion.getActiveCommunicationFirmwareVersion()).thenReturn(Optional.empty());
        when(firmwareVersion.getPassiveCommunicationFirmwareVersion()).thenReturn(Optional.empty());

        CollectedFirmwareVersionEvent event = new CollectedFirmwareVersionEvent(serviceProvider, firmwareVersion);
        // Business method
        String eventString = event.toString();

        // Asserts
        assertThat(eventString).matches("\\{.*\\}");
    }

    @Test
    public void testToStringWithCommunicationFirmwareVersionInfo(){
        DeviceIdentifier deviceIdentifier = mock(DeviceIdentifier.class);
        when(deviceIdentifier.toString()).thenReturn("My Device identifier");
        CollectedFirmwareVersion firmwareVersion = mock(CollectedFirmwareVersion.class);
        when(firmwareVersion.getDeviceIdentifier()).thenReturn(deviceIdentifier);
        when(firmwareVersion.getActiveMeterFirmwareVersion()).thenReturn(Optional.empty());
        when(firmwareVersion.getPassiveMeterFirmwareVersion()).thenReturn(Optional.empty());
        when(firmwareVersion.getActiveCommunicationFirmwareVersion()).thenReturn(Optional.of("CV2"));
        when(firmwareVersion.getPassiveCommunicationFirmwareVersion()).thenReturn(Optional.of("CV1"));

        CollectedFirmwareVersionEvent event = new CollectedFirmwareVersionEvent(serviceProvider, firmwareVersion);
        // Business method
        String eventString = event.toString();

        // Asserts
        assertThat(eventString).matches("\\{.*\\}");
    }

    @Test
    public void testToStringWithMeterAndCommunicationFirmwareVersionInfo(){
        DeviceIdentifier deviceIdentifier = mock(DeviceIdentifier.class);
        when(deviceIdentifier.toString()).thenReturn("My Device identifier");
        CollectedFirmwareVersion firmwareVersion = mock(CollectedFirmwareVersion.class);
        when(firmwareVersion.getDeviceIdentifier()).thenReturn(deviceIdentifier);
        when(firmwareVersion.getActiveMeterFirmwareVersion()).thenReturn(Optional.of("V2"));
        when(firmwareVersion.getPassiveMeterFirmwareVersion()).thenReturn(Optional.of("V1"));
        when(firmwareVersion.getActiveCommunicationFirmwareVersion()).thenReturn(Optional.of("CV2"));
        when(firmwareVersion.getPassiveCommunicationFirmwareVersion()).thenReturn(Optional.of("CV1"));

        CollectedFirmwareVersionEvent event = new CollectedFirmwareVersionEvent(serviceProvider, firmwareVersion);
        // Business method
        String eventString = event.toString();

        // Asserts
        assertThat(eventString).matches("\\{.*\\}");
    }
}

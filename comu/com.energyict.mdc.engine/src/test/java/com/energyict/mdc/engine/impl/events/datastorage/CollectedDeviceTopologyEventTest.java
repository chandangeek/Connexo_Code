/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.events.datastorage;

import com.energyict.mdc.engine.events.Category;
import com.energyict.mdc.engine.impl.events.AbstractComServerEventImpl;
import com.energyict.mdc.protocol.api.device.data.CollectedTopology;
import com.energyict.mdc.protocol.api.device.data.identifiers.DeviceIdentifier;

import java.time.Clock;
import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CollectedDeviceTopologyEventTest {

    @Mock
    private AbstractComServerEventImpl.ServiceProvider serviceProvider;

    @Before
    public void initMocks(){
        when(serviceProvider.clock()).thenReturn(Clock.systemDefaultZone());
    }

    @Test
    public void testCategory(){
        CollectedTopology topology = mock(CollectedTopology.class);

        CollectedDeviceTopologyEvent event = new CollectedDeviceTopologyEvent(serviceProvider, topology);
        assertThat(event.getCategory()).isEqualTo(Category.COLLECTED_DATA_PROCESSING);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNoPayload(){
        // Business method
        new CollectedDeviceTopologyEvent(serviceProvider, null);
    }

    @Test
    public void testToStringWithoutSlaveDevices(){
        DeviceIdentifier deviceIdentifier = mock(DeviceIdentifier.class);
        when(deviceIdentifier.toString()).thenReturn("My Device identifier");
        CollectedTopology topology = mock(CollectedTopology.class);
        when(topology.getDeviceIdentifier()).thenReturn(deviceIdentifier);

        CollectedDeviceTopologyEvent event = new CollectedDeviceTopologyEvent(serviceProvider, topology);
        // Business method
        String eventString = event.toString();

        // Asserts
        assertThat(eventString).matches("\\{.*\\}");
    }

    @Test
    public void testToStringWithSlaveDevices(){
        DeviceIdentifier deviceIdentifier = mock(DeviceIdentifier.class);
        when(deviceIdentifier.toString()).thenReturn("My Device identifier");

        DeviceIdentifier slave1 = mock(DeviceIdentifier.class);
        when(slave1.toString()).thenReturn("My first slave device");

        DeviceIdentifier slave2 = mock(DeviceIdentifier.class);
        when(slave2.toString()).thenReturn("My second slave device");

        CollectedTopology topology = mock(CollectedTopology.class);
        when(topology.getDeviceIdentifier()).thenReturn(deviceIdentifier);
        when(topology.getSlaveDeviceIdentifiers()).thenReturn(Arrays.asList(slave1, slave2));

        CollectedDeviceTopologyEvent event = new CollectedDeviceTopologyEvent(serviceProvider, topology);
        // Business method
        String eventString = event.toString();

        // Asserts
        assertThat(eventString).matches("\\{.*\\}");
    }
}

package com.energyict.mdc.engine.impl.events.datastorage;

import com.energyict.mdc.engine.events.Category;
import com.energyict.mdc.engine.impl.events.AbstractComServerEventImpl;
import com.energyict.mdc.upl.meterdata.CollectedTopology;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.Clock;
import java.util.HashMap;
import java.util.Map;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Copyrights EnergyICT
 * Date: 24/02/2016
 * Time: 13:51
 */
@RunWith(MockitoJUnitRunner.class)
public class CollectedDeviceTopologyEventTest {

    @Mock
    private AbstractComServerEventImpl.ServiceProvider serviceProvider;

    @Before
    public void initMocks() {
        when(serviceProvider.clock()).thenReturn(Clock.systemDefaultZone());
    }

    @Test
    public void testCategory() {
        CollectedTopology topology = mock(CollectedTopology.class);

        CollectedDeviceTopologyEvent event = new CollectedDeviceTopologyEvent(serviceProvider, topology);
        assertThat(event.getCategory()).isEqualTo(Category.COLLECTED_DATA_PROCESSING);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNoPayload() {
        // Business method
        new CollectedDeviceTopologyEvent(serviceProvider, null);
    }

    @Test
    public void testToStringWithoutSlaveDevices() {
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
    public void testToStringWithSlaveDevices() {
        DeviceIdentifier deviceIdentifier = mock(DeviceIdentifier.class);
        when(deviceIdentifier.toString()).thenReturn("My Device identifier");

        DeviceIdentifier slave1 = mock(DeviceIdentifier.class);
        when(slave1.toString()).thenReturn("My first slave device");

        DeviceIdentifier slave2 = mock(DeviceIdentifier.class);
        when(slave2.toString()).thenReturn("My second slave device");

        CollectedTopology topology = mock(CollectedTopology.class);
        when(topology.getDeviceIdentifier()).thenReturn(deviceIdentifier);

        Map<DeviceIdentifier, com.energyict.mdc.upl.meterdata.CollectedTopology.ObservationTimestampProperty> map = new HashMap<>();
        com.energyict.mdc.upl.meterdata.CollectedTopology.ObservationTimestampProperty date = mock(com.energyict.mdc.upl.meterdata.CollectedTopology.ObservationTimestampProperty.class);
        map.put(slave1, date);
        map.put(slave2, date);
        when(topology.getSlaveDeviceIdentifiers()).thenReturn(map);

        CollectedDeviceTopologyEvent event = new CollectedDeviceTopologyEvent(serviceProvider, topology);
        // Business method
        String eventString = event.toString();

        // Asserts
        assertThat(eventString).matches("\\{.*\\}");
    }
}

package com.energyict.mdc.device.topology.impl;

import com.energyict.mdc.device.data.Device;

import com.google.common.collect.Range;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneOffset;

import org.junit.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * Tests the {@link DeviceTopologyImpl} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-06-03 (11:00)
 */
public class DeviceTopologyImplTest {

    private static final LocalDateTime JAN_1ST = LocalDateTime.of(2014, Month.JANUARY, 1, 0, 0, 0);
    private static final Range<Instant> JAN_2014 = Range.closed(JAN_1ST.toInstant(ZoneOffset.UTC), JAN_1ST.plusMonths(1).toInstant(ZoneOffset.UTC));

    @Test
    public void testConstructor () {
        Device device = mock(Device.class);

        // Business method
        DeviceTopologyImpl communicationTopology = new DeviceTopologyImpl(device, JAN_2014);

        // Asserts
        assertThat(communicationTopology.getPeriod()).isEqualTo(JAN_2014);
        assertThat(communicationTopology.getRoot()).isEqualTo(device);
        assertThat(communicationTopology.getChildren()).isEmpty();
        assertThat(communicationTopology.getDevices()).isEmpty();
        assertThat(communicationTopology.isLeaf()).isTrue();
    }

    @Test
    public void testAddOneChild () {
        Device device1 = mock(Device.class);
        Device device2 = mock(Device.class);
        DeviceTopologyImpl communicationTopology = new DeviceTopologyImpl(device1, JAN_2014);
        DeviceTopologyImpl childTopology = new DeviceTopologyImpl(device2, JAN_2014);

        // Business method
        boolean result = communicationTopology.addChild(childTopology);

        // Asserts
        assertThat(result).isTrue();
        assertThat(communicationTopology.getChildren()).containsOnly(childTopology);
        assertThat(communicationTopology.isLeaf()).isFalse();
        assertThat(communicationTopology.getDevices()).containsOnly(device2);
    }

    @Test
    public void testAddChildren () {
        Device device1 = mock(Device.class);
        Device device2 = mock(Device.class);
        Device device3 = mock(Device.class);
        DeviceTopologyImpl communicationTopology = new DeviceTopologyImpl(device1, JAN_2014);
        DeviceTopologyImpl childTopology1 = new DeviceTopologyImpl(device2, JAN_2014);
        DeviceTopologyImpl childTopology2 = new DeviceTopologyImpl(device3, JAN_2014);

        // Business method
        communicationTopology.addChild(childTopology1);
        communicationTopology.addChild(childTopology2);

        // Asserts
        assertThat(communicationTopology.getChildren()).containsOnly(childTopology1, childTopology2);
        assertThat(communicationTopology.isLeaf()).isFalse();
        assertThat(communicationTopology.getDevices()).containsOnly(device2, device3);
    }

}
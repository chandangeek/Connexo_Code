/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.topology.impl;

import com.energyict.mdc.device.data.Device;

import com.google.common.collect.Range;

import java.time.Instant;
import java.util.Arrays;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link CompleteTopologyTimesliceImpl} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-06-03 (14:01
 */
public class CompleteTopologyTimesliceImplTest {

    @Test
    public void testVarargsConstructor () {
        Device device1 = mock(Device.class);
        when(device1.getId()).thenReturn(1L);
        Device device2 = mock(Device.class);
        when(device2.getId()).thenReturn(2L);
        Range<Instant> period = Range.atLeast(Instant.EPOCH);

        // Business method
        CompleteTopologyTimesliceImpl topologyEntry = new CompleteTopologyTimesliceImpl(period, device1, device2);

        // Asserts
        assertThat(topologyEntry.getPeriod()).isEqualTo(period);
        assertThat(topologyEntry.getDevices()).containsOnly(device1, device2);
    }

    @Test
    public void testListConstructor () {
        Device device1 = mock(Device.class);
        when(device1.getId()).thenReturn(1L);
        Device device2 = mock(Device.class);
        when(device2.getId()).thenReturn(2L);
        Range<Instant> period = Range.atLeast(Instant.EPOCH);

        // Business method
        CompleteTopologyTimesliceImpl topologyEntry = new CompleteTopologyTimesliceImpl(period, Arrays.asList(device1, device2));

        // Asserts
        assertThat(topologyEntry.getPeriod()).isEqualTo(period);
        assertThat(topologyEntry.getDevices()).containsOnly(device1, device2);
    }

    @Test
    public void testAddDeviceToEmptyTopologyWithVarargs () {
        Range<Instant> period = Range.atLeast(Instant.EPOCH);
        CompleteTopologyTimesliceImpl topologyEntry = new CompleteTopologyTimesliceImpl(period);
        Device device = mock(Device.class);
        when(device.getId()).thenReturn(1L);

        // Business method
        topologyEntry.add(device);

        // Asserts
        assertThat(topologyEntry.getPeriod()).isEqualTo(period);
        assertThat(topologyEntry.getDevices()).containsOnly(device);
    }

    @Test
    public void testAddDeviceToEmptyTopologyAsList () {
        Range<Instant> range = Range.all();
        CompleteTopologyTimesliceImpl topologyEntry = new CompleteTopologyTimesliceImpl(range);
        Device device = mock(Device.class);
        when(device.getId()).thenReturn(1L);

        // Business method
        topologyEntry.addAll(Arrays.asList(device));

        // Asserts
        assertThat(topologyEntry.getPeriod()).isEqualTo(range);
        assertThat(topologyEntry.getDevices()).containsOnly(device);
    }

    @Test
    public void testAddMultipleDevicesToEmptyTopologyWithVarargs () {
        Range<Instant> range = Range.all();
        CompleteTopologyTimesliceImpl topologyEntry = new CompleteTopologyTimesliceImpl(range);
        Device device1 = mock(Device.class);
        when(device1.getId()).thenReturn(1L);
        Device device2 = mock(Device.class);
        when(device2.getId()).thenReturn(2L);

        // Business method
        topologyEntry.add(device1, device2);

        // Asserts
        assertThat(topologyEntry.getPeriod()).isEqualTo(range);
        assertThat(topologyEntry.getDevices()).containsOnly(device1, device2);
    }

    @Test
    public void testAddMultipleDevicesToEmptyTopologyAsList () {
        Range<Instant> range = Range.all();
        CompleteTopologyTimesliceImpl topologyEntry = new CompleteTopologyTimesliceImpl(range);
        Device device1 = mock(Device.class);
        when(device1.getId()).thenReturn(1L);
        Device device2 = mock(Device.class);
        when(device2.getId()).thenReturn(2L);

        // Business method
        topologyEntry.addAll(Arrays.asList(device1, device2));

        // Asserts
        assertThat(topologyEntry.getPeriod()).isEqualTo(range);
        assertThat(topologyEntry.getDevices()).containsOnly(device1, device2);
    }

    @Test
    public void testAddDeviceWithVarargs () {
        Range<Instant> range = Range.all();
        Device device1 = mock(Device.class);
        when(device1.getId()).thenReturn(1L);
        Device device2 = mock(Device.class);
        when(device2.getId()).thenReturn(2L);
        CompleteTopologyTimesliceImpl topologyEntry = new CompleteTopologyTimesliceImpl(range, device1, device2);
        Device extraDevice = mock(Device.class);
        when(extraDevice.getId()).thenReturn(3L);

        // Business method
        topologyEntry.add(extraDevice);

        // Asserts
        assertThat(topologyEntry.getPeriod()).isEqualTo(range);
        assertThat(topologyEntry.getDevices()).containsOnly(device1, device2, extraDevice);
    }

    @Test
    public void testAddDeviceAsList () {
        Range<Instant> range = Range.all();
        Device device1 = mock(Device.class);
        when(device1.getId()).thenReturn(1L);
        Device device2 = mock(Device.class);
        when(device2.getId()).thenReturn(2L);
        CompleteTopologyTimesliceImpl topologyEntry = new CompleteTopologyTimesliceImpl(range, device1, device2);
        Device extraDevice = mock(Device.class);
        when(extraDevice.getId()).thenReturn(3L);

        // Business method
        topologyEntry.addAll(Arrays.asList(extraDevice));

        // Asserts
        assertThat(topologyEntry.getPeriod()).isEqualTo(range);
        assertThat(topologyEntry.getDevices()).containsOnly(device1, device2, extraDevice);
    }

    @Test
    public void testAddMultipleDevicesWithVarargs () {
        Range<Instant> range = Range.all();
        Device device1 = mock(Device.class);
        when(device1.getId()).thenReturn(1L);
        Device device2 = mock(Device.class);
        when(device2.getId()).thenReturn(2L);
        CompleteTopologyTimesliceImpl topologyEntry = new CompleteTopologyTimesliceImpl(range, device1, device2);
        Device extraDevice1 = mock(Device.class);
        when(extraDevice1.getId()).thenReturn(3L);
        Device extraDevice2 = mock(Device.class);
        when(extraDevice2.getId()).thenReturn(4L);

        // Business method
        topologyEntry.add(extraDevice1, extraDevice2);

        // Asserts
        assertThat(topologyEntry.getPeriod()).isEqualTo(range);
        assertThat(topologyEntry.getDevices()).containsOnly(device1, device2, extraDevice1, extraDevice2);
    }

    @Test
    public void testAddMultipleDevicesAsList () {
        Range<Instant> range = Range.all();
        Device device1 = mock(Device.class);
        when(device1.getId()).thenReturn(1L);
        Device device2 = mock(Device.class);
        when(device2.getId()).thenReturn(2L);
        CompleteTopologyTimesliceImpl topologyEntry = new CompleteTopologyTimesliceImpl(range, device1, device2);
        Device extraDevice1 = mock(Device.class);
        when(extraDevice1.getId()).thenReturn(3L);
        Device extraDevice2 = mock(Device.class);
        when(extraDevice2.getId()).thenReturn(4L);

        // Business method
        topologyEntry.addAll(Arrays.asList(extraDevice1, extraDevice2));

        // Asserts
        assertThat(topologyEntry.getPeriod()).isEqualTo(range);
        assertThat(topologyEntry.getDevices()).containsOnly(device1, device2, extraDevice1, extraDevice2);
    }

}
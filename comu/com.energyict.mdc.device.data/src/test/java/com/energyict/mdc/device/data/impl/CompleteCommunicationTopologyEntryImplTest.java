package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.util.time.Interval;
import com.energyict.mdc.device.data.Device;
import java.util.Arrays;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link CompleteCommunicationTopologyEntryImpl} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-06-03 (14:01
 */
public class CompleteCommunicationTopologyEntryImplTest {

    @Test
    public void testVarargsConstructor () {
        Device device1 = mock(Device.class);
        when(device1.getId()).thenReturn(1L);
        Device device2 = mock(Device.class);
        when(device2.getId()).thenReturn(2L);
        Interval interval = Interval.sinceEpoch();

        // Business method
        CompleteCommunicationTopologyEntryImpl topologyEntry = new CompleteCommunicationTopologyEntryImpl(interval, device1, device2);

        // Asserts
        assertThat(topologyEntry.getInterval()).isEqualTo(interval);
        assertThat(topologyEntry.getDevices()).containsOnly(device1, device2);
    }

    @Test
    public void testListConstructor () {
        Device device1 = mock(Device.class);
        when(device1.getId()).thenReturn(1L);
        Device device2 = mock(Device.class);
        when(device2.getId()).thenReturn(2L);
        Interval interval = Interval.sinceEpoch();

        // Business method
        CompleteCommunicationTopologyEntryImpl topologyEntry = new CompleteCommunicationTopologyEntryImpl(interval, Arrays.asList(device1, device2));

        // Asserts
        assertThat(topologyEntry.getInterval()).isEqualTo(interval);
        assertThat(topologyEntry.getDevices()).containsOnly(device1, device2);
    }

    @Test
    public void testAddDeviceToEmptyTopologyWithVarargs () {
        Interval interval = Interval.sinceEpoch();
        CompleteCommunicationTopologyEntryImpl topologyEntry = new CompleteCommunicationTopologyEntryImpl(interval);
        Device device = mock(Device.class);
        when(device.getId()).thenReturn(1L);

        // Business method
        topologyEntry.add(device);

        // Asserts
        assertThat(topologyEntry.getInterval()).isEqualTo(interval);
        assertThat(topologyEntry.getDevices()).containsOnly(device);
    }

    @Test
    public void testAddDeviceToEmptyTopologyAsList () {
        Interval interval = Interval.sinceEpoch();
        CompleteCommunicationTopologyEntryImpl topologyEntry = new CompleteCommunicationTopologyEntryImpl(interval);
        Device device = mock(Device.class);
        when(device.getId()).thenReturn(1L);

        // Business method
        topologyEntry.addAll(Arrays.asList(device));

        // Asserts
        assertThat(topologyEntry.getInterval()).isEqualTo(interval);
        assertThat(topologyEntry.getDevices()).containsOnly(device);
    }

    @Test
    public void testAddMultipleDevicesToEmptyTopologyWithVarargs () {
        Interval interval = Interval.sinceEpoch();
        CompleteCommunicationTopologyEntryImpl topologyEntry = new CompleteCommunicationTopologyEntryImpl(interval);
        Device device1 = mock(Device.class);
        when(device1.getId()).thenReturn(1L);
        Device device2 = mock(Device.class);
        when(device2.getId()).thenReturn(2L);

        // Business method
        topologyEntry.add(device1, device2);

        // Asserts
        assertThat(topologyEntry.getInterval()).isEqualTo(interval);
        assertThat(topologyEntry.getDevices()).containsOnly(device1, device2);
    }

    @Test
    public void testAddMultipleDevicesToEmptyTopologyAsList () {
        Interval interval = Interval.sinceEpoch();
        CompleteCommunicationTopologyEntryImpl topologyEntry = new CompleteCommunicationTopologyEntryImpl(interval);
        Device device1 = mock(Device.class);
        when(device1.getId()).thenReturn(1L);
        Device device2 = mock(Device.class);
        when(device2.getId()).thenReturn(2L);

        // Business method
        topologyEntry.addAll(Arrays.asList(device1, device2));

        // Asserts
        assertThat(topologyEntry.getInterval()).isEqualTo(interval);
        assertThat(topologyEntry.getDevices()).containsOnly(device1, device2);
    }

    @Test
    public void testAddDeviceWithVarargs () {
        Interval interval = Interval.sinceEpoch();
        Device device1 = mock(Device.class);
        when(device1.getId()).thenReturn(1L);
        Device device2 = mock(Device.class);
        when(device2.getId()).thenReturn(2L);
        CompleteCommunicationTopologyEntryImpl topologyEntry = new CompleteCommunicationTopologyEntryImpl(interval, device1, device2);
        Device extraDevice = mock(Device.class);
        when(extraDevice.getId()).thenReturn(3L);

        // Business method
        topologyEntry.add(extraDevice);

        // Asserts
        assertThat(topologyEntry.getInterval()).isEqualTo(interval);
        assertThat(topologyEntry.getDevices()).containsOnly(device1, device2, extraDevice);
    }

    @Test
    public void testAddDeviceAsList () {
        Interval interval = Interval.sinceEpoch();
        Device device1 = mock(Device.class);
        when(device1.getId()).thenReturn(1L);
        Device device2 = mock(Device.class);
        when(device2.getId()).thenReturn(2L);
        CompleteCommunicationTopologyEntryImpl topologyEntry = new CompleteCommunicationTopologyEntryImpl(interval, device1, device2);
        Device extraDevice = mock(Device.class);
        when(extraDevice.getId()).thenReturn(3L);

        // Business method
        topologyEntry.addAll(Arrays.asList(extraDevice));

        // Asserts
        assertThat(topologyEntry.getInterval()).isEqualTo(interval);
        assertThat(topologyEntry.getDevices()).containsOnly(device1, device2, extraDevice);
    }

    @Test
    public void testAddMultipleDevicesWithVarargs () {
        Interval interval = Interval.sinceEpoch();
        Device device1 = mock(Device.class);
        when(device1.getId()).thenReturn(1L);
        Device device2 = mock(Device.class);
        when(device2.getId()).thenReturn(2L);
        CompleteCommunicationTopologyEntryImpl topologyEntry = new CompleteCommunicationTopologyEntryImpl(interval, device1, device2);
        Device extraDevice1 = mock(Device.class);
        when(extraDevice1.getId()).thenReturn(3L);
        Device extraDevice2 = mock(Device.class);
        when(extraDevice2.getId()).thenReturn(4L);

        // Business method
        topologyEntry.add(extraDevice1, extraDevice2);

        // Asserts
        assertThat(topologyEntry.getInterval()).isEqualTo(interval);
        assertThat(topologyEntry.getDevices()).containsOnly(device1, device2, extraDevice1, extraDevice2);
    }

    @Test
    public void testAddMultipleDevicesAsList () {
        Interval interval = Interval.sinceEpoch();
        Device device1 = mock(Device.class);
        when(device1.getId()).thenReturn(1L);
        Device device2 = mock(Device.class);
        when(device2.getId()).thenReturn(2L);
        CompleteCommunicationTopologyEntryImpl topologyEntry = new CompleteCommunicationTopologyEntryImpl(interval, device1, device2);
        Device extraDevice1 = mock(Device.class);
        when(extraDevice1.getId()).thenReturn(3L);
        Device extraDevice2 = mock(Device.class);
        when(extraDevice2.getId()).thenReturn(4L);

        // Business method
        topologyEntry.addAll(Arrays.asList(extraDevice1, extraDevice2));

        // Asserts
        assertThat(topologyEntry.getInterval()).isEqualTo(interval);
        assertThat(topologyEntry.getDevices()).containsOnly(device1, device2, extraDevice1, extraDevice2);
    }

}
package com.energyict.mdc.device.data.impl;

import com.energyict.mdc.device.data.Device;

import com.elster.jupiter.util.time.Interval;
import org.joda.time.DateMidnight;

import org.junit.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link CommunicationTopologyImpl} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-06-03 (11:00)
 */
public class CommunicationTopologyImplTest {

    private static final Interval JAN_2014 = new Interval(new DateMidnight(2014, 1, 1).toDate(), new DateMidnight(2014, 2, 1).toDate());

    @Test
    public void testConstructor () {
        Device device = mock(Device.class);

        // Business method
        CommunicationTopologyImpl communicationTopology = new CommunicationTopologyImpl(device, JAN_2014);

        // Asserts
        assertThat(communicationTopology.getInterval()).isEqualTo(JAN_2014);
        assertThat(communicationTopology.getRoot()).isEqualTo(device);
        assertThat(communicationTopology.getChildren()).isEmpty();
        assertThat(communicationTopology.getDevices()).isEmpty();
        assertThat(communicationTopology.isLeaf()).isTrue();
    }

    @Test
    public void testAddOneChild () {
        Device device1 = mock(Device.class);
        Device device2 = mock(Device.class);
        CommunicationTopologyImpl communicationTopology = new CommunicationTopologyImpl(device1, JAN_2014);
        CommunicationTopology childTopology = mock(CommunicationTopology.class);
        when(childTopology.getRoot()).thenReturn(device2);

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
        CommunicationTopologyImpl communicationTopology = new CommunicationTopologyImpl(device1, JAN_2014);
        CommunicationTopology childTopology1 = mock(CommunicationTopology.class);
        when(childTopology1.getRoot()).thenReturn(device2);
        CommunicationTopology childTopology2 = mock(CommunicationTopology.class);
        when(childTopology2.getRoot()).thenReturn(device3);

        // Business method
        communicationTopology.addChild(childTopology1);
        communicationTopology.addChild(childTopology2);

        // Asserts
        assertThat(communicationTopology.getChildren()).containsOnly(childTopology1, childTopology2);
        assertThat(communicationTopology.isLeaf()).isFalse();
        assertThat(communicationTopology.getDevices()).containsOnly(device2, device3);
    }

}
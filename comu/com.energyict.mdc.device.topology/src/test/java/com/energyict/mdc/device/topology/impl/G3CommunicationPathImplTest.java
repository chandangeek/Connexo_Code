package com.energyict.mdc.device.topology.impl;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.topology.G3CommunicationPathSegment;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.*;
import org.junit.runner.*;

import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link G3CommunicationPathImpl} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-12-08 (15:32)
 */
@RunWith(MockitoJUnitRunner.class)
public class G3CommunicationPathImplTest {

    @Mock
    private Device source;
    @Mock
    private Device target;

    @Test
    public void testEmptyPathReturnsZeroHops() {
        G3CommunicationPathImpl communicationPath = new G3CommunicationPathImpl(this.source, this.target);

        // Business method
        int numberOfHops = communicationPath.getNumberOfHops();

        // Asserts
        assertThat(numberOfHops).isEqualTo(0);
    }

    @Test
    public void testEmptyPathReturnsZeroIntermediateDevices() {
        G3CommunicationPathImpl communicationPath = new G3CommunicationPathImpl(this.source, this.target);

        // Business method
        List<Device> intermediateDevices = communicationPath.getIntermediateDevices();

        // Asserts
        assertThat(intermediateDevices).isEmpty();
    }

    @Test
    public void testImmediatePathReturnsZeroHops() {
        G3CommunicationPathSegment segment = mock(G3CommunicationPathSegment.class);
        when(segment.getSource()).thenReturn(this.source);
        when(segment.getTarget()).thenReturn(this.target);
        when(segment.getNextHopDevice()).thenReturn(Optional.<Device>empty());
        G3CommunicationPathImpl communicationPath = new G3CommunicationPathImpl(this.source, this.target, Arrays.asList(segment));

        // Business method
        int numberOfHops = communicationPath.getNumberOfHops();

        // Asserts
        assertThat(numberOfHops).isEqualTo(0);
    }

    @Test
    public void testImmediatePathReturnsZeroIntermediateDevices() {
        G3CommunicationPathSegment segment = mock(G3CommunicationPathSegment.class);
        when(segment.getSource()).thenReturn(this.source);
        when(segment.getTarget()).thenReturn(this.target);
        when(segment.getNextHopDevice()).thenReturn(Optional.<Device>empty());
        G3CommunicationPathImpl communicationPath = new G3CommunicationPathImpl(this.source, this.target, Arrays.asList(segment));

        // Business method
        List<Device> intermediateDevices = communicationPath.getIntermediateDevices();

        // Asserts
        assertThat(intermediateDevices).isEmpty();
    }

    @Test
    public void testNumberOfHops() {
        Device intermediate1 = mock(Device.class);
        Device intermediate2 = mock(Device.class);
        G3CommunicationPathSegment segment1 = mock(G3CommunicationPathSegment.class);
        when(segment1.getSource()).thenReturn(this.source);
        when(segment1.getTarget()).thenReturn(this.target);
        when(segment1.getNextHopDevice()).thenReturn(Optional.of(intermediate1));
        G3CommunicationPathSegment segment2 = mock(G3CommunicationPathSegment.class);
        when(segment2.getSource()).thenReturn(intermediate1);
        when(segment2.getTarget()).thenReturn(this.target);
        when(segment2.getNextHopDevice()).thenReturn(Optional.of(intermediate2));
        G3CommunicationPathSegment segment3 = mock(G3CommunicationPathSegment.class);
        when(segment3.getSource()).thenReturn(intermediate2);
        when(segment3.getTarget()).thenReturn(this.target);
        when(segment3.getNextHopDevice()).thenReturn(Optional.<Device>empty());
        G3CommunicationPathImpl communicationPath = new G3CommunicationPathImpl(this.source, this.target, Arrays.asList(segment1, segment2, segment3));

        // Business method
        int numberOfHops = communicationPath.getNumberOfHops();

        // Asserts
        assertThat(numberOfHops).isEqualTo(2);
    }

    @Test
    public void testIntermediateDevices() {
        Device intermediate1 = mock(Device.class);
        Device intermediate2 = mock(Device.class);
        G3CommunicationPathSegment segment1 = mock(G3CommunicationPathSegment.class);
        when(segment1.getSource()).thenReturn(this.source);
        when(segment1.getTarget()).thenReturn(this.target);
        when(segment1.getNextHopDevice()).thenReturn(Optional.of(intermediate1));
        G3CommunicationPathSegment segment2 = mock(G3CommunicationPathSegment.class);
        when(segment2.getSource()).thenReturn(intermediate1);
        when(segment2.getTarget()).thenReturn(this.target);
        when(segment2.getNextHopDevice()).thenReturn(Optional.of(intermediate2));
        G3CommunicationPathSegment segment3 = mock(G3CommunicationPathSegment.class);
        when(segment3.getSource()).thenReturn(intermediate2);
        when(segment3.getTarget()).thenReturn(this.target);
        when(segment3.getNextHopDevice()).thenReturn(Optional.<Device>empty());
        G3CommunicationPathImpl communicationPath = new G3CommunicationPathImpl(this.source, this.target, Arrays.asList(segment1, segment2, segment3));

        // Business method
        List<Device> intermediateDevices = communicationPath.getIntermediateDevices();

        // Asserts
        assertThat(intermediateDevices).hasSize(2);
        assertThat(intermediateDevices).containsOnly(intermediate1, intermediate2);
    }

    @Test
    public void testAddSegmentIncreasesNumberOfHops() {
        Device intermediate1 = mock(Device.class);
        Device intermediate2 = mock(Device.class);
        G3CommunicationPathSegment segment1 = mock(G3CommunicationPathSegment.class);
        when(segment1.getSource()).thenReturn(this.source);
        when(segment1.getTarget()).thenReturn(this.target);
        when(segment1.getNextHopDevice()).thenReturn(Optional.of(intermediate1));
        G3CommunicationPathSegment segment2 = mock(G3CommunicationPathSegment.class);
        when(segment2.getSource()).thenReturn(intermediate1);
        when(segment2.getTarget()).thenReturn(this.target);
        when(segment2.getNextHopDevice()).thenReturn(Optional.of(intermediate2));
        G3CommunicationPathSegment segment3 = mock(G3CommunicationPathSegment.class);
        when(segment3.getSource()).thenReturn(intermediate2);
        when(segment3.getTarget()).thenReturn(this.target);
        when(segment3.getNextHopDevice()).thenReturn(Optional.<Device>empty());
        G3CommunicationPathImpl communicationPath = new G3CommunicationPathImpl(this.source, this.target, Arrays.asList(segment1, segment2));
        assertThat(communicationPath.getNumberOfHops()).isEqualTo(1);

        // Business method
        communicationPath.addSegment(segment3);
        int numberOfHops = communicationPath.getNumberOfHops();

        // Asserts
        assertThat(numberOfHops).isEqualTo(2);
    }

    @Test
    public void testAddSegmentReturnsAdditionalIntermediateDevice() {
        Device intermediate1 = mock(Device.class);
        Device intermediate2 = mock(Device.class);
        G3CommunicationPathSegment segment1 = mock(G3CommunicationPathSegment.class);
        when(segment1.getSource()).thenReturn(this.source);
        when(segment1.getTarget()).thenReturn(this.target);
        when(segment1.getNextHopDevice()).thenReturn(Optional.of(intermediate1));
        G3CommunicationPathSegment segment2 = mock(G3CommunicationPathSegment.class);
        when(segment2.getSource()).thenReturn(intermediate1);
        when(segment2.getTarget()).thenReturn(this.target);
        when(segment2.getNextHopDevice()).thenReturn(Optional.of(intermediate2));
        G3CommunicationPathSegment segment3 = mock(G3CommunicationPathSegment.class);
        when(segment3.getSource()).thenReturn(intermediate2);
        when(segment3.getTarget()).thenReturn(this.target);
        when(segment3.getNextHopDevice()).thenReturn(Optional.<Device>empty());
        G3CommunicationPathImpl communicationPath = new G3CommunicationPathImpl(this.source, this.target, Arrays.asList(segment1, segment2));
        List<Device> intermediateDevices = communicationPath.getIntermediateDevices();

        // Prologue asserts
        assertThat(intermediateDevices).hasSize(2);
        assertThat(intermediateDevices).containsOnly(intermediate1, intermediate2);

        // Business method
        communicationPath.addSegment(segment3);
        List<Device> moreIntermediateDevices = communicationPath.getIntermediateDevices();

        // Asserts
        assertThat(moreIntermediateDevices).hasSize(2);
        assertThat(moreIntermediateDevices).containsOnly(intermediate1, intermediate2);
    }

}
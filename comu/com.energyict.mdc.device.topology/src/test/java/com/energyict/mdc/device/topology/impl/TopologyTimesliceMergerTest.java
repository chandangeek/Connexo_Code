package com.energyict.mdc.device.topology.impl;

import com.elster.jupiter.util.time.Interval;
import com.energyict.mdc.device.data.Device;

import com.google.common.collect.Range;
import org.joda.time.DateMidnight;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link TopologyTimesliceMerger} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-06-04 (10:47)
 */
@RunWith(MockitoJUnitRunner.class)
public class TopologyTimesliceMergerTest {

    private static final long DEVICE1_ID = 97;
    private static final long DEVICE2_ID = 101;

    private static final Range<Instant> JAN_2014 = Range.closed(new DateMidnight(2014, 1, 1).toDate().toInstant(), new DateMidnight(2014, 2, 1).toDate().toInstant());
    private static final Range<Instant> FIRST_HALF_JAN_2014 = Range.closed(new DateMidnight(2014, 1, 1).toDate().toInstant(), new DateMidnight(2014, 1, 16).toDate().toInstant());
    private static final Range<Instant> SECOND_HALF_JAN_2014 = Range.closed(new DateMidnight(2014, 1, 16).toDate().toInstant(), new DateMidnight(2014, 2, 1).toDate().toInstant());
    private static final Range<Instant> MID_JAN_2014 =Range.closed(new DateMidnight(2014, 1, 10).toDate().toInstant(), new DateMidnight(2014, 1, 20).toDate().toInstant());
    private static final Range<Instant> MARCH_2014 = Range.closed(new DateMidnight(2014, 3, 1).toDate().toInstant(), new DateMidnight(2014, 4, 1).toDate().toInstant());
    private static final Range<Instant> MID_FEB_TO_MID_MARCH_2014 = Range.closed(new DateMidnight(2014, 2, 16).toDate().toInstant(), new DateMidnight(2014, 3, 16).toDate().toInstant());
    private static final Range<Instant> MID_MARCH_TO_MID_APRIL_2014 = Range.closed(new DateMidnight(2014, 3, 16).toDate().toInstant(), new DateMidnight(2014, 4, 16).toDate().toInstant());

    @Mock
    private Device device1;
    @Mock
    private Device device2;

    @Before
    public void mockDevices () {
        when(this.device1.getId()).thenReturn(DEVICE1_ID);
        when(this.device2.getId()).thenReturn(DEVICE2_ID);
    }

    @Test
    public void testWithoutMerging () {
        TopologyTimesliceMerger merger = new TopologyTimesliceMerger();

        // Business method
        List<CompleteTopologyTimesliceImpl> entries = merger.getEntries();

        // Asserts
        assertThat(entries).isEmpty();
    }

    @Test
    public void testAddOneEntry () {
        TopologyTimesliceMerger merger = new TopologyTimesliceMerger();

        // Business method
        merger.add(this.newEntry(JAN_2014, this.device1, this.device2));

        // Asserts
        List<CompleteTopologyTimesliceImpl> entries = merger.getEntries();
        assertThat(entries).hasSize(1);
        CompleteTopologyTimesliceImpl actualEntry = entries.get(0);
        assertThat(actualEntry.getPeriod()).isEqualTo(JAN_2014);
        assertDeviceIds(actualEntry.getDevices(), DEVICE1_ID, DEVICE2_ID);
    }

    @Test
    public void testAddEntryWithNonIntersectingInterval () {
        TopologyTimesliceMerger merger = new TopologyTimesliceMerger();
        merger.add(this.newEntry(JAN_2014, this.device1));

        // Business method
        merger.add(this.newEntry(MARCH_2014, this.device2));

        // Asserts
        List<CompleteTopologyTimesliceImpl> entries = merger.getEntries();
        assertThat(entries).hasSize(2);
        for (CompleteTopologyTimesliceImpl entry : entries) {
            if (entry.getPeriod().equals(JAN_2014)) {
                assertDeviceIds(entry.getDevices(), DEVICE1_ID);
            }
            else if (entry.getPeriod().equals(MARCH_2014)) {
                assertDeviceIds(entry.getDevices(), DEVICE2_ID);
            }
            else {
                fail("Unexpected Interval: " + entry.getPeriod());
            }
        }
    }

    @Test
    public void testAddEntryWithSameInterval () {
        TopologyTimesliceMerger merger = new TopologyTimesliceMerger();
        merger.add(this.newEntry(JAN_2014, this.device1));

        // Business method
        merger.add(this.newEntry(JAN_2014, this.device2));

        // Asserts
        List<CompleteTopologyTimesliceImpl> entries = merger.getEntries();
        assertThat(entries).hasSize(1);
        CompleteTopologyTimesliceImpl actualEntry = entries.get(0);
        assertThat(actualEntry.getPeriod()).isEqualTo(JAN_2014);
        assertDeviceIds(actualEntry.getDevices(), DEVICE1_ID, DEVICE2_ID);
    }

    @Test
    public void testAddEntryWithEnvelopedInterval () {
        TopologyTimesliceMerger merger = new TopologyTimesliceMerger();
        merger.add(this.newEntry(JAN_2014, this.device1));

        // Business method
        merger.add(this.newEntry(MID_JAN_2014, this.device2));

        // Asserts
        List<CompleteTopologyTimesliceImpl> entries = merger.getEntries();
        assertThat(entries).hasSize(3);
        for (CompleteTopologyTimesliceImpl entry : entries) {
            if (entry.getPeriod().lowerEndpoint().equals(JAN_2014.lowerEndpoint())) {
                assertDeviceIds(entry.getDevices(), DEVICE1_ID);
            }
            else if (entry.getPeriod().lowerEndpoint().equals(MID_JAN_2014.lowerEndpoint())) {
                assertDeviceIds(entry.getDevices(), DEVICE1_ID, DEVICE2_ID);
            }
            else if (entry.getPeriod().lowerEndpoint().equals(MID_JAN_2014.upperEndpoint())) {
                assertDeviceIds(entry.getDevices(), DEVICE1_ID);
            }
            else {
                fail("Unexpected Interval: " + entry.getPeriod());
            }
        }
    }

    @Test
    public void testAddEntryWithIntervalThatAbutAtEnd () {
        TopologyTimesliceMerger merger = new TopologyTimesliceMerger();
        merger.add(this.newEntry(JAN_2014, this.device1));

        // Business method
        merger.add(this.newEntry(SECOND_HALF_JAN_2014, this.device2));

        // Asserts
        List<CompleteTopologyTimesliceImpl> entries = merger.getEntries();
        assertThat(entries).hasSize(2);
        for (CompleteTopologyTimesliceImpl entry : entries) {
            if (entry.getPeriod().equals(FIRST_HALF_JAN_2014)) {
                assertDeviceIds(entry.getDevices(), DEVICE1_ID);
            }
            else if (entry.getPeriod().equals(SECOND_HALF_JAN_2014)) {
                assertDeviceIds(entry.getDevices(), DEVICE1_ID, DEVICE2_ID);
            }
            else {
                fail("Unexpected Interval: " + entry.getPeriod());
            }
        }
    }

    @Test
    public void testAddEntryWithIntervalThatAbutAtStart () {
        TopologyTimesliceMerger merger = new TopologyTimesliceMerger();
        merger.add(this.newEntry(JAN_2014, this.device1));

        // Business method
        merger.add(this.newEntry(FIRST_HALF_JAN_2014, this.device2));

        // Asserts
        List<CompleteTopologyTimesliceImpl> entries = merger.getEntries();
        assertThat(entries).hasSize(2);
        for (CompleteTopologyTimesliceImpl entry : entries) {
            if (entry.getPeriod().equals(SECOND_HALF_JAN_2014)) {
                assertDeviceIds(entry.getDevices(), DEVICE1_ID);
            }
            else if (entry.getPeriod().equals(FIRST_HALF_JAN_2014)) {
                assertDeviceIds(entry.getDevices(), DEVICE1_ID, DEVICE2_ID);
            }
            else {
                fail("Unexpected Interval: " + entry.getPeriod());
            }
        }
    }

    @Test
    public void testMergeTwoHalfClosedHalfOpenSlices() throws Exception {
        LocalDateTime now = LocalDateTime.now();
        SimpleTopologyTimesliceImpl timeslice1 = new SimpleTopologyTimesliceImpl(device1, Interval.startAt(now.toInstant(ZoneOffset.UTC)));
        SimpleTopologyTimesliceImpl timeslice2 = new SimpleTopologyTimesliceImpl(device2, Interval.startAt(now.minusDays(1).toInstant(ZoneOffset.UTC)));
        TopologyTimelineImpl.merge(Arrays.asList(timeslice1, timeslice2));
    }

    @Test
    public void testMergeThreeHalfClosedHalfOpenSlices() throws Exception {
        Device device3 = mock(Device.class);
        LocalDateTime now = LocalDateTime.now();
        SimpleTopologyTimesliceImpl timeslice1 = new SimpleTopologyTimesliceImpl(device1, Interval.of(now.toInstant(ZoneOffset.UTC), now.plusWeeks(1).toInstant(ZoneOffset.UTC)));
        SimpleTopologyTimesliceImpl timeslice2 = new SimpleTopologyTimesliceImpl(device2, Interval.of(now.minusDays(1).toInstant(ZoneOffset.UTC), now.plusWeeks(1).toInstant(ZoneOffset.UTC)));
        SimpleTopologyTimesliceImpl timeslice3 = new SimpleTopologyTimesliceImpl(device3, Interval.of(now.minusDays(1).toInstant(ZoneOffset.UTC), now.plusWeeks(1).toInstant(ZoneOffset.UTC)));
        TopologyTimelineImpl.merge(Arrays.asList(timeslice1, timeslice2, timeslice3));
    }


    @Test
    public void testAddEntryWithIntervalThatOverlapsAtStart () {
        TopologyTimesliceMerger merger = new TopologyTimesliceMerger();
        merger.add(this.newEntry(MARCH_2014, this.device1));

        // Business method
        merger.add(this.newEntry(MID_FEB_TO_MID_MARCH_2014, this.device2));

        // Asserts
        List<CompleteTopologyTimesliceImpl> entries = merger.getEntries();
        assertThat(entries).hasSize(3);
        for (CompleteTopologyTimesliceImpl entry : entries) {
            if (entry.getPeriod().lowerEndpoint().equals(MID_FEB_TO_MID_MARCH_2014.lowerEndpoint())) {
                assertDeviceIds(entry.getDevices(), DEVICE2_ID);
            }
            else if (entry.getPeriod().lowerEndpoint().equals(MARCH_2014.lowerEndpoint())) {
                assertDeviceIds(entry.getDevices(), DEVICE1_ID, DEVICE2_ID);
            }
            else if (entry.getPeriod().lowerEndpoint().equals(MID_FEB_TO_MID_MARCH_2014.upperEndpoint())) {
                assertDeviceIds(entry.getDevices(), DEVICE1_ID);
            }
            else {
                fail("Unexpected Interval: " + entry.getPeriod());
            }
        }
    }

    @Test
    public void testAddEntryWithIntervalThatOverlapsAtEnd () {
        TopologyTimesliceMerger merger = new TopologyTimesliceMerger();
        merger.add(this.newEntry(MARCH_2014, this.device1));

        // Business method
        merger.add(this.newEntry(MID_MARCH_TO_MID_APRIL_2014, this.device2));

        // Asserts
        List<CompleteTopologyTimesliceImpl> entries = merger.getEntries();
        assertThat(entries).hasSize(3);
        for (CompleteTopologyTimesliceImpl entry : entries) {
            if (entry.getPeriod().lowerEndpoint().equals(MARCH_2014.lowerEndpoint())) {
                assertDeviceIds(entry.getDevices(), DEVICE1_ID);
            }
            else if (entry.getPeriod().lowerEndpoint().equals(MID_MARCH_TO_MID_APRIL_2014.lowerEndpoint())) {
                assertDeviceIds(entry.getDevices(), DEVICE1_ID, DEVICE2_ID);
            }
            else if (entry.getPeriod().lowerEndpoint().equals(MARCH_2014.upperEndpoint())) {
                assertDeviceIds(entry.getDevices(), DEVICE2_ID);
            }
            else {
                fail("Unexpected Interval: " + entry.getPeriod());
            }
        }
    }

    @Test
    public void addEntryWithEnvelopedOverlapPart2Test() {
        TopologyTimesliceMerger merger = new TopologyTimesliceMerger();
        merger.add(this.newEntry(MID_JAN_2014, this.device1));
        merger.add(this.newEntry(JAN_2014, this.device2));

        // Asserts
        List<CompleteTopologyTimesliceImpl> entries = merger.getEntries();
        assertThat(entries).hasSize(3);
        for (CompleteTopologyTimesliceImpl entry : entries) {
            if (entry.getPeriod().lowerEndpoint().equals(JAN_2014.lowerEndpoint())) {
                assertDeviceIds(entry.getDevices(), DEVICE2_ID);
            } else if (entry.getPeriod().lowerEndpoint().equals(MID_JAN_2014.lowerEndpoint())) {
                assertDeviceIds(entry.getDevices(), DEVICE1_ID, DEVICE2_ID);
            } else if (entry.getPeriod().lowerEndpoint().equals(MID_JAN_2014.upperEndpoint())) {
                assertDeviceIds(entry.getDevices(), DEVICE2_ID);
            } else {
                fail("Unexpected Interval: " + entry.getPeriod());
            }
        }
    }

    private CompleteTopologyTimesliceImpl newEntry(Range range, Device... devices) {
        return new CompleteTopologyTimesliceImpl(range, devices);
    }

    private void assertDeviceIds (List<Device> devices, Long... expectedDeviceIds) {
        Set<Long> actualIds = new HashSet<>();
        for (Device slave : devices) {
            actualIds.add(slave.getId());
        }
        assertThat(actualIds).containsOnly(expectedDeviceIds);
    }

}
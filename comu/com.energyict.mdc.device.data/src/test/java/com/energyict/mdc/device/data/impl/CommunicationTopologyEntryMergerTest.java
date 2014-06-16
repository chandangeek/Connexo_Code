package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.util.time.Interval;
import com.energyict.mdc.device.data.Device;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.joda.time.DateMidnight;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link CommunicationTopologyEntryMerger} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-06-04 (10:47)
 */
@RunWith(MockitoJUnitRunner.class)
public class CommunicationTopologyEntryMergerTest {

    private static final long DEVICE1_ID = 97;
    private static final long DEVICE2_ID = 101;

    private static final Interval JAN_2014 = new Interval(new DateMidnight(2014, 1, 1).toDate(), new DateMidnight(2014, 2, 1).toDate());
    private static final Interval FIRST_HALF_JAN_2014 = new Interval(new DateMidnight(2014, 1, 1).toDate(), new DateMidnight(2014, 1, 16).toDate());
    private static final Interval SECOND_HALF_JAN_2014 = new Interval(new DateMidnight(2014, 1, 16).toDate(), new DateMidnight(2014, 2, 1).toDate());
    private static final Interval MID_JAN_2014 = new Interval(new DateMidnight(2014, 1, 10).toDate(), new DateMidnight(2014, 1, 20).toDate());
    private static final Interval MARCH_2014 = new Interval(new DateMidnight(2014, 3, 1).toDate(), new DateMidnight(2014, 4, 1).toDate());
    private static final Interval MID_FEB_TO_MID_MARCH_2014 = new Interval(new DateMidnight(2014, 2, 16).toDate(), new DateMidnight(2014, 3, 16).toDate());
    private static final Interval MID_MARCH_TO_MID_APRIL_2014 = new Interval(new DateMidnight(2014, 3, 16).toDate(), new DateMidnight(2014, 4, 16).toDate());

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
        CommunicationTopologyEntryMerger merger = new CommunicationTopologyEntryMerger();

        // Business method
        List<CompleteCommunicationTopologyEntryImpl> entries = merger.getEntries();

        // Asserts
        assertThat(entries).isEmpty();
    }

    @Test
    public void testAddOneEntry () {
        CommunicationTopologyEntryMerger merger = new CommunicationTopologyEntryMerger();

        // Business method
        merger.add(this.newEntry(JAN_2014, this.device1, this.device2));

        // Asserts
        List<CompleteCommunicationTopologyEntryImpl> entries = merger.getEntries();
        assertThat(entries).hasSize(1);
        CompleteCommunicationTopologyEntryImpl actualEntry = entries.get(0);
        assertThat(actualEntry.getInterval()).isEqualTo(JAN_2014);
        assertDeviceIds(actualEntry.getDevices(), DEVICE1_ID, DEVICE2_ID);
    }

    @Test
    public void testAddEntryWithNonIntersectingInterval () {
        CommunicationTopologyEntryMerger merger = new CommunicationTopologyEntryMerger();
        merger.add(this.newEntry(JAN_2014, this.device1));

        // Business method
        merger.add(this.newEntry(MARCH_2014, this.device2));

        // Asserts
        List<CompleteCommunicationTopologyEntryImpl> entries = merger.getEntries();
        assertThat(entries).hasSize(2);
        for (CompleteCommunicationTopologyEntryImpl entry : entries) {
            if (entry.getInterval().equals(JAN_2014)) {
                assertDeviceIds(entry.getDevices(), DEVICE1_ID);
            }
            else if (entry.getInterval().equals(MARCH_2014)) {
                assertDeviceIds(entry.getDevices(), DEVICE2_ID);
            }
            else {
                fail("Unexpected Interval: " + entry.getInterval());
            }
        }
    }

    @Test
    public void testAddEntryWithSameInterval () {
        CommunicationTopologyEntryMerger merger = new CommunicationTopologyEntryMerger();
        merger.add(this.newEntry(JAN_2014, this.device1));

        // Business method
        merger.add(this.newEntry(JAN_2014, this.device2));

        // Asserts
        List<CompleteCommunicationTopologyEntryImpl> entries = merger.getEntries();
        assertThat(entries).hasSize(1);
        CompleteCommunicationTopologyEntryImpl actualEntry = entries.get(0);
        assertThat(actualEntry.getInterval()).isEqualTo(JAN_2014);
        assertDeviceIds(actualEntry.getDevices(), DEVICE1_ID, DEVICE2_ID);
    }

    @Test
    public void testAddEntryWithEnvelopedInterval () {
        CommunicationTopologyEntryMerger merger = new CommunicationTopologyEntryMerger();
        merger.add(this.newEntry(JAN_2014, this.device1));

        // Business method
        merger.add(this.newEntry(MID_JAN_2014, this.device2));

        // Asserts
        List<CompleteCommunicationTopologyEntryImpl> entries = merger.getEntries();
        assertThat(entries).hasSize(3);
        for (CompleteCommunicationTopologyEntryImpl entry : entries) {
            if (entry.getInterval().getStart().equals(JAN_2014.getStart())) {
                assertDeviceIds(entry.getDevices(), DEVICE1_ID);
            }
            else if (entry.getInterval().getStart().equals(MID_JAN_2014.getStart())) {
                assertDeviceIds(entry.getDevices(), DEVICE1_ID, DEVICE2_ID);
            }
            else if (entry.getInterval().getStart().equals(MID_JAN_2014.getEnd())) {
                assertDeviceIds(entry.getDevices(), DEVICE1_ID);
            }
            else {
                fail("Unexpected Interval: " + entry.getInterval());
            }
        }
    }

    @Test
    public void testAddEntryWithIntervalThatAbutAtEnd () {
        CommunicationTopologyEntryMerger merger = new CommunicationTopologyEntryMerger();
        merger.add(this.newEntry(JAN_2014, this.device1));

        // Business method
        merger.add(this.newEntry(SECOND_HALF_JAN_2014, this.device2));

        // Asserts
        List<CompleteCommunicationTopologyEntryImpl> entries = merger.getEntries();
        assertThat(entries).hasSize(2);
        for (CompleteCommunicationTopologyEntryImpl entry : entries) {
            if (entry.getInterval().equals(FIRST_HALF_JAN_2014)) {
                assertDeviceIds(entry.getDevices(), DEVICE1_ID);
            }
            else if (entry.getInterval().equals(SECOND_HALF_JAN_2014)) {
                assertDeviceIds(entry.getDevices(), DEVICE1_ID, DEVICE2_ID);
            }
            else {
                fail("Unexpected Interval: " + entry.getInterval());
            }
        }
    }

    @Test
    public void testAddEntryWithIntervalThatAbutAtStart () {
        CommunicationTopologyEntryMerger merger = new CommunicationTopologyEntryMerger();
        merger.add(this.newEntry(JAN_2014, this.device1));

        // Business method
        merger.add(this.newEntry(FIRST_HALF_JAN_2014, this.device2));

        // Asserts
        List<CompleteCommunicationTopologyEntryImpl> entries = merger.getEntries();
        assertThat(entries).hasSize(2);
        for (CompleteCommunicationTopologyEntryImpl entry : entries) {
            if (entry.getInterval().equals(SECOND_HALF_JAN_2014)) {
                assertDeviceIds(entry.getDevices(), DEVICE1_ID);
            }
            else if (entry.getInterval().equals(FIRST_HALF_JAN_2014)) {
                assertDeviceIds(entry.getDevices(), DEVICE1_ID, DEVICE2_ID);
            }
            else {
                fail("Unexpected Interval: " + entry.getInterval());
            }
        }
    }

    @Test
    public void testAddEntryWithIntervalThatOverlapsAtStart () {
        CommunicationTopologyEntryMerger merger = new CommunicationTopologyEntryMerger();
        merger.add(this.newEntry(MARCH_2014, this.device1));

        // Business method
        merger.add(this.newEntry(MID_FEB_TO_MID_MARCH_2014, this.device2));

        // Asserts
        List<CompleteCommunicationTopologyEntryImpl> entries = merger.getEntries();
        assertThat(entries).hasSize(3);
        for (CompleteCommunicationTopologyEntryImpl entry : entries) {
            if (entry.getInterval().getStart().equals(MID_FEB_TO_MID_MARCH_2014.getStart())) {
                assertDeviceIds(entry.getDevices(), DEVICE2_ID);
            }
            else if (entry.getInterval().getStart().equals(MARCH_2014.getStart())) {
                assertDeviceIds(entry.getDevices(), DEVICE1_ID, DEVICE2_ID);
            }
            else if (entry.getInterval().getStart().equals(MID_FEB_TO_MID_MARCH_2014.getEnd())) {
                assertDeviceIds(entry.getDevices(), DEVICE1_ID);
            }
            else {
                fail("Unexpected Interval: " + entry.getInterval());
            }
        }
    }

    @Test
    public void testAddEntryWithIntervalThatOverlapsAtEnd () {
        CommunicationTopologyEntryMerger merger = new CommunicationTopologyEntryMerger();
        merger.add(this.newEntry(MARCH_2014, this.device1));

        // Business method
        merger.add(this.newEntry(MID_MARCH_TO_MID_APRIL_2014, this.device2));

        // Asserts
        List<CompleteCommunicationTopologyEntryImpl> entries = merger.getEntries();
        assertThat(entries).hasSize(3);
        for (CompleteCommunicationTopologyEntryImpl entry : entries) {
            if (entry.getInterval().getStart().equals(MARCH_2014.getStart())) {
                assertDeviceIds(entry.getDevices(), DEVICE1_ID);
            }
            else if (entry.getInterval().getStart().equals(MID_MARCH_TO_MID_APRIL_2014.getStart())) {
                assertDeviceIds(entry.getDevices(), DEVICE1_ID, DEVICE2_ID);
            }
            else if (entry.getInterval().getStart().equals(MARCH_2014.getEnd())) {
                assertDeviceIds(entry.getDevices(), DEVICE2_ID);
            }
            else {
                fail("Unexpected Interval: " + entry.getInterval());
            }
        }
    }

    private CompleteCommunicationTopologyEntryImpl newEntry(Interval interval, Device... devices) {
        return new CompleteCommunicationTopologyEntryImpl(interval, devices);
    }

    private void assertDeviceIds (List<Device> devices, Long... expectedDeviceIds) {
        Set<Long> actualIds = new HashSet<>();
        for (Device slave : devices) {
            actualIds.add(slave.getId());
        }
        assertThat(actualIds).containsOnly(expectedDeviceIds);
    }

}
package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.util.time.Interval;
import com.energyict.mdc.device.data.Device;
import org.joda.time.DateMidnight;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests the {@link DeviceImpl.CommunicationTopologyEntryComparator} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-06-02 (16:09)
 */
@RunWith(MockitoJUnitRunner.class)
public class CommunicationTopologyEntryComparatorTest {

    private static final Interval EARLY_BIG_BANG_TO_JAN_2014 = new Interval(null, new DateMidnight(2014, 1, 1).toDate());
    private static final Interval JAN_2014 = new Interval(new DateMidnight(2014, 1, 1).toDate(), new DateMidnight(2014, 2, 1).toDate());
    private static final Interval MARCH_2014 = new Interval(new DateMidnight(2014, 3, 1).toDate(), new DateMidnight(2014, 4, 1).toDate());
    private static final Interval MARCH_2014_UNTIL_FOREVER = new Interval(new DateMidnight(2014, 3, 1).toDate(), null);

    @Mock
    private Device device1;
    @Mock
    private Device device2;

    @Test
    public void testEqualCommunicationTopologies () {
        SimpleCommunicationTopologyEntryImpl topology1 = new SimpleCommunicationTopologyEntryImpl(this.device1, JAN_2014);
        SimpleCommunicationTopologyEntryImpl topology2 = new SimpleCommunicationTopologyEntryImpl(this.device2, JAN_2014);

        // Business method
        int comparison = this.newCommunicationTopologyComparator().compare(topology1, topology2);

        // Asserts
        assertThat(comparison).isZero();
    }

    @Test
    public void testEarlyBigBangToDateWithDateUntilForever () {
        SimpleCommunicationTopologyEntryImpl topology1 = new SimpleCommunicationTopologyEntryImpl(this.device1, EARLY_BIG_BANG_TO_JAN_2014);
        SimpleCommunicationTopologyEntryImpl topology2 = new SimpleCommunicationTopologyEntryImpl(this.device2, MARCH_2014_UNTIL_FOREVER);

        // Business method
        int comparison = this.newCommunicationTopologyComparator().compare(topology1, topology2);

        // Asserts
        assertThat(comparison).isEqualTo(-1);
    }

    @Test
    public void testDateUntilForeverWithEarlyBigBang () {
        SimpleCommunicationTopologyEntryImpl topology1 = new SimpleCommunicationTopologyEntryImpl(this.device1, MARCH_2014_UNTIL_FOREVER);
        SimpleCommunicationTopologyEntryImpl topology2 = new SimpleCommunicationTopologyEntryImpl(this.device2, EARLY_BIG_BANG_TO_JAN_2014);

        // Business method
        int comparison = this.newCommunicationTopologyComparator().compare(topology1, topology2);

        // Asserts
        assertThat(comparison).isEqualTo(1);
    }

    @Test
    public void testSameStartButDifferentEnd () {
        SimpleCommunicationTopologyEntryImpl topology1 = new SimpleCommunicationTopologyEntryImpl(this.device1, MARCH_2014_UNTIL_FOREVER);
        SimpleCommunicationTopologyEntryImpl topology2 = new SimpleCommunicationTopologyEntryImpl(this.device2, MARCH_2014);

        // Business method

        // Asserts
        assertThat(this.newCommunicationTopologyComparator().compare(topology1, topology2)).isEqualTo(1);    // topology1 ends after topology2 and is therefore considered bigger
        assertThat(this.newCommunicationTopologyComparator().compare(topology2, topology1)).isEqualTo(-1);   // topology2 ends before topology1 and is therefore considered smaller
    }

    private DeviceImpl.CommunicationTopologyEntryComparator newCommunicationTopologyComparator() {
        return new DeviceImpl.CommunicationTopologyEntryComparator();
    }

}
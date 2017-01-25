package com.energyict.mdc.device.topology.impl;

import com.energyict.mdc.device.data.Device;

import com.elster.jupiter.devtools.tests.EqualsContractTest;
import com.google.common.collect.Range;
import org.joda.time.DateMidnight;

import java.time.Instant;
import java.util.Arrays;

import org.junit.*;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests the equality aspects of the {@link CompleteTopologyTimesliceImpl} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-06-03 (14:06)
 */
public class CompleteDeviceTopologyEntryImplEqualityTest extends EqualsContractTest {

    private static final long DEVICE1_ID = 97;
    private static final long DEVICE2_ID = 101;
    private static final Range<Instant> JAN_2014 = Range.closed(new DateMidnight(2014, 1, 1).toDate().toInstant(), new DateMidnight(2014, 2, 1).toDate().toInstant());
    private static final Range<Instant> MARCH_2014 = Range.closed(new DateMidnight(2014, 3, 1).toDate().toInstant(), new DateMidnight(2014, 4, 1).toDate().toInstant());

    private static CompleteTopologyTimesliceImpl instanceA;
    private static Device device1;
    private static Device device2;

    @BeforeClass
    public static void setup () {
        device1 = mock(Device.class);
        when(device1.getId()).thenReturn(DEVICE1_ID);
        device2 = mock(Device.class);
        when(device2.getId()).thenReturn(DEVICE2_ID);
        instanceA = new CompleteTopologyTimesliceImpl(JAN_2014, device1, device2);
    }

    @Override
    protected Object getInstanceA() {
        return instanceA;
    }

    @Override
    protected Object getInstanceEqualToA() {
        return new CompleteTopologyTimesliceImpl(JAN_2014, device2);
    }

    @Override
    protected Iterable<?> getInstancesNotEqualToA() {
        return Arrays.asList(new CompleteTopologyTimesliceImpl(MARCH_2014, device1, device2));
    }

    @Override
    protected boolean canBeSubclassed() {
        return false;
    }

    @Override
    protected Object getInstanceOfSubclassEqualToA() {
        return null;
    }
}
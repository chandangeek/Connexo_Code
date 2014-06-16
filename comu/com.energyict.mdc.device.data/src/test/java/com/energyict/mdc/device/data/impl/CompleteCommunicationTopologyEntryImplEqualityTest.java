package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.devtools.tests.EqualsContractTest;
import com.elster.jupiter.util.time.Interval;
import com.energyict.mdc.device.data.Device;
import java.util.Arrays;
import org.joda.time.DateMidnight;
import org.junit.BeforeClass;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests the equality aspects of the {@link CompleteCommunicationTopologyEntryImpl} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-06-03 (14:06)
 */
public class CompleteCommunicationTopologyEntryImplEqualityTest extends EqualsContractTest {

    private static final long DEVICE1_ID = 97;
    private static final long DEVICE2_ID = 101;
    private static final Interval JAN_2014 = new Interval(new DateMidnight(2014, 1, 1).toDate(), new DateMidnight(2014, 2, 1).toDate());
    private static final Interval MARCH_2014 = new Interval(new DateMidnight(2014, 3, 1).toDate(), new DateMidnight(2014, 4, 1).toDate());

    private static CompleteCommunicationTopologyEntryImpl instanceA;
    private static Device device1;
    private static Device device2;

    @BeforeClass
    public static void setup () {
        device1 = mock(Device.class);
        when(device1.getId()).thenReturn(DEVICE1_ID);
        device2 = mock(Device.class);
        when(device2.getId()).thenReturn(DEVICE2_ID);
        instanceA = new CompleteCommunicationTopologyEntryImpl(JAN_2014, device1, device2);
    }

    @Override
    protected Object getInstanceA() {
        return instanceA;
    }

    @Override
    protected Object getInstanceEqualToA() {
        return new CompleteCommunicationTopologyEntryImpl(JAN_2014, device2);
    }

    @Override
    protected Iterable<?> getInstancesNotEqualToA() {
        return Arrays.asList(new CompleteCommunicationTopologyEntryImpl(MARCH_2014, device1, device2));
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
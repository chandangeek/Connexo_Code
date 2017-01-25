package com.energyict.mdc.device.topology.impl;

import com.energyict.mdc.device.data.Device;

import com.elster.jupiter.devtools.tests.EqualsContractTest;
import com.google.common.collect.Range;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneOffset;
import java.util.Arrays;

import org.junit.*;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests the equality aspects of the {@link DeviceTopologyImpl} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-06-04 (13:33)
 */
public class DeviceTopologyImplEqualityTest extends EqualsContractTest {

    private static final long DEVICE1_ID = 97;
    private static final long DEVICE2_ID = 101;
    private static final LocalDateTime JAN_1ST = LocalDateTime.of(2014, Month.JANUARY, 1, 0, 0);
    private static final LocalDateTime FEB_1ST = JAN_1ST.plusMonths(1);
    private static final LocalDateTime MARCH_1ST = FEB_1ST.plusMonths(1);
    private static final LocalDateTime APRIL_1ST = MARCH_1ST.plusMonths(1);
    private static final Range<Instant> JAN_2014 = Range.closed(JAN_1ST.toInstant(ZoneOffset.UTC), FEB_1ST.toInstant(ZoneOffset.UTC));
    private static final Range<Instant> MARCH_2014 = Range.closed(MARCH_1ST.toInstant(ZoneOffset.UTC), APRIL_1ST.toInstant(ZoneOffset.UTC));

    private static DeviceTopologyImpl instanceA;
    private static Device device1;
    private static Device device2;

    @BeforeClass
    public static void setup () {
        device1 = mock(Device.class);
        when(device1.getId()).thenReturn(DEVICE1_ID);
        device2 = mock(Device.class);
        when(device2.getId()).thenReturn(DEVICE2_ID);
        instanceA = new DeviceTopologyImpl(device1, JAN_2014);
    }

    @Override
    protected Object getInstanceA() {
        return instanceA;
    }

    @Override
    protected Object getInstanceEqualToA() {
        return new DeviceTopologyImpl(device1, JAN_2014);
    }

    @Override
    protected Iterable<?> getInstancesNotEqualToA() {
        return Arrays.asList(
                new DeviceTopologyImpl(device2, JAN_2014),
                new DeviceTopologyImpl(device2, MARCH_2014),
                new DeviceTopologyImpl(device1, MARCH_2014));
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
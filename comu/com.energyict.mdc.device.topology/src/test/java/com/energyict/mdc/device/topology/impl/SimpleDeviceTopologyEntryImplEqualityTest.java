/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.topology.impl;

import com.elster.jupiter.devtools.tests.EqualsContractTest;
import com.elster.jupiter.util.time.Interval;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.topology.TopologyTimeslice;

import org.joda.time.DateMidnight;

import java.util.Arrays;

import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests the equality aspects of the {@link SimpleTopologyTimesliceImpl} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-06-02 (16:09)
 */
@RunWith(MockitoJUnitRunner.class)
public class SimpleDeviceTopologyEntryImplEqualityTest extends EqualsContractTest {

    private static final long DEVICE1_ID = 97;
    private static final long DEVICE2_ID = 101;
    private static final Interval JAN_2014 = new Interval(new DateMidnight(2014, 1, 1).toDate(), new DateMidnight(2014, 2, 1).toDate());
    private static final Interval MARCH_2014 = new Interval(new DateMidnight(2014, 3, 1).toDate(), new DateMidnight(2014, 4, 1).toDate());

    private static TopologyTimeslice instanceA;
    private static Device device1;
    private static Device device2;

    @BeforeClass
    public static void setup () {
        device1 = mock(Device.class);
        when(device1.getId()).thenReturn(DEVICE1_ID);
        device2 = mock(Device.class);
        when(device2.getId()).thenReturn(DEVICE2_ID);
        instanceA = new SimpleTopologyTimesliceImpl(device1, JAN_2014);
    }

    @Override
    protected Object getInstanceA() {
        return instanceA;
    }

    @Override
    protected Object getInstanceEqualToA() {
        return new SimpleTopologyTimesliceImpl(device1, JAN_2014);
    }

    @Override
    protected Iterable<?> getInstancesNotEqualToA() {
        return Arrays.asList(
                new SimpleTopologyTimesliceImpl(device2, JAN_2014),
                new SimpleTopologyTimesliceImpl(device1, MARCH_2014));
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
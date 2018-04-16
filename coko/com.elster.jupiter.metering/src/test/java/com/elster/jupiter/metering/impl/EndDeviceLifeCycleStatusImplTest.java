/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl;

import com.elster.jupiter.devtools.tests.EqualsContractTest;
import com.elster.jupiter.fsm.State;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.time.Interval;

import java.time.Instant;
import java.util.Arrays;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class EndDeviceLifeCycleStatusImplTest extends EqualsContractTest {
    private EndDeviceLifeCycleStatusImpl instanceA;

    @Mock
    private UserService userService;
    @Mock
    private ThreadPrincipalService threadPrincipalService;
    @Mock
    private EndDevice endDevice, otherDevice;
    @Mock
    private State state;

    @Before
    public void equalsContractSetUp() {
        when(threadPrincipalService.getPrincipal()).thenReturn(() -> "Username");
        super.equalsContractSetUp();
    }

    @Override
    protected Object getInstanceA() {
        if (instanceA == null) {
            instanceA = new EndDeviceLifeCycleStatusImpl(userService, threadPrincipalService)
                    .initialize(Interval.sinceEpoch(), endDevice, state);
        }
        return instanceA;
    }

    @Override
    protected Object getInstanceEqualToA() {
        return new EndDeviceLifeCycleStatusImpl(userService, threadPrincipalService)
                .initialize(Interval.sinceEpoch(), endDevice, state);
    }

    @Override
    protected Iterable<?> getInstancesNotEqualToA() {
        EndDeviceLifeCycleStatusImpl other = new EndDeviceLifeCycleStatusImpl(userService, threadPrincipalService)
                .initialize(Interval.sinceEpoch(), otherDevice, state);
        EndDeviceLifeCycleStatusImpl yetAnother = new EndDeviceLifeCycleStatusImpl(userService, threadPrincipalService)
                .initialize(Interval.startAt(Instant.EPOCH.plusMillis(60000)), endDevice, state);
        return Arrays.asList(other, yetAnother);
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

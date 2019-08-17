/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.lifecycle.impl;

import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.device.lifecycle.config.AuthorizedAction;
import com.energyict.mdc.common.device.lifecycle.config.AuthorizedBusinessProcessAction;
import com.energyict.mdc.device.lifecycle.DeviceLifeCycleService;

import java.time.Clock;
import java.time.Instant;
import java.util.Collections;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link ExecutableBusinessProcessActionImpl} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-04-10 (16:08)
 */
@RunWith(MockitoJUnitRunner.class)
public class ExecutableBusinessProcessActionImplTest {

    @Mock
    private DeviceLifeCycleService service;
    @Mock
    private AuthorizedBusinessProcessAction action;
    @Mock
    private Device device;

    @Test
    public void getDevice() {
        ExecutableBusinessProcessActionImpl executableAction = new ExecutableBusinessProcessActionImpl(this.device, this.action, this.service, Clock
                .systemDefaultZone());

        // Business method
        Device device = executableAction.getDevice();

        // Asserts
        assertThat(device).isEqualTo(this.device);
    }

    @Test
    public void getAction() {
        ExecutableBusinessProcessActionImpl executableAction = new ExecutableBusinessProcessActionImpl(this.device, this.action, this.service, Clock
                .systemDefaultZone());

        // Business method
        AuthorizedAction action = executableAction.getAction();

        // Asserts
        assertThat(action).isEqualTo(this.action);
    }

    @Test
    public void executeDelegatesToService() {
        Instant now = Instant.now();
        Clock clock = mock(Clock.class);
        when(clock.instant()).thenReturn(now);
        ExecutableBusinessProcessActionImpl executableAction = new ExecutableBusinessProcessActionImpl(this.device, this.action, this.service, clock);

        // Business method
        executableAction.execute(now, Collections.emptyList());

        // Asserts
        verify(this.service).execute(this.action, this.device, now);
    }

}

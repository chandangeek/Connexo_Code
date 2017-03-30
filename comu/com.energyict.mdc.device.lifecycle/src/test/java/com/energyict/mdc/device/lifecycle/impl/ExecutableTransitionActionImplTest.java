/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.lifecycle.impl;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.lifecycle.DeviceLifeCycleService;
import com.energyict.mdc.device.lifecycle.ExecutableActionProperty;
import com.energyict.mdc.device.lifecycle.config.AuthorizedAction;
import com.energyict.mdc.device.lifecycle.config.AuthorizedTransitionAction;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Tests the {@link ExecutableTransitionActionImpl} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-04-10 (16:08)
 */
@RunWith(MockitoJUnitRunner.class)
public class ExecutableTransitionActionImplTest {

    @Mock
    private DeviceLifeCycleService service;
    @Mock
    private AuthorizedTransitionAction action;
    @Mock
    private Device device;

    @Test
    public void getDevice() {
        ExecutableTransitionActionImpl executableAction = new ExecutableTransitionActionImpl(this.device, this.action, this.service);

        // Business method
        Device device = executableAction.getDevice();

        // Asserts
        assertThat(device).isEqualTo(this.device);
    }

    @Test
    public void getAction() {
        ExecutableTransitionActionImpl executableAction = new ExecutableTransitionActionImpl(this.device, this.action, this.service);

        // Business method
        AuthorizedAction action = executableAction.getAction();

        // Asserts
        assertThat(action).isEqualTo(this.action);
    }

    @Test
    public void executeDelegatesToService() {
        ExecutableTransitionActionImpl executableAction = new ExecutableTransitionActionImpl(this.device, this.action, this.service);
        List<ExecutableActionProperty> properties = Arrays.asList(mock(ExecutableActionProperty.class));
        Instant now = Instant.now();

        // Business method
        executableAction.execute(now, properties);

        // Asserts
        verify(this.service).execute(this.action, this.device, now, properties);
    }

}
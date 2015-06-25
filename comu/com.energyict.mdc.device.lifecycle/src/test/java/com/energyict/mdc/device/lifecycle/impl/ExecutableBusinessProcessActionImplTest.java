package com.energyict.mdc.device.lifecycle.impl;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.lifecycle.DeviceLifeCycleService;
import com.energyict.mdc.device.lifecycle.config.AuthorizedAction;
import com.energyict.mdc.device.lifecycle.config.AuthorizedBusinessProcessAction;

import java.time.Instant;
import java.util.Collections;

import org.junit.*;
import org.junit.runner.*;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

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
        ExecutableBusinessProcessActionImpl executableAction = new ExecutableBusinessProcessActionImpl(this.device, this.action, this.service);

        // Business method
        Device device = executableAction.getDevice();

        // Asserts
        assertThat(device).isEqualTo(this.device);
    }

    @Test
    public void getAction() {
        ExecutableBusinessProcessActionImpl executableAction = new ExecutableBusinessProcessActionImpl(this.device, this.action, this.service);

        // Business method
        AuthorizedAction action = executableAction.getAction();

        // Asserts
        assertThat(action).isEqualTo(this.action);
    }

    @Test
    public void executeDelegatesToService() {
        ExecutableBusinessProcessActionImpl executableAction = new ExecutableBusinessProcessActionImpl(this.device, this.action, this.service);
        Instant now = Instant.now();

        // Business method
        executableAction.execute(now, Collections.emptyList());

        // Asserts
        verify(this.service).execute(this.action, this.device, now);
    }

}
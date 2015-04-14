package com.energyict.mdc.device.lifecycle.impl;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.lifecycle.DeviceLifeCycleService;
import com.energyict.mdc.device.lifecycle.config.AuthorizedAction;

import org.junit.*;
import org.junit.runner.*;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

/**
 * Tests the {@link ExecutableActionImpl} component
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-04-10 (16:08)
 */
@RunWith(MockitoJUnitRunner.class)
public class ExecutableActionImplTest {

    @Mock
    private DeviceLifeCycleService service;
    @Mock
    private AuthorizedAction action;
    @Mock
    private Device device;

    @Test
    public void getDevice() {
        ExecutableActionImpl executableAction = new ExecutableActionImpl(this.device, this.action, this.service);

        // Business method
        Device device = executableAction.getDevice();

        // Asserts
        assertThat(device).isEqualTo(this.device);
    }

    @Test
    public void getAction() {
        ExecutableActionImpl executableAction = new ExecutableActionImpl(this.device, this.action, this.service);

        // Business method
        AuthorizedAction action = executableAction.getAction();

        // Asserts
        assertThat(action).isEqualTo(this.action);
    }

    @Test
    public void executeDelegatesToService() {
        ExecutableActionImpl executableAction = new ExecutableActionImpl(this.device, this.action, this.service);

        // Business method
        executableAction.execute();

        // Asserts
        verify(service).execute(action, device);
    }

}
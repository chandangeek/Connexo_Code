/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config.impl;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.verify;

/**
 * Tests the {@link DeviceMessageFileServiceImpl} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-05-13 (09:17)
 */
@RunWith(MockitoJUnitRunner.class)
public class DeviceMessageFileServiceImplTest {

    private static final long DEVICE_MESSAGE_FILE_ID = 97L;

    @Mock
    private ServerDeviceConfigurationService deviceConfigurationService;

    @Test
    public void findDeviceMessageFileByIdDelegatesToDeviceConfigurationService() {
        // Business method
        this.getTestInstance().findDeviceMessageFile(DEVICE_MESSAGE_FILE_ID);

        // Asserts
        verify(this.deviceConfigurationService).findDeviceMessageFile(DEVICE_MESSAGE_FILE_ID);
    }

    private DeviceMessageFileServiceImpl getTestInstance() {
        return new DeviceMessageFileServiceImpl(this.deviceConfigurationService);
    }

}
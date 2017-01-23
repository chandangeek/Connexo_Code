package com.energyict.mdc.engine.impl;

import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.upl.Services;
import com.energyict.mdc.upl.messages.legacy.DeviceMessageFileFinder;
import com.energyict.mdc.upl.properties.DeviceMessageFile;

import java.util.Collections;
import java.util.Optional;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link DeviceMessageFileFinderImpl} component.
 */
@RunWith(MockitoJUnitRunner.class)
public class DeviceMessageFileFinderImplTest {

    private static final long MESSAGE_ID = 97L;

    @Mock
    private DeviceConfigurationService deviceConfigurationService;
    @Mock
    private com.energyict.mdc.device.config.DeviceMessageFile messageFile;

    @After
    public void clearServices() {
        Services.deviceMessageFileFinder(null);
    }

    @Test
    public void activate() {
        // Make sure that no service is registered
        Services.deviceMessageFileFinder(null);

        // Business methods
        this.getInstance().activate();

        // Asserts
        assertThat(Services.deviceMessageFileFinder()).isNotNull();
    }

    @Test
    public void deactivate() {
        // Make sure that a service is registered
        Services.deviceMessageFileFinder(mock(DeviceMessageFileFinder.class));

        // Business methods
        this.getInstance().deactivate();

        // Asserts
        assertThat(Services.deviceMessageFileFinder()).isNull();
    }

    @Test
    public void nonNumericalIdentifierReturnEmptyOptional() {
        DeviceMessageFileFinderImpl finder = this.getInstance();

        // Business method
        Optional<DeviceMessageFile> messageFile = finder.from(DeviceMessageFileFinderImplTest.class.getSimpleName());

        // Asserts
        assertThat(messageFile).isEmpty();
    }

    @Test
    public void nonExistingCalendar() {
        DeviceMessageFileFinderImpl finder = this.getInstance();

        // Business method
        Optional<DeviceMessageFile> messageFile = finder.from("101");

        // Asserts
        assertThat(messageFile).isEmpty();
        verify(this.deviceConfigurationService.findAllDeviceTypes());
    }

    @Test
    public void existingCalendar() {
        DeviceMessageFileFinderImpl finder = this.getInstance();
        DeviceType deviceType = mock(DeviceType.class);
        when(deviceType.getDeviceMessageFiles()).thenReturn(Collections.singletonList(this.messageFile));

        // Business method
        Optional<DeviceMessageFile> messageFile = finder.from(Long.toString(MESSAGE_ID));

        // Asserts
        verify(this.deviceConfigurationService.findAllDeviceTypes());
        verify(deviceType.getDeviceMessageFiles());
        assertThat(messageFile).isEqualTo(this.messageFile);
    }

    private DeviceMessageFileFinderImpl getInstance() {
        return new DeviceMessageFileFinderImpl(this.deviceConfigurationService);
    }

}
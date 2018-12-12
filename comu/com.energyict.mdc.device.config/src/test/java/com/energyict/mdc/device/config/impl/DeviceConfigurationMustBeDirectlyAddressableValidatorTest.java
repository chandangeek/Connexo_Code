/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config.impl;

import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.PartialOutboundConnectionTask;

import javax.validation.ConstraintValidatorContext;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link DeviceConfigurationMustBeDirectlyAddressableValidator} component.
 */
@RunWith(MockitoJUnitRunner.class)
public class DeviceConfigurationMustBeDirectlyAddressableValidatorTest {

    @Mock
    private DeviceConfiguration deviceConfiguration;
    @Mock
    private PartialOutboundConnectionTask partialConnectionTask;
    @Mock
    private ConstraintValidatorContext validatorContext;

    @Before
    public void initializeMocks() {
        when(deviceConfiguration.isActive()).thenReturn(true);
        when(this.partialConnectionTask.getConfiguration()).thenReturn(this.deviceConfiguration);
    }

    @Test
    public void validatorChecksThatConfigurationIsDirectlyAddressable () {
        DeviceConfigurationMustBeDirectlyAddressableValidator validator = new DeviceConfigurationMustBeDirectlyAddressableValidator();

        // Business method
        validator.isValid(this.partialConnectionTask, this.validatorContext);

        // Asserts
        verify(this.deviceConfiguration).isDirectlyAddressable();
    }

    @Test
    public void validWhenDeviceConfigurationIsDirectlyAddressable() {
        DeviceConfigurationMustBeDirectlyAddressableValidator validator = new DeviceConfigurationMustBeDirectlyAddressableValidator();
        when(this.deviceConfiguration.isDirectlyAddressable()).thenReturn(true);

        // Business method
        boolean valid = validator.isValid(this.partialConnectionTask, this.validatorContext);

        // Asserts
        assertThat(valid).isTrue();
    }

    @Test
    public void invalidWhenDeviceConfigurationIsNotDirectlyAddressable() {
        DeviceConfigurationMustBeDirectlyAddressableValidator validator = new DeviceConfigurationMustBeDirectlyAddressableValidator();
        when(this.deviceConfiguration.isDirectlyAddressable()).thenReturn(false);

        // Business method
        boolean valid = validator.isValid(this.partialConnectionTask, this.validatorContext);

        // Asserts
        assertThat(valid).isFalse();
    }

}
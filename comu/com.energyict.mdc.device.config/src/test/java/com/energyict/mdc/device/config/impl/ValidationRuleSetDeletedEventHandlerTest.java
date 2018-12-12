/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.validation.ValidationRuleSet;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ValidationRuleSetDeletedEventHandlerTest {

    @Mock
    private DeviceConfigurationService deviceConfigurationService;

    @Mock
    private ValidationRuleSet validationRuleSet;

    @Mock
    private DeviceConfiguration deviceConfiguration;

    @Before
    public void initializeMocks () {
        when(this.validationRuleSet.getName()).thenReturn(ValidationRuleSetDeletedEventHandlerTest.class.getSimpleName());
    }

    @Test
    public void testNotUsedValidationRuleSetDeletedEventHandler() {
        List<DeviceConfiguration> deviceConfigurations = Collections.EMPTY_LIST;
        when(this.deviceConfigurationService.findDeviceConfigurationsForValidationRuleSet(this.validationRuleSet.getId())).thenReturn(deviceConfigurations);

        LocalEvent event = this.mockDeleteEvent(this.validationRuleSet);
        this.newTestHandler().handle(event);
        verify(event).getSource();
        verify(this.deviceConfigurationService).findDeviceConfigurationsForValidationRuleSet(this.validationRuleSet.getId());
        verify(this.deviceConfiguration, never()).removeValidationRuleSet(this.validationRuleSet);
    }

    @Test
    public void testUsedValidationRuleSetDeletedEventHandler() {
        List<DeviceConfiguration> deviceConfigurations = Arrays.asList(this.deviceConfiguration);
        when(this.deviceConfigurationService.findDeviceConfigurationsForValidationRuleSet(this.validationRuleSet.getId())).thenReturn(deviceConfigurations);

        LocalEvent event = this.mockDeleteEvent(this.validationRuleSet);
        this.newTestHandler().handle(event);
        verify(this.deviceConfiguration).removeValidationRuleSet(this.validationRuleSet);


    }

    private LocalEvent mockDeleteEvent(ValidationRuleSet validationRuleSet) {
        LocalEvent event = mock(LocalEvent.class);
        when(event.getSource()).thenReturn(validationRuleSet);
        com.elster.jupiter.events.EventType eventType = mock(com.elster.jupiter.events.EventType.class);
        when(eventType.getTopic()).thenReturn("com/elster/jupiter/validation/validationruleset/DELETED");
        when(event.getType()).thenReturn(eventType);
        return event;
    }

    private ValidationRuleSetDeletedEventHandler newTestHandler () {
        ValidationRuleSetDeletedEventHandler handler = new ValidationRuleSetDeletedEventHandler();
        handler.setDeviceConfigurationService(this.deviceConfigurationService);
        return handler;
    }
}

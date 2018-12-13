/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.estimation.EstimationRuleSet;
import com.elster.jupiter.events.LocalEvent;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;

import java.util.Arrays;
import java.util.Collections;

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
public class EstimationRuleSetDeletedEventHandlerTest {

    @Mock
    private DeviceConfigurationService deviceConfigurationService;

    @Mock
    private EstimationRuleSet estimationRuleSet;

    @Mock
    private DeviceConfiguration deviceConfiguration;
    
    @Mock
    private Finder<DeviceConfiguration> finder;

    @Before
    public void initializeMocks () {
        when(estimationRuleSet.getName()).thenReturn(EstimationRuleSetDeletedEventHandlerTest.class.getSimpleName());
    }

    @Test
    public void testNotUsedEstimationRuleSetDeletedEventHandler() {
        when(deviceConfigurationService.findDeviceConfigurationsForEstimationRuleSet(estimationRuleSet)).thenReturn(finder);
        when(finder.find()).thenReturn(Collections.emptyList());

        LocalEvent event = mockDeleteEvent(estimationRuleSet);
        this.newTestHandler().handle(event);
        verify(event).getSource();
        verify(deviceConfigurationService).findDeviceConfigurationsForEstimationRuleSet(estimationRuleSet);
        verify(deviceConfiguration, never()).removeEstimationRuleSet(estimationRuleSet);
    }

    @Test
    public void testUsedEstimationRuleSetDeletedEventHandler() {
        when(deviceConfigurationService.findDeviceConfigurationsForEstimationRuleSet(estimationRuleSet)).thenReturn(finder);
        when(finder.find()).thenReturn(Arrays.asList(deviceConfiguration));

        LocalEvent event = mockDeleteEvent(estimationRuleSet);
        newTestHandler().handle(event);
        verify(deviceConfiguration).removeEstimationRuleSet(estimationRuleSet);
    }

    private LocalEvent mockDeleteEvent(EstimationRuleSet estimationRuleSet) {
        LocalEvent event = mock(LocalEvent.class);
        when(event.getSource()).thenReturn(estimationRuleSet);
        com.elster.jupiter.events.EventType eventType = mock(com.elster.jupiter.events.EventType.class);
        when(eventType.getTopic()).thenReturn("com/elster/jupiter/estimation/estimationruleset/DELETED");
        when(event.getType()).thenReturn(eventType);
        return event;
    }

    private EstimationRuleSetDeletedEventHandler newTestHandler () {
        EstimationRuleSetDeletedEventHandler handler = new EstimationRuleSetDeletedEventHandler();
        handler.setDeviceConfigurationService(this.deviceConfigurationService);
        return handler;
    }
}

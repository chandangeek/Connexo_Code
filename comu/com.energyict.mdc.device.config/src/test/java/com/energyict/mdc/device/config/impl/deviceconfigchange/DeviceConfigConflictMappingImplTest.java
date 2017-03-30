/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config.impl.deviceconfigchange;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.orm.DataModel;
import com.energyict.mdc.device.config.ConflictingConnectionMethodSolution;
import com.energyict.mdc.device.config.ConflictingSecuritySetSolution;
import com.energyict.mdc.device.config.DeviceConfigConflictMapping;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.PartialConnectionTask;
import com.energyict.mdc.device.config.SecurityPropertySet;
import com.energyict.mdc.device.config.events.EventType;

import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class DeviceConfigConflictMappingImplTest {

    @Mock
    private DataModel dataModel;
    @Mock
    private ValidatorFactory validatorFactory;
    @Mock
    private Validator validator;
    @Mock
    private EventService eventService;
    @Mock
    private DeviceType deviceType;
    @Mock
    private DeviceConfiguration origin;
    @Mock
    private DeviceConfiguration destination;

    @Mock
    private PartialConnectionTask partialConnectionTask;
    @Mock
    private PartialConnectionTask otherPartialConnectionTask;
    @Mock
    private SecurityPropertySet securityPropertySet;
    @Mock
    private SecurityPropertySet otherSecurityPropertySet;

    @Mock
    private ConflictingSecuritySetSolutionImpl conflictingSecuritySetSolution;
    @Mock
    private ConflictingConnectionMethodSolutionImpl conflictingConnectionMethodSolution;

    @Before
    public void setup() {
        when(dataModel.getValidatorFactory()).thenReturn(validatorFactory);
        when(validatorFactory.getValidator()).thenReturn(validator);
        when(dataModel.getInstance(ConflictingSecuritySetSolutionImpl.class)).thenReturn(conflictingSecuritySetSolution);
        when(dataModel.getInstance(ConflictingConnectionMethodSolutionImpl.class)).thenReturn(conflictingConnectionMethodSolution);
        when(conflictingSecuritySetSolution.getConflictingMappingAction()).thenReturn(DeviceConfigConflictMapping.ConflictingMappingAction.NOT_DETERMINED_YET);
        when(conflictingSecuritySetSolution.initialize(any(DeviceConfigConflictMappingImpl.class), any(SecurityPropertySet.class))).thenReturn(conflictingSecuritySetSolution);
        when(conflictingConnectionMethodSolution.getConflictingMappingAction()).thenReturn(DeviceConfigConflictMapping.ConflictingMappingAction.NOT_DETERMINED_YET);
        when(conflictingConnectionMethodSolution.initialize(any(DeviceConfigConflictMappingImpl.class), any(PartialConnectionTask.class))).thenReturn(conflictingConnectionMethodSolution);
    }

    @Test
    public void verifyEventIsCreatedAtInitializationTest() {
        final DeviceConfigConflictMappingImpl deviceConfigConflictMapping = new DeviceConfigConflictMappingImpl(dataModel, eventService).initialize(deviceType, origin, destination);
        verify(eventService, times(1)).postEvent(EventType.DEVICE_CONFIG_CONFLICT_VALIDATE_CREATE.topic(), deviceConfigConflictMapping);
    }

    @Test
    public void verifyEventIsCreatedWhenNewConnectionMethodConflictIsAddedTest() {
        final DeviceConfigConflictMappingImpl deviceConfigConflictMapping = new DeviceConfigConflictMappingImpl(dataModel, eventService).initialize(deviceType, origin, destination);
        deviceConfigConflictMapping.newConflictingConnectionMethods(partialConnectionTask);
        verify(eventService, times(2)).postEvent(EventType.DEVICE_CONFIG_CONFLICT_VALIDATE_CREATE.topic(), deviceConfigConflictMapping);
    }

    @Test
    public void verifyEventIsCreatedWhenNewSecurityPropertyConflictIsAddedTest() {
        final DeviceConfigConflictMappingImpl deviceConfigConflictMapping = new DeviceConfigConflictMappingImpl(dataModel, eventService).initialize(deviceType, origin, destination);
        deviceConfigConflictMapping.newConflictingSecurityPropertySets(securityPropertySet);
        verify(eventService, times(2)).postEvent(EventType.DEVICE_CONFIG_CONFLICT_VALIDATE_CREATE.topic(), deviceConfigConflictMapping);
    }

    @Test
    public void verifyEventIsNOTTriggeredWhenConnectionMethodConflictIsSolvedAsRemovedTest() {
        final DeviceConfigConflictMappingImpl deviceConfigConflictMapping = new DeviceConfigConflictMappingImpl(dataModel, eventService).initialize(deviceType, origin, destination);
        final ConflictingConnectionMethodSolution conflictingConnectionMethodSolution = deviceConfigConflictMapping.newConflictingConnectionMethods(partialConnectionTask);
        verify(eventService, times(2)).postEvent(EventType.DEVICE_CONFIG_CONFLICT_VALIDATE_CREATE.topic(), deviceConfigConflictMapping);

        conflictingConnectionMethodSolution.markSolutionAsRemove();
        verify(eventService, times(2)).postEvent(EventType.DEVICE_CONFIG_CONFLICT_VALIDATE_CREATE.topic(), deviceConfigConflictMapping);
    }

    @Test
    public void verifyEventIsNOTTriggeredWhenConnectionMethodConflictIsSolvedAsMapTest() {
        final DeviceConfigConflictMappingImpl deviceConfigConflictMapping = new DeviceConfigConflictMappingImpl(dataModel, eventService).initialize(deviceType, origin, destination);
        final ConflictingConnectionMethodSolution conflictingConnectionMethodSolution = deviceConfigConflictMapping.newConflictingConnectionMethods(partialConnectionTask);
        verify(eventService, times(2)).postEvent(EventType.DEVICE_CONFIG_CONFLICT_VALIDATE_CREATE.topic(), deviceConfigConflictMapping);

        conflictingConnectionMethodSolution.markSolutionAsMap(otherPartialConnectionTask);
        verify(eventService, times(2)).postEvent(EventType.DEVICE_CONFIG_CONFLICT_VALIDATE_CREATE.topic(), deviceConfigConflictMapping);
    }

    @Test
    public void verifyEventIsNOTTriggeredWhenSecurityPropertySetConflictIsSolvedAsMapTest() {
        final DeviceConfigConflictMappingImpl deviceConfigConflictMapping = new DeviceConfigConflictMappingImpl(dataModel, eventService).initialize(deviceType, origin, destination);
        final ConflictingSecuritySetSolution conflictingSecuritySetSolution = deviceConfigConflictMapping.newConflictingSecurityPropertySets(securityPropertySet);
        verify(eventService, times(2)).postEvent(EventType.DEVICE_CONFIG_CONFLICT_VALIDATE_CREATE.topic(), deviceConfigConflictMapping);

        conflictingSecuritySetSolution.markSolutionAsMap(otherSecurityPropertySet);
        verify(eventService, times(2)).postEvent(EventType.DEVICE_CONFIG_CONFLICT_VALIDATE_CREATE.topic(), deviceConfigConflictMapping);
    }
}
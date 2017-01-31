/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.events.LocalEvent;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.impl.ServerDeviceType;
import com.energyict.mdc.device.config.impl.deviceconfigchange.DeviceConfigConflictMappingImpl;
import com.energyict.mdc.device.data.impl.configchange.ConflictCreationEventHandler;
import com.energyict.mdc.device.data.impl.configchange.DeviceConfigChangeRequestImpl;
import com.energyict.mdc.device.data.impl.configchange.VetoCreateNewConflictForActiveConfigChange;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DeviceConfigCRUDLockingTest extends PersistenceIntegrationTest {

    private LocalEvent mockLocalEvent(String topic, Object source) {
        LocalEvent localEvent = mock(LocalEvent.class);
        com.elster.jupiter.events.EventType eventType = mock(com.elster.jupiter.events.EventType.class);
        when(localEvent.getType()).thenReturn(eventType);
        when(eventType.getTopic()).thenReturn(topic);
        when(localEvent.getSource()).thenReturn(source);
        return localEvent;
    }

    @Test(expected = VetoCreateNewConflictForActiveConfigChange.class)
    @Transactional
    public void eventHandlerGeneratesProperVetoWhenLockExists() {
        final DeviceConfiguration myConfig = deviceType.newConfiguration("MyConfig").add();
        myConfig.save();
        final DeviceConfigConflictMappingImpl deviceConfigConflictMapping = ((ServerDeviceType) deviceType).newConflictMappingFor(myConfig, myConfig);
        final LocalEvent localEvent = mockLocalEvent(com.energyict.mdc.device.config.events.EventType.DEVICE_CONFIG_CONFLICT_VALIDATE_CREATE.topic(), deviceConfigConflictMapping);

        final DeviceConfigChangeRequestImpl deviceConfigChangeRequest = inMemoryPersistence.getDeviceDataModelService().dataModel().getInstance(DeviceConfigChangeRequestImpl.class).init(myConfig);
        deviceConfigChangeRequest.save();

        final ConflictCreationEventHandler conflictCreationEventHandler = new ConflictCreationEventHandler();
        conflictCreationEventHandler.setDeviceService(inMemoryPersistence.getDeviceService());
        conflictCreationEventHandler.setThesaurus(inMemoryPersistence.getThesaurusFromDeviceDataModel());
        conflictCreationEventHandler.handle(localEvent);
    }

}

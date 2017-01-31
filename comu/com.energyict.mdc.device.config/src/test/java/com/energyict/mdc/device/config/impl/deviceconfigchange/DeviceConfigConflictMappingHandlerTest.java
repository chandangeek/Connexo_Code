/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config.impl.deviceconfigchange;

import com.elster.jupiter.events.EventType;
import com.elster.jupiter.events.LocalEvent;
import com.energyict.mdc.device.config.DeviceConfigConflictMapping;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.PartialConnectionTask;
import com.energyict.mdc.device.config.SecurityPropertySet;
import com.energyict.mdc.device.config.events.PartialConnectionTaskUpdateDetails;
import com.energyict.mdc.device.config.impl.ServerDeviceType;

import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DeviceConfigConflictMappingHandlerTest {

    @Mock
    private DeviceConfiguration deviceConfiguration;
    @Mock
    private ServerDeviceType deviceType;

    @Before
    public void setup() {
        when(deviceConfiguration.getDeviceType()).thenReturn(deviceType);
    }

    private PartialConnectionTask mockConnectionTask() {
        PartialConnectionTask connectionTask = mock(PartialConnectionTask.class);
        when(connectionTask.getConfiguration()).thenReturn(deviceConfiguration);
        return connectionTask;
    }

    private LocalEvent mockLocalEvent(String topic, Object source) {
        LocalEvent localEvent = mock(LocalEvent.class);
        EventType eventType = mock(EventType.class);
        when(localEvent.getType()).thenReturn(eventType);
        when(eventType.getTopic()).thenReturn(topic);
        when(localEvent.getSource()).thenReturn(source);
        return localEvent;
    }

    private SecurityPropertySet mockSecurityPropertySet() {
        SecurityPropertySet securityPropertySet = mock(SecurityPropertySet.class);
        when(securityPropertySet.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        return securityPropertySet;
    }

    /**
     * We verify this by checking whether or not the removeDeviceConfigConflictMappings is called (normally this is the only place where it happens)
     */
    private void verifyRecalculateWasTriggered() {
        verify(deviceType, times(1)).removeDeviceConfigConflictMappings(Collections.<DeviceConfigConflictMapping>emptyList());
    }

    /**
     * We verify this by checking whether or not the removeDeviceConfigConflictMappings is called (normally this is the only place where it happens)
     */
    private void verifyRecalculateWasNotTriggered() {
        verify(deviceType, never()).removeDeviceConfigConflictMappings(Collections.<DeviceConfigConflictMapping>emptyList());
    }

    @Test
    public void deleteConnectionTaskUpdatesConflictsTest() {
        String topic = "com/energyict/mdc/device/config/partialinboundconnectiontask/DELETED";
        PartialConnectionTask connectionTask = mockConnectionTask();
        LocalEvent localEvent = mockLocalEvent(topic, connectionTask);

        new DeviceConfigConflictMappingHandler().onEvent(localEvent);

        verifyRecalculateWasTriggered();
    }

    @Test
    public void createConnectionTaskUpdatesConflictsTest() {
        String topic = "com/energyict/mdc/device/config/partialinboundconnectiontask/CREATED";
        PartialConnectionTask connectionTask = mockConnectionTask();
        LocalEvent localEvent = mockLocalEvent(topic, connectionTask);

        new DeviceConfigConflictMappingHandler().onEvent(localEvent);

        verifyRecalculateWasTriggered();
    }

    @Test
    public void updateConnectionTaskUpdatesConflictsTest() {
        String topic = "com/energyict/mdc/device/config/partialinboundconnectiontask/UPDATED";
        PartialConnectionTask connectionTask = mockConnectionTask();
        PartialConnectionTaskUpdateDetails partialConnectionTaskUpdateDetails = mock(PartialConnectionTaskUpdateDetails.class);
        when(partialConnectionTaskUpdateDetails.getPartialConnectionTask()).thenReturn(connectionTask);
        LocalEvent localEvent = mockLocalEvent(topic, partialConnectionTaskUpdateDetails);

        new DeviceConfigConflictMappingHandler().onEvent(localEvent);

        verifyRecalculateWasTriggered();
    }

    @Test
    public void deleteSecurityPropertySetUpdatesConflictsTest() {
        String topic = "com/energyict/mdc/device/config/securitypropertyset/DELETED";
        SecurityPropertySet securityPropertySet = mockSecurityPropertySet();
        LocalEvent localEvent = mockLocalEvent(topic, securityPropertySet);

        new DeviceConfigConflictMappingHandler().onEvent(localEvent);

        verifyRecalculateWasTriggered();
    }

    @Test
    public void createSecurityPropertySetUpdatesConflictsTest() {
        String topic = "com/energyict/mdc/device/config/securitypropertyset/CREATED";
        SecurityPropertySet securityPropertySet = mockSecurityPropertySet();
        LocalEvent localEvent = mockLocalEvent(topic, securityPropertySet);

        new DeviceConfigConflictMappingHandler().onEvent(localEvent);

        verifyRecalculateWasTriggered();
    }

    @Test
    public void updateSecurityPropertySetUpdatesConflictsTest() {
        String topic = "com/energyict/mdc/device/config/securitypropertyset/UPDATED";
        SecurityPropertySet securityPropertySet = mockSecurityPropertySet();
        LocalEvent localEvent = mockLocalEvent(topic, securityPropertySet);

        new DeviceConfigConflictMappingHandler().onEvent(localEvent);

        verifyRecalculateWasTriggered();
    }

    @Test
    public void activateDeviceConfigurationUpdatesConflictsTest() {
        String topic = "com/energyict/mdc/device/config/deviceconfiguration/ACTIVATED";
        LocalEvent localEvent = mockLocalEvent(topic, deviceConfiguration);

        new DeviceConfigConflictMappingHandler().onEvent(localEvent);

        verifyRecalculateWasTriggered();
    }

    @Test
    public void deactivateDeviceConfigurationUpdatesConflictsTest() {
        String topic = "com/energyict/mdc/device/config/deviceconfiguration/DEACTIVATED";
        LocalEvent localEvent = mockLocalEvent(topic, deviceConfiguration);

        new DeviceConfigConflictMappingHandler().onEvent(localEvent);

        verifyRecalculateWasTriggered();
    }

    @Test
    public void createDeviceConfigDoesNotUpdateConflictsTest() {
        String topic = "com/energyict/mdc/device/config/deviceconfiguration/CREATED";
        LocalEvent localEvent = mockLocalEvent(topic, deviceConfiguration);

        new DeviceConfigConflictMappingHandler().onEvent(localEvent);

        verifyRecalculateWasNotTriggered();
    }

    @Test // the update should have happened when we deactivated the config
    public void deleteDeviceConfigDoesNotUpdateConflictsTest() {
        String topic = "com/energyict/mdc/device/config/deviceconfiguration/DELETED";
        LocalEvent localEvent = mockLocalEvent(topic, deviceConfiguration);

        new DeviceConfigConflictMappingHandler().onEvent(localEvent);

        verifyRecalculateWasNotTriggered();
    }

    @Test
    public void updateDeviceConfigDoesNotUpdateConflictsTest() {
        String topic = "com/energyict/mdc/device/config/deviceconfiguration/UPDATED";
        LocalEvent localEvent = mockLocalEvent(topic, deviceConfiguration);

        new DeviceConfigConflictMappingHandler().onEvent(localEvent);

        verifyRecalculateWasNotTriggered();
    }
}
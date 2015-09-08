package com.energyict.mdc.device.config.impl.deviceconfigchange;

import com.elster.jupiter.events.EventType;
import com.elster.jupiter.events.LocalEvent;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.PartialConnectionTask;
import com.energyict.mdc.device.config.SecurityPropertySet;
import com.energyict.mdc.device.config.impl.ServerDeviceType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.*;

/**
 * Copyrights EnergyICT
 * Date: 07.09.15
 * Time: 12:16
 */
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

    @Test
    public void deleteConnectionTaskUpdatesConflictsTest() {
        String topic = "com/energyict/mdc/device/config/partialinboundconnectiontask/DELETED";
        PartialConnectionTask connectionTask = mockConnectionTask();
        LocalEvent localEvent = mockLocalEvent(topic, connectionTask);

        new DeviceConfigConflictMappingHandler().onEvent(localEvent);

        verify(deviceType).updateConflictingMappings();
    }

    @Test
    public void createConnectionTaskUpdatesConflictsTest() {
        String topic = "com/energyict/mdc/device/config/partialinboundconnectiontask/CREATED";
        PartialConnectionTask connectionTask = mockConnectionTask();
        LocalEvent localEvent = mockLocalEvent(topic, connectionTask);

        new DeviceConfigConflictMappingHandler().onEvent(localEvent);

        verify(deviceType).updateConflictingMappings();
    }

    @Test
    public void updateConnectionTaskDoesntUpdateConflictsTest() {
        String topic = "com/energyict/mdc/device/config/partialinboundconnectiontask/UPDATED";
        PartialConnectionTask connectionTask = mockConnectionTask();
        LocalEvent localEvent = mockLocalEvent(topic, connectionTask);

        new DeviceConfigConflictMappingHandler().onEvent(localEvent);

        verify(deviceType).updateConflictingMappings();
    }

    @Test
    public void deleteSecurityPropertySetUpdatesConflictsTest() {
        String topic = "com/energyict/mdc/device/config/securitypropertyset/DELETED";
        SecurityPropertySet securityPropertySet = mockSecurityPropertySet();
        LocalEvent localEvent = mockLocalEvent(topic, securityPropertySet);

        new DeviceConfigConflictMappingHandler().onEvent(localEvent);

        verify(deviceType).updateConflictingMappings();
    }

    @Test
    public void createSecurityPropertySetUpdatesConflictsTest() {
        String topic = "com/energyict/mdc/device/config/securitypropertyset/CREATED";
        SecurityPropertySet securityPropertySet = mockSecurityPropertySet();
        LocalEvent localEvent = mockLocalEvent(topic, securityPropertySet);

        new DeviceConfigConflictMappingHandler().onEvent(localEvent);

        verify(deviceType).updateConflictingMappings();
    }

    @Test
    public void updateSecurityPropertySetDoesntUpdateConflictsTest() {
        String topic = "com/energyict/mdc/device/config/securitypropertyset/UPDATED";
        SecurityPropertySet securityPropertySet = mockSecurityPropertySet();
        LocalEvent localEvent = mockLocalEvent(topic, securityPropertySet);

        new DeviceConfigConflictMappingHandler().onEvent(localEvent);

        verify(deviceType).updateConflictingMappings();
    }

    @Test
    public void activateDeviceConfigurationUpdatesConflictsTest() {
        String topic = "com/energyict/mdc/device/config/deviceconfiguration/ACTIVATED";
        LocalEvent localEvent = mockLocalEvent(topic, deviceConfiguration);

        new DeviceConfigConflictMappingHandler().onEvent(localEvent);

        verify(deviceType).updateConflictingMappings();
    }

    @Test
    public void deactivateDeviceConfigurationUpdatesConflictsTest() {
        String topic = "com/energyict/mdc/device/config/deviceconfiguration/DEACTIVATED";
        LocalEvent localEvent = mockLocalEvent(topic, deviceConfiguration);

        new DeviceConfigConflictMappingHandler().onEvent(localEvent);

        verify(deviceType).updateConflictingMappings();
    }

    @Test
    public void createDeviceConfigDoesNotUpdateConflictsTest() {
        String topic = "com/energyict/mdc/device/config/deviceconfiguration/CREATED";
        LocalEvent localEvent = mockLocalEvent(topic, deviceConfiguration);

        new DeviceConfigConflictMappingHandler().onEvent(localEvent);

        verify(deviceType, never()).updateConflictingMappings();
    }

    @Test // the update should have happened when we deactivated the config
    public void deleteDeviceConfigDoesNotUpdateConflictsTest() {
        String topic = "com/energyict/mdc/device/config/deviceconfiguration/DELETED";
        LocalEvent localEvent = mockLocalEvent(topic, deviceConfiguration);

        new DeviceConfigConflictMappingHandler().onEvent(localEvent);

        verify(deviceType, never()).updateConflictingMappings();
    }

    @Test
    public void updateDeviceConfigDoesNotUpdateConflictsTest() {
        String topic = "com/energyict/mdc/device/config/deviceconfiguration/UPDATED";
        LocalEvent localEvent = mockLocalEvent(topic, deviceConfiguration);

        new DeviceConfigConflictMappingHandler().onEvent(localEvent);

        verify(deviceType, never()).updateConflictingMappings();
    }
}
/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.configuration.rest.impl;

import com.energyict.mdc.device.config.ConflictingConnectionMethodSolution;
import com.energyict.mdc.device.config.ConflictingSecuritySetSolution;
import com.energyict.mdc.device.config.DeviceConfigConflictMapping;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.PartialConnectionTask;
import com.energyict.mdc.device.config.SecurityPropertySet;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycle;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DeviceConfigConflictMappingResourceTest extends DeviceConfigurationApplicationJerseyTest {

    public static final long OK_VERSION = 24L;
    public static final long BAD_VERSION = 17L;

    private DeviceType mockDeviceType(long id) {
        DeviceType deviceType = mock(DeviceType.class);
        when(deviceType.getName()).thenReturn("Device type " + id);
        when(deviceType.getId()).thenReturn(id);
        DeviceProtocolPluggableClass deviceProtocolPluggableClass = mock(DeviceProtocolPluggableClass.class);
        when(deviceType.getDeviceProtocolPluggableClass()).thenReturn(Optional.of(deviceProtocolPluggableClass));
        DeviceProtocol deviceProtocol = mock(DeviceProtocol.class);
        when(deviceProtocolPluggableClass.getDeviceProtocol()).thenReturn(deviceProtocol);
        DeviceLifeCycle deviceLifeCycle = mockStandardDeviceLifeCycle();
        when(deviceType.getDeviceLifeCycle()).thenReturn(deviceLifeCycle);
        List<DeviceConfiguration> deviceConfigurations = new ArrayList<>();
        when(deviceType.getConfigurations()).thenReturn(deviceConfigurations);
        when(deviceType.getVersion()).thenReturn(OK_VERSION);

        doReturn(Optional.of(deviceType)).when(deviceConfigurationService).findDeviceType(id);
        doReturn(Optional.of(deviceType)).when(deviceConfigurationService).findAndLockDeviceType(id, OK_VERSION);
        doReturn(Optional.empty()).when(deviceConfigurationService).findAndLockDeviceType(id, BAD_VERSION);

        return deviceType;
    }

    @Test
    public void getDeviceConfigConflictMappingsForDeviceTypeTest() throws Exception {
        DeviceType deviceType = mockDeviceType(100500L);

        DeviceConfiguration deviceConfigurationOrigin = mock(DeviceConfiguration.class);
        DeviceConfiguration deviceConfigurationDestination = mock(DeviceConfiguration.class);
        DeviceConfigConflictMapping deviceConfigConflictMapping = mock(DeviceConfigConflictMapping.class);

        when(deviceConfigConflictMapping.getId()).thenReturn(7L);
        when(deviceConfigConflictMapping.isSolved()).thenReturn(false);
        when(deviceConfigConflictMapping.getOriginDeviceConfiguration()).thenReturn(deviceConfigurationOrigin);
        when(deviceConfigConflictMapping.getDestinationDeviceConfiguration()).thenReturn(deviceConfigurationDestination);
        when(deviceConfigConflictMapping.getOriginDeviceConfiguration().getName()).thenReturn("origin");
        when(deviceConfigConflictMapping.getDestinationDeviceConfiguration().getName()).thenReturn("destination");
        when(deviceConfigConflictMapping.getDeviceType()).thenReturn(deviceType);
        when(deviceConfigConflictMapping.getVersion()).thenReturn(OK_VERSION);
        when(deviceType.getDeviceConfigConflictMappings()).thenReturn(Arrays.asList(deviceConfigConflictMapping));

        Map<String, Object> map = target("/devicetypes/100500/conflictmappings").request().get(Map.class);
        assertThat(map.get("total")).describedAs("JSon representation of a field, JavaScript impact if it changed").isEqualTo(1);
        assertThat((List) map.get("conflictMapping")).hasSize(1).describedAs("JSon representation of a field, JavaScript impact if it changed");
        Map jsonConflictMapping = (Map) ((List) map.get("conflictMapping")).get(0);
        assertThat(jsonConflictMapping.get("id")).isEqualTo(7).describedAs("JSon representation of a field, JavaScript impact if it changed");
        assertThat(jsonConflictMapping.get("isSolved")).isEqualTo(false).describedAs("JSon representation of a field, JavaScript impact if it changed");
        assertThat(jsonConflictMapping.get("fromConfiguration")).isEqualTo("origin").describedAs("JSon representation of a field, JavaScript impact if it changed");
        assertThat(jsonConflictMapping.get("toConfiguration")).isEqualTo("destination").describedAs("JSon representation of a field, JavaScript impact if it changed");
        assertThat(jsonConflictMapping.get("version")).isEqualTo(((Number) OK_VERSION).intValue());
        assertThat(jsonConflictMapping.get("parent")).isNotNull();
    }

    @Test
    public void getDeviceConfigConflictByIdTest() throws Exception {
        DeviceType deviceType = mockDeviceType(100500L);
        DeviceConfigConflictMapping deviceConfigConflictMapping = mock(DeviceConfigConflictMapping.class);
        when(deviceConfigurationService.findDeviceConfigConflictMapping(13L)).thenReturn(Optional.of(deviceConfigConflictMapping));
        when(deviceConfigurationService.findAndLockDeviceConfigConflictMappingByIdAndVersion(13L, OK_VERSION)).thenReturn(Optional.of(deviceConfigConflictMapping));
        when(deviceConfigurationService.findAndLockDeviceConfigConflictMappingByIdAndVersion(13L, BAD_VERSION)).thenReturn(Optional.empty());

        DeviceConfiguration deviceConfigurationOrigin = mock(DeviceConfiguration.class);
        when(deviceConfigConflictMapping.getOriginDeviceConfiguration()).thenReturn(deviceConfigurationOrigin);

        DeviceConfiguration deviceConfigurationDestination = mock(DeviceConfiguration.class);
        when(deviceConfigConflictMapping.getDestinationDeviceConfiguration()).thenReturn(deviceConfigurationDestination);

        ConflictingConnectionMethodSolution conflictingConnectionMethodSolution = mock(ConflictingConnectionMethodSolution.class);
        when(deviceConfigConflictMapping.getConflictingConnectionMethodSolutions()).thenReturn(Arrays.asList(conflictingConnectionMethodSolution));

        PartialConnectionTask connectionTaskOrigin = mock(PartialConnectionTask.class);
        when(conflictingConnectionMethodSolution.getOriginDataSource()).thenReturn(connectionTaskOrigin);

        PartialConnectionTask connectionTaskDestination = mock(PartialConnectionTask.class);
        when(conflictingConnectionMethodSolution.getDestinationDataSource()).thenReturn(connectionTaskDestination);

        ConflictingSecuritySetSolution conflictingSecuritySetSolution = mock(ConflictingSecuritySetSolution.class);
        when(deviceConfigConflictMapping.getConflictingSecuritySetSolutions()).thenReturn(Arrays.asList(conflictingSecuritySetSolution));

        SecurityPropertySet securityTaskOrigin = mock(SecurityPropertySet.class);
        when(conflictingSecuritySetSolution.getOriginDataSource()).thenReturn(securityTaskOrigin);

        SecurityPropertySet securityTaskDestination = mock(SecurityPropertySet.class);
        when(conflictingSecuritySetSolution.getDestinationDataSource()).thenReturn(securityTaskDestination);

        PartialConnectionTask mappableConnectionleTask = mock(PartialConnectionTask.class);
        when(conflictingConnectionMethodSolution.getMappableToDataSources()).thenReturn(Arrays.asList(mappableConnectionleTask));

        SecurityPropertySet mappableSecurityTask = mock(SecurityPropertySet.class);
        when(conflictingSecuritySetSolution.getMappableToDataSources()).thenReturn(Arrays.asList(mappableSecurityTask));

        when(deviceConfigConflictMapping.getId()).thenReturn(13L);
        when(deviceConfigConflictMapping.isSolved()).thenReturn(false);
        when(deviceConfigConflictMapping.getOriginDeviceConfiguration().getId()).thenReturn(1L);
        when(deviceConfigConflictMapping.getDestinationDeviceConfiguration().getId()).thenReturn(2L);
        when(deviceConfigConflictMapping.getOriginDeviceConfiguration().getName()).thenReturn("origin");
        when(deviceConfigConflictMapping.getDestinationDeviceConfiguration().getName()).thenReturn("destination");
        when(deviceConfigConflictMapping.getDeviceType()).thenReturn(deviceType);
        when(deviceConfigConflictMapping.getVersion()).thenReturn(OK_VERSION);
        when(mappableConnectionleTask.getId()).thenReturn(50L);
        when(mappableConnectionleTask.getName()).thenReturn("fifty");
        when(mappableSecurityTask.getId()).thenReturn(60L);
        when(mappableSecurityTask.getName()).thenReturn("sixty");
        when(connectionTaskOrigin.getId()).thenReturn(10L);
        when(connectionTaskOrigin.getName()).thenReturn("ten");
        when(connectionTaskDestination.getId()).thenReturn(20L);
        when(connectionTaskDestination.getName()).thenReturn("twenty");
        when(securityTaskOrigin.getId()).thenReturn(30L);
        when(securityTaskOrigin.getName()).thenReturn("thirty");
        when(securityTaskDestination.getId()).thenReturn(40L);
        when(securityTaskDestination.getName()).thenReturn("forty");
        when(conflictingConnectionMethodSolution.getConflictingMappingAction()).thenReturn(DeviceConfigConflictMapping.ConflictingMappingAction.MAP);
        when(conflictingSecuritySetSolution.getConflictingMappingAction()).thenReturn(DeviceConfigConflictMapping.ConflictingMappingAction.MAP);

        Map<String, Object> map = target("/devicetypes/100500/conflictmappings/13").request().get(Map.class);
        assertThat(map.get("id")).isEqualTo(13).describedAs("JSon representation of a field, JavaScript impact if it changed");
    }
}
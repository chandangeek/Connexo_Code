package com.energyict.mdc.device.configuration.rest.impl;

import com.energyict.mdc.device.config.*;
import org.junit.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DeviceConfigConflictMappingResourceTest extends DeviceConfigurationApplicationJerseyTest {

    @Test
    public void getDeviceConfigConflictMappingsForDeviceTypeTest() throws Exception {
        DeviceType deviceType = mock(DeviceType.class);
        when(deviceConfigurationService.findDeviceType(100500L)).thenReturn(Optional.of(deviceType));

        DeviceConfiguration deviceConfigurationOrigin = mock(DeviceConfiguration.class);
        DeviceConfiguration deviceConfigurationDestination = mock(DeviceConfiguration.class);
        DeviceConfigConflictMapping deviceConfigConflictMapping = mock(DeviceConfigConflictMapping.class);

        when(deviceConfigConflictMapping.getId()).thenReturn(7L);
        when(deviceConfigConflictMapping.isSolved()).thenReturn(false);
        when(deviceConfigConflictMapping.getOriginDeviceConfiguration()).thenReturn(deviceConfigurationOrigin);
        when(deviceConfigConflictMapping.getDestinationDeviceConfiguration()).thenReturn(deviceConfigurationDestination);
        when(deviceConfigConflictMapping.getOriginDeviceConfiguration().getName()).thenReturn("origin");
        when(deviceConfigConflictMapping.getDestinationDeviceConfiguration().getName()).thenReturn("destination");
        when(deviceType.getDeviceConfigConflictMappings()).thenReturn(Arrays.asList(deviceConfigConflictMapping));

        Map<String, Object> map = target("/devicetypes/100500/conflictmappings").request().get(Map.class);
        assertThat(map.get("total")).describedAs("JSon representation of a field, JavaScript impact if it changed").isEqualTo(1);
        assertThat((List) map.get("conflictMapping")).hasSize(1).describedAs("JSon representation of a field, JavaScript impact if it changed");
        Map jsonConflictMapping = (Map) ((List) map.get("conflictMapping")).get(0);
        assertThat(jsonConflictMapping.get("id")).isEqualTo(7).describedAs("JSon representation of a field, JavaScript impact if it changed");
        assertThat(jsonConflictMapping.get("isSolved")).isEqualTo(false).describedAs("JSon representation of a field, JavaScript impact if it changed");
        assertThat(jsonConflictMapping.get("fromConfiguration")).isEqualTo("origin").describedAs("JSon representation of a field, JavaScript impact if it changed");
        assertThat(jsonConflictMapping.get("toConfiguration")).isEqualTo("destination").describedAs("JSon representation of a field, JavaScript impact if it changed");
    }

    @Test
    public void getDeviceConfigConflictByIdTest() throws Exception {
        DeviceConfigConflictMapping deviceConfigConflictMapping = mock(DeviceConfigConflictMapping.class);
        when(deviceConfigurationService.findDeviceConfigConflictMapping(13L)).thenReturn(Optional.of(deviceConfigConflictMapping));

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
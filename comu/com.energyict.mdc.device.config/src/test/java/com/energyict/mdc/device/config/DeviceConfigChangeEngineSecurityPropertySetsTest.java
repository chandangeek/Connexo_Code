/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config;

import com.energyict.mdc.protocol.api.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.protocol.api.security.EncryptionDeviceAccessLevel;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DeviceConfigChangeEngineSecurityPropertySetsTest {

    private long incrementalConfigId = 1;
    private long incrementalSecuritySetId = 1;

    private DeviceType mockDeviceType() {
        return mock(DeviceType.class);
    }

    @Test
    public void deviceTypeHasTwoConfigsWithExactlyOneSecuritySetThatMatchesTest() {
        String name = "MySecurityPropertySet";
        AuthenticationDeviceAccessLevel authenticationDeviceAccessLevel = mockAuthenticationLevel(102);
        EncryptionDeviceAccessLevel encryptionDeviceAccessLevel = mockEncryptionLevel(44856);
        DeviceType deviceType = mockDeviceType();
        DeviceConfiguration deviceConfiguration1 = mockActiveDeviceConfiguration();
        SecurityPropertySet securityPropertySet1 = mockSecurityPropertySet(name, authenticationDeviceAccessLevel, encryptionDeviceAccessLevel);
        when(deviceConfiguration1.getSecurityPropertySets()).thenReturn(Collections.singletonList(securityPropertySet1));
        DeviceConfiguration deviceConfiguration2 = mockActiveDeviceConfiguration();
        SecurityPropertySet securityPropertySet2 = mockSecurityPropertySet(name, authenticationDeviceAccessLevel, encryptionDeviceAccessLevel);
        when(deviceConfiguration2.getSecurityPropertySets()).thenReturn(Collections.singletonList(securityPropertySet2));
        when(deviceType.getConfigurations()).thenReturn(Arrays.asList(deviceConfiguration1, deviceConfiguration2));
        List<DeviceConfigChangeAction> deviceConfigChangeActions = DeviceConfigChangeEngine.INSTANCE.calculateDeviceConfigChangeActionsForConflicts(deviceType);

        assertThat(deviceConfigChangeActions).isEmpty();
    }

    private AuthenticationDeviceAccessLevel mockAuthenticationLevel(int id) {
        AuthenticationDeviceAccessLevel authenticationDeviceAccessLevel = mock(AuthenticationDeviceAccessLevel.class);
        when(authenticationDeviceAccessLevel.getId()).thenReturn(id);
        return authenticationDeviceAccessLevel;
    }

    @Test
    public void deviceTypeHasTwoConfigsWithSecurityPropertySetsThatDontMatchAtAllTest() {
        String name1 = "MySecurityPropertySet1";
        String name2 = "MySecurityPropertySet2";

        AuthenticationDeviceAccessLevel authenticationDeviceAccessLevel1 = mockAuthenticationLevel(102);
        EncryptionDeviceAccessLevel encryptionDeviceAccessLevel1 = mockEncryptionLevel(44856);
        AuthenticationDeviceAccessLevel authenticationDeviceAccessLevel2 = mockAuthenticationLevel(4456);
        EncryptionDeviceAccessLevel encryptionDeviceAccessLevel2 = mockEncryptionLevel(744565);
        DeviceType deviceType = mockDeviceType();
        DeviceConfiguration deviceConfiguration1 = mockActiveDeviceConfiguration();
        SecurityPropertySet securityPropertySet1 = mockSecurityPropertySet(name1, authenticationDeviceAccessLevel1, encryptionDeviceAccessLevel1);
        when(deviceConfiguration1.getSecurityPropertySets()).thenReturn(Collections.singletonList(securityPropertySet1));
        DeviceConfiguration deviceConfiguration2 = mockActiveDeviceConfiguration();
        SecurityPropertySet securityPropertySet2 = mockSecurityPropertySet(name2, authenticationDeviceAccessLevel2, encryptionDeviceAccessLevel2);
        when(deviceConfiguration2.getSecurityPropertySets()).thenReturn(Collections.singletonList(securityPropertySet2));
        when(deviceType.getConfigurations()).thenReturn(Arrays.asList(deviceConfiguration1, deviceConfiguration2));
        List<DeviceConfigChangeAction> deviceConfigChangeActions = DeviceConfigChangeEngine.INSTANCE.calculateDeviceConfigChangeActionsForConflicts(deviceType);

        assertThat(deviceConfigChangeActions).isEmpty();
    }

    private EncryptionDeviceAccessLevel mockEncryptionLevel(int id) {
        EncryptionDeviceAccessLevel encryptionDeviceAccessLevel = mock(EncryptionDeviceAccessLevel.class);
        when(encryptionDeviceAccessLevel.getId()).thenReturn(id);
        return encryptionDeviceAccessLevel;
    }

    private boolean matchSecurityPropertySet(DeviceConfigChangeAction deviceConfigChangeAction, DeviceConfiguration originDeviceConfiguration, DeviceConfiguration destinationDeviceConfiguration, SecurityPropertySet origin, SecurityPropertySet destination, DeviceConfigChangeActionType actionType) {
        return deviceConfigChangeAction.getOriginDeviceConfiguration().equals(originDeviceConfiguration)
                && deviceConfigChangeAction.getDestinationDeviceConfiguration().equals(destinationDeviceConfiguration)
                && (deviceConfigChangeAction.getOrigin() == null || deviceConfigChangeAction.getOrigin().equals(origin))
                && (deviceConfigChangeAction.getDestination() == null || deviceConfigChangeAction.getDestination().equals(destination))
                && deviceConfigChangeAction.getActionType().equals(actionType);
    }


    private SecurityPropertySet mockSecurityPropertySet(String name, AuthenticationDeviceAccessLevel authenticationDeviceAccessLevel, EncryptionDeviceAccessLevel encryptionDeviceAccessLevel) {
        SecurityPropertySet securityPropertySet = mock(SecurityPropertySet.class);
        when(securityPropertySet.getName()).thenReturn(name);
        when(securityPropertySet.getAuthenticationDeviceAccessLevel()).thenReturn(authenticationDeviceAccessLevel);
        when(securityPropertySet.getEncryptionDeviceAccessLevel()).thenReturn(encryptionDeviceAccessLevel);
        when(securityPropertySet.toString()).thenReturn("SS - " + incrementalSecuritySetId + " - " + name);
        when(securityPropertySet.getId()).thenReturn(incrementalSecuritySetId++);
        return securityPropertySet;
    }


    private DeviceConfiguration mockActiveDeviceConfiguration() {
        DeviceConfiguration deviceConfiguration = mock(DeviceConfiguration.class);
        when(deviceConfiguration.isActive()).thenReturn(true);
        when(deviceConfiguration.toString()).thenReturn("DC - " + incrementalConfigId);
        when(deviceConfiguration.getId()).thenReturn(incrementalConfigId++);
        return deviceConfiguration;
    }

}
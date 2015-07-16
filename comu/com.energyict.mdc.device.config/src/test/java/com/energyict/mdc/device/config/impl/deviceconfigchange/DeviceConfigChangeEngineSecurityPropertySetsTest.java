package com.energyict.mdc.device.config.impl.deviceconfigchange;

import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.SecurityPropertySet;
import com.energyict.mdc.protocol.api.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.protocol.api.security.EncryptionDeviceAccessLevel;
import org.fest.assertions.core.Condition;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Copyrights EnergyICT
 * Date: 16/07/15
 * Time: 15:36
 */
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
        AuthenticationDeviceAccessLevel authenticationDeviceAccessLevel = mock(AuthenticationDeviceAccessLevel.class);
        EncryptionDeviceAccessLevel encryptionDeviceAccessLevel = mock(EncryptionDeviceAccessLevel.class);
        DeviceType deviceType = mockDeviceType();
        DeviceConfiguration deviceConfiguration1 = mockActiveDeviceConfiguration();
        SecurityPropertySet securityPropertySet1 = mockSecurityPropertySet(name, authenticationDeviceAccessLevel, encryptionDeviceAccessLevel);
        when(deviceConfiguration1.getSecurityPropertySets()).thenReturn(Collections.singletonList(securityPropertySet1));
        DeviceConfiguration deviceConfiguration2 = mockActiveDeviceConfiguration();
        SecurityPropertySet securityPropertySet2 = mockSecurityPropertySet(name, authenticationDeviceAccessLevel, encryptionDeviceAccessLevel);
        when(deviceConfiguration2.getSecurityPropertySets()).thenReturn(Collections.singletonList(securityPropertySet2));
        when(deviceType.getConfigurations()).thenReturn(Arrays.asList(deviceConfiguration1, deviceConfiguration2));
        DeviceConfigChangeEngine deviceConfigChangeEngine = new DeviceConfigChangeEngine(deviceType);
        deviceConfigChangeEngine.calculateConfigChangeActions();
        assertThat(deviceConfigChangeEngine.getDeviceConfigChangeActions()).hasSize(2);
        assertThat(deviceConfigChangeEngine.getDeviceConfigChangeActions()).haveExactly(1, new Condition<DeviceConfigChangeAction>() {
            @Override
            public boolean matches(DeviceConfigChangeAction deviceConfigChangeAction) {
                return matchSecurityPropertySet(deviceConfigChangeAction, deviceConfiguration1, deviceConfiguration2, securityPropertySet1, securityPropertySet2, DeviceConfigChangeActionType.MATCH);
            }
        });
        assertThat(deviceConfigChangeEngine.getDeviceConfigChangeActions()).haveExactly(1, new Condition<DeviceConfigChangeAction>() {
            @Override
            public boolean matches(DeviceConfigChangeAction deviceConfigChangeAction) {
                return matchSecurityPropertySet(deviceConfigChangeAction, deviceConfiguration2, deviceConfiguration1, securityPropertySet2, securityPropertySet1, DeviceConfigChangeActionType.MATCH);
            }
        });
    }

    @Test
    public void deviceTypeHasTwoConfigsWithSecurityPropertySetsThatDontMatchAtAllTest() {
        String name1 = "MySecurityPropertySet1";
        String name2 = "MySecurityPropertySet2";

        AuthenticationDeviceAccessLevel authenticationDeviceAccessLevel1 = mock(AuthenticationDeviceAccessLevel.class);
        EncryptionDeviceAccessLevel encryptionDeviceAccessLevel1 = mock(EncryptionDeviceAccessLevel.class);
        AuthenticationDeviceAccessLevel authenticationDeviceAccessLevel2 = mock(AuthenticationDeviceAccessLevel.class);
        EncryptionDeviceAccessLevel encryptionDeviceAccessLevel2 = mock(EncryptionDeviceAccessLevel.class);
        DeviceType deviceType = mockDeviceType();
        DeviceConfiguration deviceConfiguration1 = mockActiveDeviceConfiguration();
        SecurityPropertySet securityPropertySet1 = mockSecurityPropertySet(name1, authenticationDeviceAccessLevel1, encryptionDeviceAccessLevel1);
        when(deviceConfiguration1.getSecurityPropertySets()).thenReturn(Collections.singletonList(securityPropertySet1));
        DeviceConfiguration deviceConfiguration2 = mockActiveDeviceConfiguration();
        SecurityPropertySet securityPropertySet2 = mockSecurityPropertySet(name2, authenticationDeviceAccessLevel2, encryptionDeviceAccessLevel2);
        when(deviceConfiguration2.getSecurityPropertySets()).thenReturn(Collections.singletonList(securityPropertySet2));
        when(deviceType.getConfigurations()).thenReturn(Arrays.asList(deviceConfiguration1, deviceConfiguration2));
        DeviceConfigChangeEngine deviceConfigChangeEngine = new DeviceConfigChangeEngine(deviceType);
        deviceConfigChangeEngine.calculateConfigChangeActions();
        assertThat(deviceConfigChangeEngine.getDeviceConfigChangeActions()).hasSize(4);
        assertThat(deviceConfigChangeEngine.getDeviceConfigChangeActions()).haveExactly(1, new Condition<DeviceConfigChangeAction>() {
            @Override
            public boolean matches(DeviceConfigChangeAction deviceConfigChangeAction) {
                return matchSecurityPropertySet(deviceConfigChangeAction, deviceConfiguration1, deviceConfiguration2, securityPropertySet1, null, DeviceConfigChangeActionType.REMOVE);
            }
        });
        assertThat(deviceConfigChangeEngine.getDeviceConfigChangeActions()).haveExactly(1, new Condition<DeviceConfigChangeAction>() {
            @Override
            public boolean matches(DeviceConfigChangeAction deviceConfigChangeAction) {
                return matchSecurityPropertySet(deviceConfigChangeAction, deviceConfiguration1, deviceConfiguration2, null, securityPropertySet2, DeviceConfigChangeActionType.ADD);
            }
        });
        assertThat(deviceConfigChangeEngine.getDeviceConfigChangeActions()).haveExactly(1, new Condition<DeviceConfigChangeAction>() {
            @Override
            public boolean matches(DeviceConfigChangeAction deviceConfigChangeAction) {
                return matchSecurityPropertySet(deviceConfigChangeAction, deviceConfiguration2, deviceConfiguration1, securityPropertySet2, null, DeviceConfigChangeActionType.REMOVE);
            }
        });
        assertThat(deviceConfigChangeEngine.getDeviceConfigChangeActions()).haveExactly(1, new Condition<DeviceConfigChangeAction>() {
            @Override
            public boolean matches(DeviceConfigChangeAction deviceConfigChangeAction) {
                return matchSecurityPropertySet(deviceConfigChangeAction, deviceConfiguration2, deviceConfiguration1, null, securityPropertySet1, DeviceConfigChangeActionType.ADD);
            }
        });
    }

    @Test
    public void deviceTypeHasTwoConfigsWithSecurityPropertySetMatchOnNameAndConflictOnAccessLevelsTest() {
        String name = "MySecurityPropertySet1";

        AuthenticationDeviceAccessLevel authenticationDeviceAccessLevel1 = mock(AuthenticationDeviceAccessLevel.class);
        EncryptionDeviceAccessLevel encryptionDeviceAccessLevel1 = mock(EncryptionDeviceAccessLevel.class);
        AuthenticationDeviceAccessLevel authenticationDeviceAccessLevel2 = mock(AuthenticationDeviceAccessLevel.class);
        EncryptionDeviceAccessLevel encryptionDeviceAccessLevel2 = mock(EncryptionDeviceAccessLevel.class);
        DeviceType deviceType = mockDeviceType();
        DeviceConfiguration deviceConfiguration1 = mockActiveDeviceConfiguration();
        SecurityPropertySet securityPropertySet1 = mockSecurityPropertySet(name, authenticationDeviceAccessLevel1, encryptionDeviceAccessLevel1);
        when(deviceConfiguration1.getSecurityPropertySets()).thenReturn(Collections.singletonList(securityPropertySet1));
        DeviceConfiguration deviceConfiguration2 = mockActiveDeviceConfiguration();
        SecurityPropertySet securityPropertySet2 = mockSecurityPropertySet(name, authenticationDeviceAccessLevel2, encryptionDeviceAccessLevel2);
        when(deviceConfiguration2.getSecurityPropertySets()).thenReturn(Collections.singletonList(securityPropertySet2));
        when(deviceType.getConfigurations()).thenReturn(Arrays.asList(deviceConfiguration1, deviceConfiguration2));
        DeviceConfigChangeEngine deviceConfigChangeEngine = new DeviceConfigChangeEngine(deviceType);
        deviceConfigChangeEngine.calculateConfigChangeActions();
        assertThat(deviceConfigChangeEngine.getDeviceConfigChangeActions()).hasSize(4);
        assertThat(deviceConfigChangeEngine.getDeviceConfigChangeActions()).haveExactly(1, new Condition<DeviceConfigChangeAction>() {
            @Override
            public boolean matches(DeviceConfigChangeAction deviceConfigChangeAction) {
                return matchSecurityPropertySet(deviceConfigChangeAction, deviceConfiguration1, deviceConfiguration2, securityPropertySet1, null, DeviceConfigChangeActionType.REMOVE);
            }
        });
        assertThat(deviceConfigChangeEngine.getDeviceConfigChangeActions()).haveExactly(1, new Condition<DeviceConfigChangeAction>() {
            @Override
            public boolean matches(DeviceConfigChangeAction deviceConfigChangeAction) {
                return matchSecurityPropertySet(deviceConfigChangeAction, deviceConfiguration1, deviceConfiguration2, null, securityPropertySet2, DeviceConfigChangeActionType.ADD);
            }
        });
        assertThat(deviceConfigChangeEngine.getDeviceConfigChangeActions()).haveExactly(1, new Condition<DeviceConfigChangeAction>() {
            @Override
            public boolean matches(DeviceConfigChangeAction deviceConfigChangeAction) {
                return matchSecurityPropertySet(deviceConfigChangeAction, deviceConfiguration2, deviceConfiguration1, securityPropertySet2, null, DeviceConfigChangeActionType.REMOVE);
            }
        });
        assertThat(deviceConfigChangeEngine.getDeviceConfigChangeActions()).haveExactly(1, new Condition<DeviceConfigChangeAction>() {
            @Override
            public boolean matches(DeviceConfigChangeAction deviceConfigChangeAction) {
                return matchSecurityPropertySet(deviceConfigChangeAction, deviceConfiguration2, deviceConfiguration1, null, securityPropertySet1, DeviceConfigChangeActionType.ADD);
            }
        });
    }

    @Test
    public void deviceTypeHasTwoConfigsWithSecurityPropertySetThatMatchOnLevelsAndConflictOnNameTest() {
        String name1 = "LowLevelSecurity";
        String name2 = "EasyPeasySecurity";
        AuthenticationDeviceAccessLevel authenticationDeviceAccessLevel1 = mock(AuthenticationDeviceAccessLevel.class);
        EncryptionDeviceAccessLevel encryptionDeviceAccessLevel1 = mock(EncryptionDeviceAccessLevel.class);

        DeviceType deviceType = mockDeviceType();
        DeviceConfiguration deviceConfiguration1 = mockActiveDeviceConfiguration();
        SecurityPropertySet securityPropertySet1 = mockSecurityPropertySet(name1, authenticationDeviceAccessLevel1, encryptionDeviceAccessLevel1);
        when(deviceConfiguration1.getSecurityPropertySets()).thenReturn(Collections.singletonList(securityPropertySet1));
        DeviceConfiguration deviceConfiguration2 = mockActiveDeviceConfiguration();
        SecurityPropertySet securityPropertySet2 = mockSecurityPropertySet(name2, authenticationDeviceAccessLevel1, encryptionDeviceAccessLevel1);
        when(deviceConfiguration2.getSecurityPropertySets()).thenReturn(Collections.singletonList(securityPropertySet2));
        when(deviceType.getConfigurations()).thenReturn(Arrays.asList(deviceConfiguration1, deviceConfiguration2));
        DeviceConfigChangeEngine deviceConfigChangeEngine = new DeviceConfigChangeEngine(deviceType);
        deviceConfigChangeEngine.calculateConfigChangeActions();
        assertThat(deviceConfigChangeEngine.getDeviceConfigChangeActions()).hasSize(2);
        assertThat(deviceConfigChangeEngine.getDeviceConfigChangeActions()).haveExactly(1, new Condition<DeviceConfigChangeAction>() {
            @Override
            public boolean matches(DeviceConfigChangeAction deviceConfigChangeAction) {
                return matchSecurityPropertySet(deviceConfigChangeAction, deviceConfiguration1, deviceConfiguration2, securityPropertySet1, securityPropertySet2, DeviceConfigChangeActionType.CONFLICT);
            }
        });
        assertThat(deviceConfigChangeEngine.getDeviceConfigChangeActions()).haveExactly(1, new Condition<DeviceConfigChangeAction>() {
            @Override
            public boolean matches(DeviceConfigChangeAction deviceConfigChangeAction) {
                return matchSecurityPropertySet(deviceConfigChangeAction, deviceConfiguration2, deviceConfiguration1, securityPropertySet2, securityPropertySet1, DeviceConfigChangeActionType.CONFLICT);
            }
        });
    }

    @Test
    public void unEvenSecurityPropertySetsOneMatchesExactOneNotAtAllTest() {
        String name1 = "LowLevelSecurity";
        String name2 = "MajorSecurityLevel";
        AuthenticationDeviceAccessLevel authenticationDeviceAccessLevel1 = mock(AuthenticationDeviceAccessLevel.class);
        EncryptionDeviceAccessLevel encryptionDeviceAccessLevel1 = mock(EncryptionDeviceAccessLevel.class);
        AuthenticationDeviceAccessLevel authenticationDeviceAccessLevel2 = mock(AuthenticationDeviceAccessLevel.class);
        EncryptionDeviceAccessLevel encryptionDeviceAccessLevel2 = mock(EncryptionDeviceAccessLevel.class);

        DeviceType deviceType = mockDeviceType();
        DeviceConfiguration deviceConfiguration1 = mockActiveDeviceConfiguration();
        SecurityPropertySet securityPropertySet1 = mockSecurityPropertySet(name1, authenticationDeviceAccessLevel1, encryptionDeviceAccessLevel1);
        SecurityPropertySet securityPropertySet2 = mockSecurityPropertySet(name2, authenticationDeviceAccessLevel2, encryptionDeviceAccessLevel2);
        when(deviceConfiguration1.getSecurityPropertySets()).thenReturn(Arrays.asList(securityPropertySet1, securityPropertySet2));
        DeviceConfiguration deviceConfiguration2 = mockActiveDeviceConfiguration();
        SecurityPropertySet securityPropertySet3 = mockSecurityPropertySet(name1, authenticationDeviceAccessLevel1, encryptionDeviceAccessLevel1);
        when(deviceConfiguration2.getSecurityPropertySets()).thenReturn(Collections.singletonList(securityPropertySet3));
        when(deviceType.getConfigurations()).thenReturn(Arrays.asList(deviceConfiguration1, deviceConfiguration2));
        DeviceConfigChangeEngine deviceConfigChangeEngine = new DeviceConfigChangeEngine(deviceType);
        deviceConfigChangeEngine.calculateConfigChangeActions();
        assertThat(deviceConfigChangeEngine.getDeviceConfigChangeActions()).hasSize(4);
        assertThat(deviceConfigChangeEngine.getDeviceConfigChangeActions()).haveExactly(1, new Condition<DeviceConfigChangeAction>() {
            @Override
            public boolean matches(DeviceConfigChangeAction deviceConfigChangeAction) {
                return matchSecurityPropertySet(deviceConfigChangeAction, deviceConfiguration1, deviceConfiguration2, securityPropertySet2, null, DeviceConfigChangeActionType.REMOVE);
            }
        });
        assertThat(deviceConfigChangeEngine.getDeviceConfigChangeActions()).haveExactly(1, new Condition<DeviceConfigChangeAction>() {
            @Override
            public boolean matches(DeviceConfigChangeAction deviceConfigChangeAction) {
                return matchSecurityPropertySet(deviceConfigChangeAction, deviceConfiguration1, deviceConfiguration2, securityPropertySet1, securityPropertySet3, DeviceConfigChangeActionType.MATCH);
            }
        });
        assertThat(deviceConfigChangeEngine.getDeviceConfigChangeActions()).haveExactly(1, new Condition<DeviceConfigChangeAction>() {
            @Override
            public boolean matches(DeviceConfigChangeAction deviceConfigChangeAction) {
                return matchSecurityPropertySet(deviceConfigChangeAction, deviceConfiguration2, deviceConfiguration1, securityPropertySet3, securityPropertySet1, DeviceConfigChangeActionType.MATCH);
            }
        });
        assertThat(deviceConfigChangeEngine.getDeviceConfigChangeActions()).haveExactly(1, new Condition<DeviceConfigChangeAction>() {
            @Override
            public boolean matches(DeviceConfigChangeAction deviceConfigChangeAction) {
                return matchSecurityPropertySet(deviceConfigChangeAction, deviceConfiguration2, deviceConfiguration1, null, securityPropertySet2, DeviceConfigChangeActionType.ADD);
            }
        });
    }

    @Test
    public void unEvenConnectionTasksOneMatchesExactOneMatchesOnLevelsTest() {
        String name1 = "LowLevelSecurity";
        String name2 = "MajorSecurityLevel";
        AuthenticationDeviceAccessLevel authenticationDeviceAccessLevel1 = mock(AuthenticationDeviceAccessLevel.class);
        EncryptionDeviceAccessLevel encryptionDeviceAccessLevel1 = mock(EncryptionDeviceAccessLevel.class);

        DeviceType deviceType = mockDeviceType();
        DeviceConfiguration deviceConfiguration1 = mockActiveDeviceConfiguration();
        SecurityPropertySet securityPropertySet1 = mockSecurityPropertySet(name1, authenticationDeviceAccessLevel1, encryptionDeviceAccessLevel1);
        SecurityPropertySet securityPropertySet2 = mockSecurityPropertySet(name2, authenticationDeviceAccessLevel1, encryptionDeviceAccessLevel1);
        when(deviceConfiguration1.getSecurityPropertySets()).thenReturn(Arrays.asList(securityPropertySet1, securityPropertySet2));
        DeviceConfiguration deviceConfiguration2 = mockActiveDeviceConfiguration();
        SecurityPropertySet securityPropertySet3 = mockSecurityPropertySet(name1, authenticationDeviceAccessLevel1, encryptionDeviceAccessLevel1);
        when(deviceConfiguration2.getSecurityPropertySets()).thenReturn(Collections.singletonList(securityPropertySet3));
        when(deviceType.getConfigurations()).thenReturn(Arrays.asList(deviceConfiguration1, deviceConfiguration2));
        DeviceConfigChangeEngine deviceConfigChangeEngine = new DeviceConfigChangeEngine(deviceType);
        deviceConfigChangeEngine.calculateConfigChangeActions();
        assertThat(deviceConfigChangeEngine.getDeviceConfigChangeActions()).hasSize(4);
        assertThat(deviceConfigChangeEngine.getDeviceConfigChangeActions()).haveExactly(1, new Condition<DeviceConfigChangeAction>() {
            @Override
            public boolean matches(DeviceConfigChangeAction deviceConfigChangeAction) {
                return matchSecurityPropertySet(deviceConfigChangeAction, deviceConfiguration1, deviceConfiguration2, securityPropertySet2, null, DeviceConfigChangeActionType.REMOVE);
            }
        });
        assertThat(deviceConfigChangeEngine.getDeviceConfigChangeActions()).haveExactly(1, new Condition<DeviceConfigChangeAction>() {
            @Override
            public boolean matches(DeviceConfigChangeAction deviceConfigChangeAction) {
                return matchSecurityPropertySet(deviceConfigChangeAction, deviceConfiguration1, deviceConfiguration2, securityPropertySet1, securityPropertySet3, DeviceConfigChangeActionType.MATCH);
            }
        });
        assertThat(deviceConfigChangeEngine.getDeviceConfigChangeActions()).haveExactly(1, new Condition<DeviceConfigChangeAction>() {
            @Override
            public boolean matches(DeviceConfigChangeAction deviceConfigChangeAction) {
                return matchSecurityPropertySet(deviceConfigChangeAction, deviceConfiguration2, deviceConfiguration1, securityPropertySet3, securityPropertySet1, DeviceConfigChangeActionType.MATCH);
            }
        });
        assertThat(deviceConfigChangeEngine.getDeviceConfigChangeActions()).haveExactly(1, new Condition<DeviceConfigChangeAction>() {
            @Override
            public boolean matches(DeviceConfigChangeAction deviceConfigChangeAction) {
                return matchSecurityPropertySet(deviceConfigChangeAction, deviceConfiguration2, deviceConfiguration1, null, securityPropertySet2, DeviceConfigChangeActionType.ADD);
            }
        });
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
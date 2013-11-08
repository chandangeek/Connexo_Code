package com.energyict.protocolimplv2.security;

import com.energyict.cpo.PropertySpec;
import com.energyict.mdc.protocol.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdw.core.DataVault;
import com.energyict.mdw.core.DataVaultProvider;
import com.energyict.protocolimplv2.security.DeviceSecurityProperty;
import com.energyict.protocolimplv2.security.NoOrPasswordSecuritySupport;
import org.fest.assertions.core.Condition;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Tests for the {@link com.energyict.protocolimplv2.security.NoOrPasswordSecuritySupport} component
 * 
 * Copyrights EnergyICT
 * Date: 31/01/13
 * Time: 14:05
 */
@RunWith(MockitoJUnitRunner.class)
public class NoOrPasswordSecuritySupportTest {

    @Mock
    private DataVaultProvider dataVaultProvider;
    @Mock
    private DataVault dataVault;

    @Before
    public void setUp() {
        DataVaultProvider.instance.set(dataVaultProvider);
        when(dataVaultProvider.getKeyVault()).thenReturn(dataVault);
    }

    @Test
    public void getSecurityPropertiesTest() {
        NoOrPasswordSecuritySupport noOrPasswordSecuritySupport = new NoOrPasswordSecuritySupport();

        // assert that you only have two properties to set
        assertThat(noOrPasswordSecuritySupport.getSecurityProperties()).hasSize(1);

        // check for the password propertySpec
        assertThat(noOrPasswordSecuritySupport.getSecurityProperties()).has(new Condition<List<PropertySpec>>() {
            @Override
            public boolean matches(List<PropertySpec> propertySpecs) {
                boolean match = false;
                for (PropertySpec propertySpec : propertySpecs) {
                    if (propertySpec.equals(DeviceSecurityProperty.PASSWORD.getPropertySpec())) {
                        match |= true;
                    }
                }
                return match;
            }
        });
    }

    @Test
    public void getAuthenticationAccessLevelsTest() {
        NoOrPasswordSecuritySupport noOrPasswordSecuritySupport = new NoOrPasswordSecuritySupport();

        // assert that you only have one authentication level
        assertThat(noOrPasswordSecuritySupport.getAuthenticationAccessLevels()).hasSize(2);

        // check for the no authentication level
        assertThat(noOrPasswordSecuritySupport.getAuthenticationAccessLevels()).has(new Condition<List<AuthenticationDeviceAccessLevel>>() {
            @Override
            public boolean matches(List<AuthenticationDeviceAccessLevel> authenticationDeviceAccessLevels) {
                boolean match = false;
                for (AuthenticationDeviceAccessLevel authenticationDeviceAccessLevel : authenticationDeviceAccessLevels) {
                    if (authenticationDeviceAccessLevel.getClass().isAssignableFrom(NoOrPasswordSecuritySupport.NoAuthenticationAccessLevel.class)) {
                        match |= true;
                    }
                }
                return match;
            }
        });

        // check for the standard authentication level
        assertThat(noOrPasswordSecuritySupport.getAuthenticationAccessLevels()).has(new Condition<List<AuthenticationDeviceAccessLevel>>() {
            @Override
            public boolean matches(List<AuthenticationDeviceAccessLevel> authenticationDeviceAccessLevels) {
                boolean match = false;
                for (AuthenticationDeviceAccessLevel authenticationDeviceAccessLevel : authenticationDeviceAccessLevels) {
                    if (authenticationDeviceAccessLevel.getClass().isAssignableFrom(NoOrPasswordSecuritySupport.StandardAuthenticationAccessLevel.class)) {
                        match |= true;
                    }
                }
                return match;
            }
        });
    }

    @Test
    public void getEncryptionAccessLevelsTest(){
        NoOrPasswordSecuritySupport passwordWithLevelSecuritySupport = new NoOrPasswordSecuritySupport();

        // assert that you only have one encryption level
        assertThat(passwordWithLevelSecuritySupport.getEncryptionAccessLevels()).hasSize(0);
    }

}

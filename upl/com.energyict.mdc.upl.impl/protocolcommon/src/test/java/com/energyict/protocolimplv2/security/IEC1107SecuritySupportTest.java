package com.energyict.protocolimplv2.security;

import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.upl.security.DeviceAccessLevel;
import com.energyict.mdc.upl.security.DeviceProtocolSecurityPropertySet;

import com.energyict.mdc.upl.TypedProperties;
import org.fest.assertions.core.Condition;

import java.util.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;

/**
 * Tests the {@link IEC1107SecuritySupport} component
 * <p>
 * Copyrights EnergyICT
 * Date: 21/01/13
 * Time: 11:29
 */
@RunWith(MockitoJUnitRunner.class)
public class IEC1107SecuritySupportTest extends AbstractSecuritySupportTest {

    @Test
    public void getSecurityPropertiesTest() {
        IEC1107SecuritySupport iec1107SecuritySupport = new IEC1107SecuritySupport(propertySpecService);

        // currently only 1 property is necessary
        assertThat(iec1107SecuritySupport.getSecurityProperties()).hasSize(1);

        // check for the password propertySpec
        Optional<PropertySpec> passwordPropertySpec = iec1107SecuritySupport.getSecurityPropertySpec(SecurityPropertySpecTranslationKeys.PASSWORD.getKey());
        assertPropertySpecsEqual(DeviceSecurityProperty.PASSWORD.getPropertySpec(propertySpecService), passwordPropertySpec);
    }

    @Test
    public void getAuthenticationAccessLevelsTest() {
        IEC1107SecuritySupport iec1107SecuritySupport = new IEC1107SecuritySupport(propertySpecService);

        // currently only 4 levels are supported
        assertThat(iec1107SecuritySupport.getAuthenticationAccessLevels()).hasSize(4);

        // check for no authentication level
        assertThat(iec1107SecuritySupport.getAuthenticationAccessLevels()).areExactly(1, new Condition<AuthenticationDeviceAccessLevel>() {
            @Override
            public boolean matches(AuthenticationDeviceAccessLevel authenticationDeviceAccessLevel) {
                return authenticationDeviceAccessLevel.getClass().isAssignableFrom(IEC1107SecuritySupport.NoAuthentication.class);
            }
        });

        // check for level one authentication
        assertThat(iec1107SecuritySupport.getAuthenticationAccessLevels()).areExactly(1, new Condition<AuthenticationDeviceAccessLevel>() {
            @Override
            public boolean matches(AuthenticationDeviceAccessLevel authenticationDeviceAccessLevel) {
                return authenticationDeviceAccessLevel.getClass().isAssignableFrom(IEC1107SecuritySupport.LevelOneAuthentication.class);
            }
        });
        // check for level two authentication
        assertThat(iec1107SecuritySupport.getAuthenticationAccessLevels()).areExactly(1, new Condition<AuthenticationDeviceAccessLevel>() {
            @Override
            public boolean matches(AuthenticationDeviceAccessLevel authenticationDeviceAccessLevel) {
                return authenticationDeviceAccessLevel.getClass().isAssignableFrom(IEC1107SecuritySupport.LevelTwoAuthentication.class);
            }
        });
        // check for level three authentication
        assertThat(iec1107SecuritySupport.getAuthenticationAccessLevels()).areExactly(1, new Condition<AuthenticationDeviceAccessLevel>() {
            @Override
            public boolean matches(AuthenticationDeviceAccessLevel authenticationDeviceAccessLevel) {
                return authenticationDeviceAccessLevel.getClass().isAssignableFrom(IEC1107SecuritySupport.LevelThreeAuthentication.class);
            }
        });

    }

    @Test
    public void getEncryptionAccessLevelsTest() {
        IEC1107SecuritySupport iec1107SecuritySupport = new IEC1107SecuritySupport(propertySpecService);

        // currently no encryption levels are supported
        assertThat(iec1107SecuritySupport.getEncryptionAccessLevels()).isEmpty();
    }

    @Test
    public void convertToTypedPropertiesTest() {
        IEC1107SecuritySupport iec1107SecuritySupport = new IEC1107SecuritySupport(propertySpecService);
        final TypedProperties securityProperties = TypedProperties.empty();
        String passwordValue = "MyPassword";
        securityProperties.setProperty(SecurityPropertySpecTranslationKeys.PASSWORD.toString(), passwordValue);
        DeviceProtocolSecurityPropertySet deviceProtocolSecurityPropertySet =
                new DeviceProtocolSecurityPropertySet() {
                    @Override
                    public String getName() {
                        return "security";
                    }

                    @Override
                    public String getClient() {
                        return null;
                    }

                    @Override
                    public int getAuthenticationDeviceAccessLevel() {
                        return IEC1107SecuritySupport.AccessLevelIds.LEVEL_TWO.getAccessLevel();
                    }

                    @Override
                    public int getEncryptionDeviceAccessLevel() {
                        return DeviceAccessLevel.NOT_USED_DEVICE_ACCESS_LEVEL_ID;
                    }

                    @Override
                    public TypedProperties getSecurityProperties() {
                        return securityProperties;
                    }
                };
        // business method
        TypedProperties legacyProperties = TypedProperties.copyOf(iec1107SecuritySupport.convertToTypedProperties(deviceProtocolSecurityPropertySet));

        // asserts
        assertNotNull(legacyProperties);
        assertThat(legacyProperties.getProperty("SecurityLevel")).isEqualTo("2");
        assertThat(legacyProperties.getProperty("Password")).isEqualTo(passwordValue);
    }

    @Test
    public void testConvertFromTypedProperties() throws Exception {
        IEC1107SecuritySupport iec1107SecuritySupport = new IEC1107SecuritySupport(propertySpecService);
        TypedProperties securityProperties = TypedProperties.empty();
        securityProperties.setProperty(DeviceSecurityProperty.PASSWORD.getPropertySpec(propertySpecService).getName(), "MyPassword");
        securityProperties.setProperty("SecurityLevel", "" + IEC1107SecuritySupport.AccessLevelIds.LEVEL_ONE.getAccessLevel());

        DeviceProtocolSecurityPropertySet securityPropertySet = iec1107SecuritySupport.convertFromTypedProperties(securityProperties);

        assertThat(securityPropertySet).isNotNull();
        assertThat(securityPropertySet.getAuthenticationDeviceAccessLevel()).isEqualTo(1);
        assertThat(securityPropertySet.getEncryptionDeviceAccessLevel()).isEqualTo(-1);
        assertThat(securityPropertySet.getSecurityProperties()).isNotNull();
        assertThat(securityPropertySet.getSecurityProperties().size()).isEqualTo(1);
        assertThat(securityPropertySet.getSecurityProperties().getProperty(DeviceSecurityProperty.PASSWORD.getPropertySpec(propertySpecService).getName())).isEqualTo("MyPassword");


    }

    @Test
    public void testConvertTypedPropertiesWithMissingSecurityLevel() throws Exception {
        IEC1107SecuritySupport iec1107SecuritySupport = new IEC1107SecuritySupport(propertySpecService);
        TypedProperties securityProperties = TypedProperties.empty();
        securityProperties.setProperty(SecurityPropertySpecTranslationKeys.PASSWORD.toString(), "MyPassword");

        final DeviceProtocolSecurityPropertySet securityPropertySet = iec1107SecuritySupport.convertFromTypedProperties(securityProperties);

        // asserts
        assertThat(securityPropertySet).isNotNull();
        assertThat(securityPropertySet.getAuthenticationDeviceAccessLevel()).isEqualTo(1);
        assertThat(securityPropertySet.getEncryptionDeviceAccessLevel()).isEqualTo(-1);
        assertThat(securityPropertySet.getSecurityProperties()).isNotNull();
        assertThat(securityPropertySet.getSecurityProperties().size()).isEqualTo(1);
        assertThat(securityPropertySet.getSecurityProperties().getProperty(DeviceSecurityProperty.PASSWORD.getPropertySpec(propertySpecService).getName())).isEqualTo("MyPassword");
    }
}

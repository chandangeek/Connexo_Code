package com.energyict.protocolimplv2.security;

import com.energyict.cpo.PropertySpec;
import com.energyict.cpo.TypedProperties;
import com.energyict.mdc.protocol.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.protocol.security.DeviceAccessLevel;
import com.energyict.mdc.protocol.security.DeviceProtocolSecurityPropertySet;
import com.energyict.mdc.protocol.security.DeviceProtocolSecurityPropertySetImpl;
import java.util.List;
import org.fest.assertions.core.Condition;
import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;

/**
 * Tests the {@link IEC1107SecuritySupport} component
 * <p/>
 * Copyrights EnergyICT
 * Date: 21/01/13
 * Time: 11:29
 */
public class IEC1107SecuritySupportTest {

    @Test
    public void getSecurityPropertiesTest() {
        IEC1107SecuritySupport iec1107SecuritySupport = new IEC1107SecuritySupport();

        // currently only 4 properties are necessary
        assertThat(iec1107SecuritySupport.getSecurityProperties()).hasSize(1);

        // check for the password propertySpec
        assertThat(iec1107SecuritySupport.getSecurityProperties()).has(new Condition<List<PropertySpec>>() {
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
        IEC1107SecuritySupport iec1107SecuritySupport = new IEC1107SecuritySupport();

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
        IEC1107SecuritySupport iec1107SecuritySupport = new IEC1107SecuritySupport();

        // currently no encryption levels are supported
        assertThat(iec1107SecuritySupport.getEncryptionAccessLevels()).isEmpty();
    }

    @Test
    public void convertToTypedPropertiesTest() {
        IEC1107SecuritySupport iec1107SecuritySupport = new IEC1107SecuritySupport();
        TypedProperties securityProperties = new TypedProperties();
        String passwordValue = "MyPassword";
        securityProperties.setProperty(SecurityPropertySpecName.PASSWORD.toString(), passwordValue);
        DeviceProtocolSecurityPropertySetImpl deviceProtocolSecurityPropertySet =
                new DeviceProtocolSecurityPropertySetImpl(IEC1107SecuritySupport.AccessLevelIds.LEVEL_TWO.getAccessLevel(),
                        DeviceAccessLevel.NOT_USED_DEVICE_ACCESS_LEVEL_ID, securityProperties);

        // business method
        TypedProperties legacyProperties = iec1107SecuritySupport.convertToTypedProperties(deviceProtocolSecurityPropertySet);

        // asserts
        assertNotNull(legacyProperties);
        assertThat(legacyProperties.getProperty("SecurityLevel")).isEqualTo("2");
        assertThat(legacyProperties.getProperty("Password")).isEqualTo(passwordValue);
    }

    @Test
    public void testConvertFromTypedProperties() throws Exception {
        IEC1107SecuritySupport iec1107SecuritySupport = new IEC1107SecuritySupport();
        TypedProperties securityProperties = new TypedProperties();
        securityProperties.setProperty(DeviceSecurityProperty.PASSWORD.getPropertySpec().getName(), "MyPassword");
        securityProperties.setProperty("SecurityLevel", ""+IEC1107SecuritySupport.AccessLevelIds.LEVEL_ONE.getAccessLevel());

        DeviceProtocolSecurityPropertySet securityPropertySet = iec1107SecuritySupport.convertFromTypedProperties(securityProperties);

        assertThat(securityPropertySet).isNotNull();
        assertThat(securityPropertySet.getAuthenticationDeviceAccessLevel()).isEqualTo(1);
        assertThat(securityPropertySet.getEncryptionDeviceAccessLevel()).isEqualTo(-1);
        assertThat(securityPropertySet.getSecurityProperties()).isNotNull();
        assertThat(securityPropertySet.getSecurityProperties().size()).isEqualTo(1);
        assertThat(securityPropertySet.getSecurityProperties().getProperty(DeviceSecurityProperty.PASSWORD.getPropertySpec().getName())).isEqualTo("MyPassword");


    }

    @Test(expected = IllegalStateException.class)
    public void testCanNotConvertTypedPropertiesWithMissingSecurityLevel() throws Exception {
        IEC1107SecuritySupport iec1107SecuritySupport = new IEC1107SecuritySupport();
        TypedProperties securityProperties = new TypedProperties();
        securityProperties.setProperty(SecurityPropertySpecName.PASSWORD.toString(), "MyPassword");

        iec1107SecuritySupport.convertFromTypedProperties(securityProperties);
    }
}

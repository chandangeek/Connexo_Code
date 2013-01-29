package com.energyict.protocolimplv2.security;

import com.energyict.cpo.PropertySpec;
import com.energyict.cpo.TypedProperties;
import com.energyict.mdc.protocol.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.protocol.security.DeviceAccessLevel;
import com.energyict.mdc.protocol.security.DeviceProtocolSecurityPropertySetImpl;
import org.fest.assertions.core.Condition;
import org.junit.Test;

import java.util.List;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;

/**
 * Tests the {@link SimplePasswordSecuritySupport} component
 * <p/>
 * Copyrights EnergyICT
 * Date: 11/01/13
 * Time: 14:56
 */
public class SimplePasswordSecuritySupportTest {

    @Test
    public void getSecurityPropertiesTest() {
        SimplePasswordSecuritySupport simplePasswordSecuritySupport = new SimplePasswordSecuritySupport();

        // assert that you only have one property to set
        assertThat(simplePasswordSecuritySupport.getSecurityProperties()).hasSize(1);


        // check for the password propertySpec
        assertThat(simplePasswordSecuritySupport.getSecurityProperties()).has(new Condition<List<PropertySpec>>() {
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
        SimplePasswordSecuritySupport simplePasswordSecuritySupport = new SimplePasswordSecuritySupport();

        // assert that you only have one authentication level
        assertThat(simplePasswordSecuritySupport.getAuthenticationAccessLevels()).hasSize(1);

        // check for the simple authentication level
        assertThat(simplePasswordSecuritySupport.getAuthenticationAccessLevels()).has(new Condition<List<AuthenticationDeviceAccessLevel>>() {
            @Override
            public boolean matches(List<AuthenticationDeviceAccessLevel> authenticationDeviceAccessLevels) {
                boolean match = false;
                for (AuthenticationDeviceAccessLevel authenticationDeviceAccessLevel : authenticationDeviceAccessLevels) {
                    if (authenticationDeviceAccessLevel.getClass().isAssignableFrom(SimplePasswordSecuritySupport.SimpleAuthentication.class)) {
                        match |= true;
                    }
                }
                return match;
            }
        });
    }

    @Test
    public void getEncryptionAccessLevelsTest(){
        SimplePasswordSecuritySupport simplePasswordSecuritySupport = new SimplePasswordSecuritySupport();

        // assert that we don't have any encryption level
        assertThat(simplePasswordSecuritySupport.getEncryptionAccessLevels()).isEmpty();
    }

    @Test
    public void convertToTypedPropertiesTest(){
        SimplePasswordSecuritySupport simplePasswordSecuritySupport = new SimplePasswordSecuritySupport();
        TypedProperties securityProperties = new TypedProperties();
        String passwordValue = "MyPassword";
        securityProperties.setProperty(SecurityPropertySpecName.PASSWORD.toString(), passwordValue);

        DeviceProtocolSecurityPropertySetImpl deviceProtocolSecurityPropertySet =
                new DeviceProtocolSecurityPropertySetImpl(0, DeviceAccessLevel.NOT_USED_DEVICE_ACCESS_LEVEL_ID, securityProperties);

        // business method
        TypedProperties legacyProperties = simplePasswordSecuritySupport.convertToTypedProperties(deviceProtocolSecurityPropertySet);

        // asserts
        assertNotNull(legacyProperties);
        assertThat(legacyProperties.getProperty("Password")).isEqualTo(passwordValue);
    }
}

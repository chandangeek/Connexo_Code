package com.energyict.protocolimplv2.security;

import com.energyict.cpo.PropertySpec;
import com.energyict.mdc.protocol.security.AuthenticationDeviceAccessLevel;
import org.fest.assertions.core.Condition;
import org.junit.Test;

import java.util.List;

import static org.fest.assertions.api.Assertions.assertThat;

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
}

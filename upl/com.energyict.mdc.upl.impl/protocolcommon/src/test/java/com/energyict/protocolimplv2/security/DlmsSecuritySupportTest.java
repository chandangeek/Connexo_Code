package com.energyict.protocolimplv2.security;

import com.energyict.cpo.PropertySpec;
import com.energyict.mdc.protocol.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.protocol.security.EncryptionDeviceAccessLevel;
import org.fest.assertions.core.Condition;
import org.junit.Test;

import java.util.List;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * Tests the {@link DlmsSecuritySupport} component
 * <p/>
 * Copyrights EnergyICT
 * Date: 11/01/13
 * Time: 10:40
 */
public class DlmsSecuritySupportTest {

    @Test
    public void getSecurityPropertiesTest() {
        DlmsSecuritySupport dlmsSecuritySupport = new DlmsSecuritySupport();

        // currently only 4 properties are necessary
        assertThat(dlmsSecuritySupport.getSecurityProperties()).hasSize(4);

        // check for the password propertySpec
        assertThat(dlmsSecuritySupport.getSecurityProperties()).has(new Condition<List<PropertySpec>>() {
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

        // check for the encryptionKey propertySpec
        assertThat(dlmsSecuritySupport.getSecurityProperties()).has(new Condition<List<PropertySpec>>() {
            @Override
            public boolean matches(List<PropertySpec> propertySpecs) {
                boolean match = false;
                for (PropertySpec propertySpec : propertySpecs) {
                    if (propertySpec.equals(DeviceSecurityProperty.ENCRYPTION_KEY.getPropertySpec())) {
                        match |= true;
                    }
                }
                return match;
            }
        });

        // check for the authenticationKey propertySpec
        assertThat(dlmsSecuritySupport.getSecurityProperties()).has(new Condition<List<PropertySpec>>() {
            @Override
            public boolean matches(List<PropertySpec> propertySpecs) {
                boolean match = false;
                for (PropertySpec propertySpec : propertySpecs) {
                    if (propertySpec.equals(DeviceSecurityProperty.AUTHENTICATION_KEY.getPropertySpec())) {
                        match |= true;
                    }
                }
                return match;
            }
        });

        // check for the client identification propertySpec
        assertThat(dlmsSecuritySupport.getSecurityProperties()).has(new Condition<List<PropertySpec>>() {
            @Override
            public boolean matches(List<PropertySpec> propertySpecs) {
                boolean match = false;
                for (PropertySpec propertySpec : propertySpecs) {
                    if (propertySpec.equals(DeviceSecurityProperty.CLIENT_MAC_ADDRESS.getPropertySpec())) {
                        match |= true;
                    }
                }
                return match;
            }
        });
    }

    @Test
    public void getAuthenticationAccessLevelsTest() {
        DlmsSecuritySupport dlmsSecuritySupport = new DlmsSecuritySupport();

        // currently only 6 levels are supported
        assertThat(dlmsSecuritySupport.getAuthenticationAccessLevels()).hasSize(6);

        // check for no authentication level
        assertThat(dlmsSecuritySupport.getAuthenticationAccessLevels()).has(new Condition<List<AuthenticationDeviceAccessLevel>>() {
            @Override
            public boolean matches(List<AuthenticationDeviceAccessLevel> authenticationDeviceAccessLevels) {
                boolean match = false;
                for (AuthenticationDeviceAccessLevel authenticationDeviceAccessLevel : authenticationDeviceAccessLevels) {
                    if (authenticationDeviceAccessLevel.getClass().isAssignableFrom(DlmsSecuritySupport.NoAuthentication.class)) {
                        match |= true;
                    }
                }
                return match;
            }
        });

        // check for the low level security
        assertThat(dlmsSecuritySupport.getAuthenticationAccessLevels()).has(new Condition<List<AuthenticationDeviceAccessLevel>>() {
            @Override
            public boolean matches(List<AuthenticationDeviceAccessLevel> authenticationDeviceAccessLevels) {
                boolean match = false;
                for (AuthenticationDeviceAccessLevel authenticationDeviceAccessLevel : authenticationDeviceAccessLevels) {
                    if (authenticationDeviceAccessLevel.getClass().isAssignableFrom(DlmsSecuritySupport.LowLevelAuthentication.class)) {
                        match |= true;
                    }
                }
                return match;
            }
        });

        // check for the manufacturer level security
        assertThat(dlmsSecuritySupport.getAuthenticationAccessLevels()).has(new Condition<List<AuthenticationDeviceAccessLevel>>() {
            @Override
            public boolean matches(List<AuthenticationDeviceAccessLevel> authenticationDeviceAccessLevels) {
                boolean match = false;
                for (AuthenticationDeviceAccessLevel authenticationDeviceAccessLevel : authenticationDeviceAccessLevels) {
                    if (authenticationDeviceAccessLevel.getClass().isAssignableFrom(DlmsSecuritySupport.ManufactureAuthentication.class)) {
                        match |= true;
                    }
                }
                return match;
            }
        });

        // check for the MD5 level security
        assertThat(dlmsSecuritySupport.getAuthenticationAccessLevels()).has(new Condition<List<AuthenticationDeviceAccessLevel>>() {
            @Override
            public boolean matches(List<AuthenticationDeviceAccessLevel> authenticationDeviceAccessLevels) {
                boolean match = false;
                for (AuthenticationDeviceAccessLevel authenticationDeviceAccessLevel : authenticationDeviceAccessLevels) {
                    if (authenticationDeviceAccessLevel.getClass().isAssignableFrom(DlmsSecuritySupport.Md5Authentication.class)) {
                        match |= true;
                    }
                }
                return match;
            }
        });

        // check for the SHA-1 level security
        assertThat(dlmsSecuritySupport.getAuthenticationAccessLevels()).has(new Condition<List<AuthenticationDeviceAccessLevel>>() {
            @Override
            public boolean matches(List<AuthenticationDeviceAccessLevel> authenticationDeviceAccessLevels) {
                boolean match = false;
                for (AuthenticationDeviceAccessLevel authenticationDeviceAccessLevel : authenticationDeviceAccessLevels) {
                    if (authenticationDeviceAccessLevel.getClass().isAssignableFrom(DlmsSecuritySupport.Sha1Authentication.class)) {
                        match |= true;
                    }
                }
                return match;
            }
        });

        // check for the GMAC level security
        assertThat(dlmsSecuritySupport.getAuthenticationAccessLevels()).has(new Condition<List<AuthenticationDeviceAccessLevel>>() {
            @Override
            public boolean matches(List<AuthenticationDeviceAccessLevel> authenticationDeviceAccessLevels) {
                boolean match = false;
                for (AuthenticationDeviceAccessLevel authenticationDeviceAccessLevel : authenticationDeviceAccessLevels) {
                    if (authenticationDeviceAccessLevel.getClass().isAssignableFrom(DlmsSecuritySupport.GmacAuthentication.class)) {
                        match |= true;
                    }
                }
                return match;
            }
        });
    }

    @Test
    public void getEncryptionAccessLevelsTest() {
        DlmsSecuritySupport dlmsSecuritySupport = new DlmsSecuritySupport();

        // currently only 6 levels are supported
        assertThat(dlmsSecuritySupport.getEncryptionAccessLevels()).hasSize(4);

        // check for the no encryption security
        assertThat(dlmsSecuritySupport.getEncryptionAccessLevels()).has(new Condition<List<EncryptionDeviceAccessLevel>>() {
            @Override
            public boolean matches(List<EncryptionDeviceAccessLevel> encryptionDeviceAccessLevels) {
                boolean match = false;
                for (EncryptionDeviceAccessLevel encryptionDeviceAccessLevel : encryptionDeviceAccessLevels) {
                    if (encryptionDeviceAccessLevel.getClass().isAssignableFrom(DlmsSecuritySupport.NoMessageEncryption.class)) {
                        match |= true;
                    }
                }
                return match;
            }
        });

        // check for the message encryption security
        assertThat(dlmsSecuritySupport.getEncryptionAccessLevels()).has(new Condition<List<EncryptionDeviceAccessLevel>>() {
            @Override
            public boolean matches(List<EncryptionDeviceAccessLevel> encryptionDeviceAccessLevels) {
                boolean match = false;
                for (EncryptionDeviceAccessLevel encryptionDeviceAccessLevel : encryptionDeviceAccessLevels) {
                    if (encryptionDeviceAccessLevel.getClass().isAssignableFrom(DlmsSecuritySupport.MessageEncryption.class)) {
                        match |= true;
                    }
                }
                return match;
            }
        });

        // check for the message authentication security
        assertThat(dlmsSecuritySupport.getEncryptionAccessLevels()).has(new Condition<List<EncryptionDeviceAccessLevel>>() {
            @Override
            public boolean matches(List<EncryptionDeviceAccessLevel> encryptionDeviceAccessLevels) {
                boolean match = false;
                for (EncryptionDeviceAccessLevel encryptionDeviceAccessLevel : encryptionDeviceAccessLevels) {
                    if (encryptionDeviceAccessLevel.getClass().isAssignableFrom(DlmsSecuritySupport.MessageAuthentication.class)) {
                        match |= true;
                    }
                }
                return match;
            }
        });

        // check for the message encryption and authentication security
        assertThat(dlmsSecuritySupport.getEncryptionAccessLevels()).has(new Condition<List<EncryptionDeviceAccessLevel>>() {
            @Override
            public boolean matches(List<EncryptionDeviceAccessLevel> encryptionDeviceAccessLevels) {
                boolean match = false;
                for (EncryptionDeviceAccessLevel encryptionDeviceAccessLevel : encryptionDeviceAccessLevels) {
                    if (encryptionDeviceAccessLevel.getClass().isAssignableFrom(DlmsSecuritySupport.MessageEncryptionAndAuthentication.class)) {
                        match |= true;
                    }
                }
                return match;
            }
        });
    }
}

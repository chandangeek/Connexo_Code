package com.energyict.protocolimplv2.security;

import com.energyict.cbo.Password;
import com.energyict.cpo.PropertySpec;
import com.energyict.cpo.TypedProperties;
import com.energyict.mdc.protocol.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.protocol.security.DeviceProtocolSecurityPropertySet;
import com.energyict.mdc.protocol.security.DeviceProtocolSecurityPropertySetImpl;
import com.energyict.mdc.protocol.security.EncryptionDeviceAccessLevel;
import org.fest.assertions.core.Condition;
import org.junit.Test;

import java.util.List;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;

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

    @Test
    public void convertToTypedPropertiesTest() {
        DlmsSecuritySupport dlmsSecuritySupport = new DlmsSecuritySupport();
        TypedProperties securityProperties = new TypedProperties();
        String encryptionKeyValue = "MyEncryptionKey";
        securityProperties.setProperty(SecurityPropertySpecName.ENCRYPTION_KEY.toString(), encryptionKeyValue);
        String authenticationKeyValue = "MyAuthenticationKey";
        securityProperties.setProperty(SecurityPropertySpecName.AUTHENTICATION_KEY.toString(), authenticationKeyValue);
        String clientMacAddressValue = "1";
        securityProperties.setProperty(SecurityPropertySpecName.CLIENT_MAC_ADDRESS.toString(), clientMacAddressValue);
        String passwordValue = "MyPassword";
        securityProperties.setProperty(SecurityPropertySpecName.PASSWORD.toString(), passwordValue);
        DeviceProtocolSecurityPropertySetImpl deviceProtocolSecurityPropertySet =
                new DeviceProtocolSecurityPropertySetImpl(DlmsSecuritySupport.AccessLevelIds.GMAC_AUTHENTICATION.getAccessLevel(),
                        DlmsSecuritySupport.AccessLevelIds.NO_MESSAGE_ENCRYPTION.getAccessLevel(), securityProperties);

        // business method
        TypedProperties legacyProperties = dlmsSecuritySupport.convertToTypedProperties(deviceProtocolSecurityPropertySet);

        // asserts
        assertNotNull(legacyProperties);
        assertThat(legacyProperties.getProperty("SecurityLevel")).isEqualTo("5:0");
        assertThat(legacyProperties.getProperty("DataTransportEncryptionKey")).isEqualTo(encryptionKeyValue);
        assertThat(legacyProperties.getProperty("DataTransportAuthenticationKey")).isEqualTo(authenticationKeyValue);
        assertThat(legacyProperties.getProperty("ClientMacAddress")).isEqualTo(clientMacAddressValue);
        assertThat(legacyProperties.getProperty("Password")).isEqualTo(passwordValue);
    }

    @Test
    public void testConvertTypedPropertiesToSecuritySet() throws Exception {
        DlmsSecuritySupport dlmsSecuritySupport = new DlmsSecuritySupport();
        TypedProperties securityProperties = new TypedProperties();
        securityProperties.setProperty("SecurityLevel", "12:21");

        DeviceProtocolSecurityPropertySet securityPropertySet = dlmsSecuritySupport.convertFromTypedProperties(securityProperties);

        assertThat(securityPropertySet).isNotNull();
        assertThat(securityPropertySet.getEncryptionDeviceAccessLevel()).isEqualTo(21);
        assertThat(securityPropertySet.getAuthenticationDeviceAccessLevel()).isEqualTo(12);
    }

    @Test (expected = IllegalStateException.class)
    public void testConvertTypedPropertiesToSecuritySetWithSecurityLevelIllegalFormat() throws Exception {
        DlmsSecuritySupport dlmsSecuritySupport = new DlmsSecuritySupport();
        TypedProperties securityProperties = new TypedProperties();
        securityProperties.setProperty("SecurityLevel", "12;21"); // illegal format

        dlmsSecuritySupport.convertFromTypedProperties(securityProperties);
    }

    @Test (expected = IllegalArgumentException.class)
    public void testConvertTypedPropertiesToSecuritySetWithSecurityLevelIllegalAuthenticationLevel() throws Exception {
        DlmsSecuritySupport dlmsSecuritySupport = new DlmsSecuritySupport();
        TypedProperties securityProperties = new TypedProperties();
        securityProperties.setProperty("SecurityLevel", "1A2:21"); // illegal int value

        DeviceProtocolSecurityPropertySet securityPropertySet = dlmsSecuritySupport.convertFromTypedProperties(securityProperties);
        securityPropertySet.getAuthenticationDeviceAccessLevel();

    }

    @Test (expected = IllegalArgumentException.class)
    public void testConvertTypedPropertiesToSecuritySetWithSecurityLevelIllegalEncryptionLevel() throws Exception {
        DlmsSecuritySupport dlmsSecuritySupport = new DlmsSecuritySupport();
        TypedProperties securityProperties = new TypedProperties();
        securityProperties.setProperty("SecurityLevel", "12:2A1"); // illegal int value

        DeviceProtocolSecurityPropertySet securityPropertySet = dlmsSecuritySupport.convertFromTypedProperties(securityProperties);
        securityPropertySet.getEncryptionDeviceAccessLevel();
    }

    @Test(expected = IllegalStateException.class)
    public void testConvertTypedPropertiesToSecuritySetMissingSecurityProperty() throws Exception {
        DlmsSecuritySupport dlmsSecuritySupport = new DlmsSecuritySupport();
        TypedProperties securityProperties = new TypedProperties();

        dlmsSecuritySupport.convertFromTypedProperties(securityProperties);
    }
    
    @Test
    public void testPasswordConversion(){
        DlmsSecuritySupport dlmsSecuritySupport = new DlmsSecuritySupport();
        TypedProperties securityProperties = TypedProperties.empty();
        String passwordValue = "MyPassword";
        Password password = new Password(passwordValue);
        securityProperties.setProperty(SecurityPropertySpecName.PASSWORD.toString(), password);
        DeviceProtocolSecurityPropertySetImpl deviceProtocolSecurityPropertySet =
                new DeviceProtocolSecurityPropertySetImpl(DlmsSecuritySupport.AccessLevelIds.GMAC_AUTHENTICATION.getAccessLevel(),
                        DlmsSecuritySupport.AccessLevelIds.NO_MESSAGE_ENCRYPTION.getAccessLevel(), securityProperties);

        // business method
        TypedProperties legacyProperties = dlmsSecuritySupport.convertToTypedProperties(deviceProtocolSecurityPropertySet);

        assertThat(legacyProperties.getProperty(SecurityPropertySpecName.PASSWORD.toString())).isEqualTo(passwordValue);

    }
    
}

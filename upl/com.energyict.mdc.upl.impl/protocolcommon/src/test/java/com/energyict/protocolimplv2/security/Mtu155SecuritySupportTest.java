package com.energyict.protocolimplv2.security;

import com.energyict.mdc.upl.TypedProperties;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.upl.security.DeviceProtocolSecurityPropertySet;
import com.energyict.mdc.upl.security.EncryptionDeviceAccessLevel;
import com.energyict.mdc.upl.security.SecurityPropertySpecTranslationKeys;

import org.fest.assertions.core.Condition;

import java.util.List;
import java.util.Optional;

import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Copyrights EnergyICT
 * Date: 28/05/13
 * Time: 11:22
 */
public class Mtu155SecuritySupportTest extends AbstractSecuritySupportTest {

    @Test
    public void getSecurityPropertiesTest() {
        Mtu155SecuritySupport mtuSecuritySupport = new Mtu155SecuritySupport(propertySpecService);

        // currently only 4 properties are necessary
        assertThat(mtuSecuritySupport.getSecurityProperties()).hasSize(4);


        // check for the password propertySpec
        Optional<PropertySpec> passwordPropertySpec = mtuSecuritySupport.getSecurityPropertySpec(SecurityPropertySpecTranslationKeys.PASSWORD.getKey());
        assertPropertySpecsEqual(DeviceSecurityProperty.PASSWORD.getPropertySpec(propertySpecService), passwordPropertySpec);

        // check for the encryptionKey1 propertySpec
        Optional<PropertySpec> encryptionKey1PropertySpec = mtuSecuritySupport.getSecurityPropertySpec(SecurityPropertySpecTranslationKeys.ENCRYPTION_KEY_1.getKey());
        assertTrue(encryptionKey1PropertySpec.isPresent());
        assertEquals(encryptionKey1PropertySpec.get().getName(), SecurityPropertySpecTranslationKeys.ENCRYPTION_KEY_1.getKey());

        // check for the encryptionKey2 propertySpec
        Optional<PropertySpec> encryptionKey2PropertySpec = mtuSecuritySupport.getSecurityPropertySpec(SecurityPropertySpecTranslationKeys.ENCRYPTION_KEY_2.getKey());
        assertTrue(encryptionKey2PropertySpec.isPresent());
        assertEquals(encryptionKey2PropertySpec.get().getName(), SecurityPropertySpecTranslationKeys.ENCRYPTION_KEY_2.getKey());

        // check for the encryptionKey3 propertySpec

        Optional<PropertySpec> encryptionKey3PropertySpec = mtuSecuritySupport.getSecurityPropertySpec(SecurityPropertySpecTranslationKeys.ENCRYPTION_KEY_3.getKey());
        assertTrue(encryptionKey3PropertySpec.isPresent());
        assertEquals(encryptionKey3PropertySpec.get().getName(), SecurityPropertySpecTranslationKeys.ENCRYPTION_KEY_3.getKey());
    }


    @Test
    public void getAuthenticationAccessLevelsTest() {
        Mtu155SecuritySupport mtu155SecuritySupport = new Mtu155SecuritySupport(propertySpecService);

        // currently only 6 levels are supported
        assertThat(mtu155SecuritySupport.getAuthenticationAccessLevels()).hasSize(1);

        // check for default authentication level
        assertThat(mtu155SecuritySupport.getAuthenticationAccessLevels()).has(new Condition<List<AuthenticationDeviceAccessLevel>>() {
            @Override
            public boolean matches(List<AuthenticationDeviceAccessLevel> authenticationDeviceAccessLevels) {
                boolean match = false;
                for (AuthenticationDeviceAccessLevel authenticationDeviceAccessLevel : authenticationDeviceAccessLevels) {
                    if (authenticationDeviceAccessLevel.getClass().isAssignableFrom(Mtu155SecuritySupport.SimpleAuthentication.class)) {
                        match |= true;
                    }
                }
                return match;
            }
        });
    }

    @Test
    public void getEncryptionAccessLevelsTest() {
        Mtu155SecuritySupport mtu155SecuritySupport = new Mtu155SecuritySupport(propertySpecService);

        // currently only 3 levels are supported
        assertThat(mtu155SecuritySupport.getEncryptionAccessLevels()).hasSize(3);

        // check for the KeyC encryption security
        assertThat(mtu155SecuritySupport.getEncryptionAccessLevels()).has(new Condition<List<EncryptionDeviceAccessLevel>>() {
            @Override
            public boolean matches(List<EncryptionDeviceAccessLevel> encryptionDeviceAccessLevels) {
                boolean match = false;
                for (EncryptionDeviceAccessLevel encryptionDeviceAccessLevel : encryptionDeviceAccessLevels) {
                    if (encryptionDeviceAccessLevel.getClass().isAssignableFrom(Mtu155SecuritySupport.KeyCEncryption.class)) {
                        match |= true;
                    }
                }
                return match;
            }
        });

        // check for the KeyF encryption security
        assertThat(mtu155SecuritySupport.getEncryptionAccessLevels()).has(new Condition<List<EncryptionDeviceAccessLevel>>() {
            @Override
            public boolean matches(List<EncryptionDeviceAccessLevel> encryptionDeviceAccessLevels) {
                boolean match = false;
                for (EncryptionDeviceAccessLevel encryptionDeviceAccessLevel : encryptionDeviceAccessLevels) {
                    if (encryptionDeviceAccessLevel.getClass().isAssignableFrom(Mtu155SecuritySupport.KeyFEncryption.class)) {
                        match |= true;
                    }
                }
                return match;
            }
        });

        // check for the KeyT encryption security
        assertThat(mtu155SecuritySupport.getEncryptionAccessLevels()).has(new Condition<List<EncryptionDeviceAccessLevel>>() {
            @Override
            public boolean matches(List<EncryptionDeviceAccessLevel> encryptionDeviceAccessLevels) {
                boolean match = false;
                for (EncryptionDeviceAccessLevel encryptionDeviceAccessLevel : encryptionDeviceAccessLevels) {
                    if (encryptionDeviceAccessLevel.getClass().isAssignableFrom(Mtu155SecuritySupport.KeyTEncryption.class)) {
                        match |= true;
                    }
                }
                return match;
            }
        });
    }

    @Test
    public void convertToTypedPropertiesTest() {
        Mtu155SecuritySupport mtu155SecuritySupport = new Mtu155SecuritySupport(propertySpecService);
        final TypedProperties securityProperties = TypedProperties.empty();
        String encryptionKey_2_Value = "MyEncryptionKey_2";
        securityProperties.setProperty(SecurityPropertySpecTranslationKeys.ENCRYPTION_KEY_2.toString(), encryptionKey_2_Value);
        String passwordValue = "MyPassword";
        securityProperties.setProperty(SecurityPropertySpecTranslationKeys.PASSWORD.toString(), passwordValue);
        DeviceProtocolSecurityPropertySet deviceProtocolSecurityPropertySet =
                new DeviceProtocolSecurityPropertySet() {
                    @Override
                    public String getName() {
                        return "security";
                    }

                    @Override
                    public Object getClient() {
                        return null;
                    }

                    @Override
                    public int getAuthenticationDeviceAccessLevel() {
                        return 0;
                    }

                    @Override
                    public int getEncryptionDeviceAccessLevel() {
                        return Mtu155SecuritySupport.AccessLevelIds.KEYC.getAccessLevel();
                    }

                    @Override
                    public TypedProperties getSecurityProperties() {
                        return securityProperties;
                    }
                };

        // business method
        TypedProperties legacyProperties = mtu155SecuritySupport.convertToTypedProperties(deviceProtocolSecurityPropertySet);

        // asserts
        assertNotNull(legacyProperties);
        assertThat(legacyProperties.getProperty("SecurityLevel")).isEqualTo("1");
        assertThat(legacyProperties.getProperty("KeyC")).isEqualTo(encryptionKey_2_Value);
        assertThat(legacyProperties.getProperty("Password")).isEqualTo(passwordValue);
    }


    @Test
    public void testConvertTypedPropertiesToSecuritySet() throws Exception {
        Mtu155SecuritySupport mtu155SecuritySupport = new Mtu155SecuritySupport(propertySpecService);
        TypedProperties securityProperties = TypedProperties.empty();
        securityProperties.setProperty("SecurityLevel", "321");

        DeviceProtocolSecurityPropertySet securityPropertySet = mtu155SecuritySupport.convertFromTypedProperties(securityProperties);

        assertThat(securityPropertySet).isNotNull();
        assertThat(securityPropertySet.getEncryptionDeviceAccessLevel()).isEqualTo(321);
        assertThat(securityPropertySet.getAuthenticationDeviceAccessLevel()).isEqualTo(0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConvertTypedPropertiesToSecuritySetWithSecurityLevelIllegalAuthenticationLevel() throws Exception {
        Mtu155SecuritySupport mtu155SecuritySupport = new Mtu155SecuritySupport(propertySpecService);
        TypedProperties securityProperties = TypedProperties.empty();
        securityProperties.setProperty("SecurityLevel", "twee"); // illegal format

        mtu155SecuritySupport.convertFromTypedProperties(securityProperties);
    }

    @Test
    public void testConvertTypedPropertiesToSecuritySetMissingSecurityProperty() throws Exception {
        Mtu155SecuritySupport mtu155SecuritySupport = new Mtu155SecuritySupport(propertySpecService);
        TypedProperties securityProperties = TypedProperties.empty();

        DeviceProtocolSecurityPropertySet securityPropertySet = mtu155SecuritySupport.convertFromTypedProperties(securityProperties);
        assertThat(securityPropertySet.getEncryptionDeviceAccessLevel()).isEqualTo(1);
        assertThat(securityPropertySet.getAuthenticationDeviceAccessLevel()).isEqualTo(0);
    }

    @Test
    public void testPasswordConversion() {
        Mtu155SecuritySupport mtu155SecuritySupport = new Mtu155SecuritySupport(propertySpecService);
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
                    public Object getClient() {
                        return null;
                    }

                    @Override
                    public int getAuthenticationDeviceAccessLevel() {
                        return 1;
                    }

                    @Override
                    public int getEncryptionDeviceAccessLevel() {
                        return 1;
                    }

                    @Override
                    public TypedProperties getSecurityProperties() {
                        return securityProperties;
                    }
                };

        // business method
        TypedProperties legacyProperties = mtu155SecuritySupport.convertToTypedProperties(deviceProtocolSecurityPropertySet);
        assertThat(legacyProperties.getProperty(SecurityPropertySpecTranslationKeys.PASSWORD.toString())).isEqualTo(passwordValue);
    }

}

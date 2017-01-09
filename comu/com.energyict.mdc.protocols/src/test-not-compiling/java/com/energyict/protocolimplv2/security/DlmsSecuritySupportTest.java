package com.energyict.protocolimplv2.security;

import com.elster.jupiter.datavault.DataVault;
import com.elster.jupiter.datavault.LegacyDataVaultProvider;
import com.energyict.mdc.common.Password;
import com.energyict.mdc.common.TypedProperties;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.upl.security.DeviceProtocolSecurityPropertySet;
import com.energyict.mdc.protocol.api.security.EncryptionDeviceAccessLevel;
import org.fest.assertions.core.Condition;
import org.junit.*;
import org.junit.runner.*;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.util.List;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link com.energyict.protocolimplv2.securitysupport.DlmsSecuritySupport} component
 * <p/>
 * Copyrights EnergyICT
 * Date: 11/01/13
 * Time: 10:40
 */
@RunWith(MockitoJUnitRunner.class)
public class DlmsSecuritySupportTest {

    @Mock
    private PropertySpecService propertySpecService;
    @Mock
    private LegacyDataVaultProvider dataVaultProvider;
    @Mock
    private DataVault dataVault;

    @Before
    public void setUp() {
        LegacyDataVaultProvider.instance.set(dataVaultProvider);
        when(dataVaultProvider.getKeyVault()).thenReturn(dataVault);
    }

    @Test
    public void getSecurityPropertiesTest() {
        DlmsSecuritySupport dlmsSecuritySupport = new DlmsSecuritySupport(propertySpecService);

        // currently only 4 properties are necessary
        assertThat(dlmsSecuritySupport.getSecurityProperties()).hasSize(4);

        // check for the password propertySpec
        assertThat(dlmsSecuritySupport.getSecurityProperties()).has(new Condition<List<PropertySpec>>() {
            @Override
            public boolean matches(List<PropertySpec> propertySpecs) {
                boolean match = false;
                for (PropertySpec propertySpec : propertySpecs) {
                    if (propertySpec.equals(DeviceSecurityProperty.PASSWORD.getPropertySpec(propertySpecService))) {
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
                    if (propertySpec.equals(DeviceSecurityProperty.ENCRYPTION_KEY.getPropertySpec(propertySpecService))) {
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
                    if (propertySpec.equals(DeviceSecurityProperty.AUTHENTICATION_KEY.getPropertySpec(propertySpecService))) {
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
                    if (propertySpec.equals(DeviceSecurityProperty.CLIENT_MAC_ADDRESS.getPropertySpec(propertySpecService))) {
                        match |= true;
                    }
                }
                return match;
            }
        });
    }

    @Test
    public void getAuthenticationAccessLevelsTest() {
        DlmsSecuritySupport dlmsSecuritySupport = new DlmsSecuritySupport(propertySpecService);

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
        DlmsSecuritySupport dlmsSecuritySupport = new DlmsSecuritySupport(propertySpecService);

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
        DlmsSecuritySupport dlmsSecuritySupport = new DlmsSecuritySupport(propertySpecService);
        final TypedProperties securityProperties = new TypedProperties();
        String encryptionKeyValue = "MyEncryptionKey";
        securityProperties.setProperty(SecurityPropertySpecName.ENCRYPTION_KEY.toString(), encryptionKeyValue);
        String authenticationKeyValue = "MyAuthenticationKey";
        securityProperties.setProperty(SecurityPropertySpecName.AUTHENTICATION_KEY.toString(), authenticationKeyValue);
        String clientMacAddressValue = "1";
        securityProperties.setProperty(SecurityPropertySpecName.CLIENT_MAC_ADDRESS.toString(), clientMacAddressValue);
        String passwordValue = "MyPassword";
        securityProperties.setProperty(SecurityPropertySpecName.PASSWORD.toString(), passwordValue);
        DeviceProtocolSecurityPropertySet deviceProtocolSecurityPropertySet =
                new DeviceProtocolSecurityPropertySet() {
                    @Override
                    public int getAuthenticationDeviceAccessLevel() {
                        return DlmsSecuritySupport.AuthenticationAccessLevelIds.GMAC_AUTHENTICATION.getAccessLevel();
                    }

                    @Override
                    public int getEncryptionDeviceAccessLevel() {
                        return DlmsSecuritySupport.EncryptionAccessLevelIds.NO_MESSAGE_ENCRYPTION.getAccessLevel();
                    }

                    @Override
                    public TypedProperties getSecurityProperties() {
                        return securityProperties;
                    }
                };

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
        DlmsSecuritySupport dlmsSecuritySupport = new DlmsSecuritySupport(propertySpecService);
        TypedProperties securityProperties = new TypedProperties();
        securityProperties.setProperty("SecurityLevel", "12:21");

        DeviceProtocolSecurityPropertySet securityPropertySet = dlmsSecuritySupport.convertFromTypedProperties(securityProperties);

        assertThat(securityPropertySet).isNotNull();
        assertThat(securityPropertySet.getEncryptionDeviceAccessLevel()).isEqualTo(21);
        assertThat(securityPropertySet.getAuthenticationDeviceAccessLevel()).isEqualTo(12);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConvertTypedPropertiesToSecuritySetWithSecurityLevelIllegalFormat() throws Exception {
        DlmsSecuritySupport dlmsSecuritySupport = new DlmsSecuritySupport(propertySpecService);
        TypedProperties securityProperties = TypedProperties.empty();
        securityProperties.setProperty("SecurityLevel", "12;21"); // illegal format

        dlmsSecuritySupport.convertFromTypedProperties(securityProperties);
    }

    @Test
    public void testConvertTypedPropertiesToSecuritySetWithoutEncryptionLevelDefaultsTo0() throws Exception {
        DlmsSecuritySupport dlmsSecuritySupport = new DlmsSecuritySupport(propertySpecService);
        TypedProperties securityProperties = new TypedProperties();
        securityProperties.setProperty("SecurityLevel", "13");

        DeviceProtocolSecurityPropertySet securityPropertySet = dlmsSecuritySupport.convertFromTypedProperties(securityProperties);

        assertThat(securityPropertySet).isNotNull();
        assertThat(securityPropertySet.getEncryptionDeviceAccessLevel()).isEqualTo(0);
        assertThat(securityPropertySet.getAuthenticationDeviceAccessLevel()).isEqualTo(13);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConvertTypedPropertiesToSecuritySetWithSecurityLevelIllegalAuthenticationLevel() throws Exception {
        DlmsSecuritySupport dlmsSecuritySupport = new DlmsSecuritySupport(propertySpecService);
        TypedProperties securityProperties = TypedProperties.empty();
        securityProperties.setProperty("SecurityLevel", "1A2:21"); // illegal int value

        DeviceProtocolSecurityPropertySet securityPropertySet = dlmsSecuritySupport.convertFromTypedProperties(securityProperties);
        securityPropertySet.getAuthenticationDeviceAccessLevel();

    }

    @Test(expected = IllegalArgumentException.class)
    public void testConvertTypedPropertiesToSecuritySetWithSecurityLevelIllegalEncryptionLevel() throws Exception {
        DlmsSecuritySupport dlmsSecuritySupport = new DlmsSecuritySupport(propertySpecService);
        TypedProperties securityProperties = TypedProperties.empty();
        securityProperties.setProperty("SecurityLevel", "12:2A1"); // illegal int value

        DeviceProtocolSecurityPropertySet securityPropertySet = dlmsSecuritySupport.convertFromTypedProperties(securityProperties);
        securityPropertySet.getEncryptionDeviceAccessLevel();
    }

    @Test
    public void testConvertTypedPropertiesToSecuritySetMissingSecurityProperty() throws Exception {
        DlmsSecuritySupport dlmsSecuritySupport = new DlmsSecuritySupport(propertySpecService);
        TypedProperties securityProperties = TypedProperties.empty();

        DeviceProtocolSecurityPropertySet securityPropertySet = dlmsSecuritySupport.convertFromTypedProperties(securityProperties);
        assertThat(securityPropertySet.getEncryptionDeviceAccessLevel()).isEqualTo(0);
        assertThat(securityPropertySet.getAuthenticationDeviceAccessLevel()).isEqualTo(0);
    }

    @Test
    public void testPasswordConversion() {
        DlmsSecuritySupport dlmsSecuritySupport = new DlmsSecuritySupport(propertySpecService);
        final TypedProperties securityProperties = TypedProperties.empty();
        String passwordValue = "MyPassword";
        Password password = new Password(passwordValue);
        securityProperties.setProperty(SecurityPropertySpecName.PASSWORD.toString(), password);
        DeviceProtocolSecurityPropertySet deviceProtocolSecurityPropertySet =
                new DeviceProtocolSecurityPropertySet() {
                    @Override
                    public int getAuthenticationDeviceAccessLevel() {
                        return DlmsSecuritySupport.AuthenticationAccessLevelIds.GMAC_AUTHENTICATION.getAccessLevel();
                    }

                    @Override
                    public int getEncryptionDeviceAccessLevel() {
                        return DlmsSecuritySupport.EncryptionAccessLevelIds.NO_MESSAGE_ENCRYPTION.getAccessLevel();
                    }

                    @Override
                    public TypedProperties getSecurityProperties() {
                        return securityProperties;
                    }
                };

        // business method
        TypedProperties legacyProperties = dlmsSecuritySupport.convertToTypedProperties(deviceProtocolSecurityPropertySet);

        assertThat(legacyProperties.getProperty(SecurityPropertySpecName.PASSWORD.toString())).isEqualTo(passwordValue);

    }

    @Test
    public void testSecuredPropertySetConversionWithTypedProperties() throws Exception {
        DlmsSecuritySupport dlmsSecuritySupport = new DlmsSecuritySupport(propertySpecService);
        TypedProperties securityProperties = TypedProperties.empty();
        securityProperties.setProperty("SecurityLevel", DlmsSecuritySupport.AuthenticationAccessLevelIds.MD5_AUTHENTICATION.getAccessLevel() + ":" + DlmsSecuritySupport.EncryptionAccessLevelIds.NO_MESSAGE_ENCRYPTION.getAccessLevel());
        securityProperties.setProperty(DeviceSecurityProperty.CLIENT_MAC_ADDRESS.getPropertySpec(propertySpecService).getName(), "some mac address");
        securityProperties.setProperty(DeviceSecurityProperty.PASSWORD.getPropertySpec(propertySpecService).getName(), "some password");
        securityProperties.setProperty(DeviceSecurityProperty.ENCRYPTION_KEY.getPropertySpec(propertySpecService).getName(), "unused");

        DeviceProtocolSecurityPropertySet deviceProtocolSecurityPropertySet = dlmsSecuritySupport.convertFromTypedProperties(securityProperties);
        assertThat(deviceProtocolSecurityPropertySet.getAuthenticationDeviceAccessLevel()).isEqualTo(DlmsSecuritySupport.AuthenticationAccessLevelIds.MD5_AUTHENTICATION.getAccessLevel());
        assertThat(deviceProtocolSecurityPropertySet.getEncryptionDeviceAccessLevel()).isEqualTo(DlmsSecuritySupport.EncryptionAccessLevelIds.NO_MESSAGE_ENCRYPTION.getAccessLevel());
        assertThat(deviceProtocolSecurityPropertySet.getSecurityProperties().size()).isEqualTo(2);
        assertThat(deviceProtocolSecurityPropertySet.getSecurityProperties().getProperty(DeviceSecurityProperty.CLIENT_MAC_ADDRESS.getPropertySpec(propertySpecService).getName())).isEqualTo(new BigDecimal(1));
        assertThat(deviceProtocolSecurityPropertySet.getSecurityProperties().getProperty(DeviceSecurityProperty.PASSWORD.getPropertySpec(propertySpecService).getName())).isEqualTo("some password");
        assertThat(deviceProtocolSecurityPropertySet.getSecurityProperties().getProperty(DeviceSecurityProperty.ENCRYPTION_KEY.getPropertySpec(propertySpecService).getName())).isNull();

    }

    @Test
    public void testSecuredPropertySetConversionWithDifferentTypedPropertiesInAuthenticationAndEncryption() throws Exception {
        DlmsSecuritySupport dlmsSecuritySupport = new DlmsSecuritySupport(propertySpecService);
        TypedProperties securityProperties = TypedProperties.empty();
        securityProperties.setProperty("SecurityLevel", DlmsSecuritySupport.EncryptionAccessLevelIds.MESSAGE_ENCRYPTION_AUTHENTICATION.getAccessLevel() + ":" + DlmsSecuritySupport.AuthenticationAccessLevelIds.LOW_LEVEL_AUTHENTICATION.getAccessLevel());
        securityProperties.setProperty(DeviceSecurityProperty.BINARY_PASSWORD.getPropertySpec(propertySpecService).getName(), "unused");
        securityProperties.setProperty(DeviceSecurityProperty.CLIENT_MAC_ADDRESS.getPropertySpec(propertySpecService).getName(), "some mac address");
        securityProperties.setProperty(DeviceSecurityProperty.ENCRYPTION_KEY.getPropertySpec(propertySpecService).getName(), "some encryption key");
        securityProperties.setProperty(DeviceSecurityProperty.AUTHENTICATION_KEY.getPropertySpec(propertySpecService).getName(), "some authentication key");
        securityProperties.setProperty(DeviceSecurityProperty.PASSWORD.getPropertySpec(propertySpecService).getName(), "some password");
        securityProperties.setProperty(DeviceSecurityProperty.ANSI_C12_USER.getPropertySpec(propertySpecService).getName(), "unused");

        DeviceProtocolSecurityPropertySet deviceProtocolSecurityPropertySet = dlmsSecuritySupport.convertFromTypedProperties(securityProperties);
        assertThat(deviceProtocolSecurityPropertySet.getAuthenticationDeviceAccessLevel()).isEqualTo(DlmsSecuritySupport.EncryptionAccessLevelIds.MESSAGE_ENCRYPTION_AUTHENTICATION.getAccessLevel());
        assertThat(deviceProtocolSecurityPropertySet.getEncryptionDeviceAccessLevel()).isEqualTo(DlmsSecuritySupport.AuthenticationAccessLevelIds.LOW_LEVEL_AUTHENTICATION.getAccessLevel());
        assertThat(deviceProtocolSecurityPropertySet.getSecurityProperties().size()).isEqualTo(4);
        assertThat(deviceProtocolSecurityPropertySet.getSecurityProperties().getProperty(DeviceSecurityProperty.CLIENT_MAC_ADDRESS.getPropertySpec(propertySpecService).getName())).isEqualTo(new BigDecimal(1));
        assertThat(deviceProtocolSecurityPropertySet.getSecurityProperties().getProperty(DeviceSecurityProperty.PASSWORD.getPropertySpec(propertySpecService).getName())).isEqualTo("some password");
        assertThat(deviceProtocolSecurityPropertySet.getSecurityProperties().getProperty(DeviceSecurityProperty.ENCRYPTION_KEY.getPropertySpec(propertySpecService).getName())).isEqualTo("some encryption key");
        assertThat(deviceProtocolSecurityPropertySet.getSecurityProperties().getProperty(DeviceSecurityProperty.AUTHENTICATION_KEY.getPropertySpec(propertySpecService).getName())).isEqualTo("some authentication key");
        assertThat(deviceProtocolSecurityPropertySet.getSecurityProperties().getProperty(DeviceSecurityProperty.ANSI_C12_USER.getPropertySpec(propertySpecService).getName())).isNull();
        assertThat(deviceProtocolSecurityPropertySet.getSecurityProperties().getProperty(DeviceSecurityProperty.BINARY_PASSWORD.getPropertySpec(propertySpecService).getName())).isNull();
    }

    @Test
    public void testClientMacAddressConversion() {
        DlmsSecuritySupport dlmsSecuritySupport = new DlmsSecuritySupport(propertySpecService);
        TypedProperties securityProperties = TypedProperties.empty();
        securityProperties.setProperty(DeviceSecurityProperty.CLIENT_MAC_ADDRESS.getPropertySpec(propertySpecService).getName(), "16");

        DeviceProtocolSecurityPropertySet deviceProtocolSecurityPropertySet = dlmsSecuritySupport.convertFromTypedProperties(securityProperties);
        final Object property = deviceProtocolSecurityPropertySet.getSecurityProperties().getProperty(SecurityPropertySpecName.CLIENT_MAC_ADDRESS.toString());
        assertThat(property).isNotNull();
        assertThat(property).isExactlyInstanceOf(BigDecimal.class);
        assertThat(((BigDecimal)property).intValue()).isEqualTo(16);
    }

}
package com.energyict.protocolimplv2.security;

import com.elster.jupiter.datavault.DataVault;
import com.elster.jupiter.datavault.LegacyDataVaultProvider;
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

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link com.energyict.protocolimplv2.securitysupport.ExtendedAnsiC12SecuritySupport} component
 * <p/>
 * Copyrights EnergyICT
 * Date: 31/01/13
 * Time: 13:34
 */
@RunWith(MockitoJUnitRunner.class)
public class ExtendedAnsiC12SecuritySupportTest {

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
        ExtendedAnsiC12SecuritySupport ansiC12SecuritySupport = newExtendedAnsiC12SecuritySupport();

        // currently only 6 properties are necessary
        assertThat(ansiC12SecuritySupport.getSecurityProperties()).hasSize(6);

        // check for the password propertySpec
        assertThat(ansiC12SecuritySupport.getSecurityProperties()).areExactly(1, new Condition<PropertySpec>() {
            @Override
            public boolean matches(PropertySpec propertySpec) {
                return propertySpec.equals(DeviceSecurityProperty.PASSWORD.getPropertySpec(propertySpecService));
            }
        });
        // check for the ANSI C12 user propertySpec
        assertThat(ansiC12SecuritySupport.getSecurityProperties()).areExactly(1, new Condition<PropertySpec>() {
            @Override
            public boolean matches(PropertySpec propertySpec) {
                return propertySpec.equals(DeviceSecurityProperty.ANSI_C12_USER.getPropertySpec(propertySpecService));
            }
        });
        // check for the ANSI C12 userId propertySpec
        assertThat(ansiC12SecuritySupport.getSecurityProperties()).areExactly(1, new Condition<PropertySpec>() {
            @Override
            public boolean matches(PropertySpec propertySpec) {
                return propertySpec.equals(DeviceSecurityProperty.ANSI_C12_USER_ID.getPropertySpec(propertySpecService));
            }
        });
        // check for the ANSI EncryptionKey
        assertThat(ansiC12SecuritySupport.getSecurityProperties()).areExactly(1, new Condition<PropertySpec>() {
            @Override
            public boolean matches(PropertySpec propertySpec) {
                return propertySpec.equals(DeviceSecurityProperty.ENCRYPTION_KEY.getPropertySpec(propertySpecService));
            }
        });
        // check for the ANSI Called AP Title
        assertThat(ansiC12SecuritySupport.getSecurityProperties()).areExactly(1, new Condition<PropertySpec>() {
            @Override
            public boolean matches(PropertySpec propertySpec) {
                return propertySpec.equals(DeviceSecurityProperty.ANSI_CALLED_AP_TITLE.getPropertySpec(propertySpecService));
            }
        });
        // check for the ANSI binary password property
        assertThat(ansiC12SecuritySupport.getSecurityProperties()).areExactly(1, new Condition<PropertySpec>() {
            @Override
            public boolean matches(PropertySpec propertySpec) {
                return propertySpec.equals(DeviceSecurityProperty.BINARY_PASSWORD.getPropertySpec(propertySpecService));
            }
        });
    }

    @Test
    public void getAuthenticationAccessLevelsTest() {
        ExtendedAnsiC12SecuritySupport ansiC12SecuritySupport = newExtendedAnsiC12SecuritySupport();

        // currently only 3 levels are supported
        assertThat(ansiC12SecuritySupport.getAuthenticationAccessLevels()).hasSize(3);

        // check for no authentication level
        assertThat(ansiC12SecuritySupport.getAuthenticationAccessLevels()).areExactly(1, new Condition<AuthenticationDeviceAccessLevel>() {
            @Override
            public boolean matches(AuthenticationDeviceAccessLevel authenticationDeviceAccessLevel) {
                return authenticationDeviceAccessLevel.getClass().isAssignableFrom(ExtendedAnsiC12SecuritySupport.UnRestrictedAuthentication.class);
            }
        });
        // check for level one authentication
        assertThat(ansiC12SecuritySupport.getAuthenticationAccessLevels()).areExactly(1, new Condition<AuthenticationDeviceAccessLevel>() {
            @Override
            public boolean matches(AuthenticationDeviceAccessLevel authenticationDeviceAccessLevel) {
                return authenticationDeviceAccessLevel.getClass().isAssignableFrom(ExtendedAnsiC12SecuritySupport.RestrictedAuthentication.class);
            }
        });
        // check for level two authentication
        assertThat(ansiC12SecuritySupport.getAuthenticationAccessLevels()).areExactly(1, new Condition<AuthenticationDeviceAccessLevel>() {
            @Override
            public boolean matches(AuthenticationDeviceAccessLevel authenticationDeviceAccessLevel) {
                return authenticationDeviceAccessLevel.getClass().isAssignableFrom(ExtendedAnsiC12SecuritySupport.ReadOnlyAuthentication.class);
            }
        });
    }

    @Test
    public void getEncryptionAccessLevelsTest() {
        ExtendedAnsiC12SecuritySupport ansiC12SecuritySupport = newExtendedAnsiC12SecuritySupport();

        // currently 3 encryption levels are supported
        assertThat(ansiC12SecuritySupport.getEncryptionAccessLevels()).hasSize(3);

        // check for no encryption level
        assertThat(ansiC12SecuritySupport.getEncryptionAccessLevels()).areExactly(1, new Condition<EncryptionDeviceAccessLevel>() {
            @Override
            public boolean matches(EncryptionDeviceAccessLevel encryptionDeviceAccessLevel) {
                return encryptionDeviceAccessLevel.getClass().isAssignableFrom(ExtendedAnsiC12SecuritySupport.NoMessageEncryption.class);
            }
        });
        // check for clearText encryption level
        assertThat(ansiC12SecuritySupport.getEncryptionAccessLevels()).areExactly(1, new Condition<EncryptionDeviceAccessLevel>() {
            @Override
            public boolean matches(EncryptionDeviceAccessLevel encryptionDeviceAccessLevel) {
                return encryptionDeviceAccessLevel.getClass().isAssignableFrom(ExtendedAnsiC12SecuritySupport.ClearTextAuthenticationMessageEncryption.class);
            }
        });
        // check for cipherText encryption level
        assertThat(ansiC12SecuritySupport.getEncryptionAccessLevels()).areExactly(1, new Condition<EncryptionDeviceAccessLevel>() {
            @Override
            public boolean matches(EncryptionDeviceAccessLevel encryptionDeviceAccessLevel) {
                return encryptionDeviceAccessLevel.getClass().isAssignableFrom(ExtendedAnsiC12SecuritySupport.CipherTextAuthenticationMessageEncryption.class);
            }
        });
    }

    @Test
    public void convertToTypedPropertiesTest() {
        ExtendedAnsiC12SecuritySupport ansiC12SecuritySupport = newExtendedAnsiC12SecuritySupport();
        final TypedProperties securityProperties = TypedProperties.empty();
        String ansiC12UserIdValue = "MyAnsiC12UserId";
        securityProperties.setProperty(SecurityPropertySpecName.ANSI_C12_USER_ID.toString(), ansiC12UserIdValue);
        String ansiC12UserValue = "MyAnsiC12User";
        securityProperties.setProperty(SecurityPropertySpecName.ANSI_C12_USER.toString(), ansiC12UserValue);
        String passwordValue = "MyPassword";
        securityProperties.setProperty(SecurityPropertySpecName.PASSWORD.toString(), passwordValue);
        String binaryPassword = "0";
        securityProperties.setProperty(SecurityPropertySpecName.BINARY_PASSWORD.toString(), binaryPassword);
        String encryptionKey = "MyPrivateEncryptionKey";
        securityProperties.setProperty(SecurityPropertySpecName.ENCRYPTION_KEY.toString(), encryptionKey);
        String calledApTitle = "MyPersonalAPTitle";
        securityProperties.setProperty(SecurityPropertySpecName.ANSI_CALLED_AP_TITLE.toString(), calledApTitle);
        DeviceProtocolSecurityPropertySet deviceProtocolSecurityPropertySet =
                new DeviceProtocolSecurityPropertySet() {
                    @Override
                    public int getAuthenticationDeviceAccessLevel() {
                        return 1;
                    }

                    @Override
                    public int getEncryptionDeviceAccessLevel() {
                        return 2;
                    }

                    @Override
                    public TypedProperties getSecurityProperties() {
                        return securityProperties;
                    }
                };

        // business method
        TypedProperties legacyProperties = ansiC12SecuritySupport.convertToTypedProperties(deviceProtocolSecurityPropertySet);

        // asserts
        assertNotNull(legacyProperties);
        assertThat(legacyProperties.getProperty("SecurityLevel")).isEqualTo("1");
        assertThat(legacyProperties.getProperty("C12UserId")).isEqualTo(ansiC12UserIdValue);
        assertThat(legacyProperties.getProperty("C12User")).isEqualTo(ansiC12UserValue);
        assertThat(legacyProperties.getProperty("Password")).isEqualTo(passwordValue);
        assertThat(legacyProperties.getProperty("PasswordBinary")).isEqualTo(binaryPassword);
        assertThat(legacyProperties.getProperty("CalledAPTitle")).isEqualTo(calledApTitle);
        assertThat(legacyProperties.getProperty("SecurityKey")).isEqualTo(encryptionKey);
        assertThat(legacyProperties.getProperty("SecurityMode")).isEqualTo("2");
    }

    protected ExtendedAnsiC12SecuritySupport newExtendedAnsiC12SecuritySupport() {
        ExtendedAnsiC12SecuritySupport extendedAnsiC12SecuritySupport = new ExtendedAnsiC12SecuritySupport();
        extendedAnsiC12SecuritySupport.setPropertySpecService(this.propertySpecService);
        return extendedAnsiC12SecuritySupport;
    }

}
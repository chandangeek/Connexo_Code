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

import java.util.List;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

/**
 * Copyrights EnergyICT
 * Date: 28/05/13
 * Time: 11:22
 */
@RunWith(MockitoJUnitRunner.class)
public class Mtu155SecuritySupportTest {

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
        Mtu155SecuritySupport mtuSecuritySupport = newMtu155SecuritySupport();

        // currently only 4 properties are necessary
        assertThat(mtuSecuritySupport.getSecurityProperties()).hasSize(4);

        // check for the password propertySpec
        assertThat(mtuSecuritySupport.getSecurityProperties()).has(new Condition<List<PropertySpec>>() {
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

        // check for the encryptionKey1 propertySpec
        assertThat(mtuSecuritySupport.getSecurityProperties()).has(new Condition<List<PropertySpec>>() {
            @Override
            public boolean matches(List<PropertySpec> propertySpecs) {
                boolean match = false;
                for (PropertySpec propertySpec : propertySpecs) {
                    if (propertySpec.getName().equals(SecurityPropertySpecName.ENCRYPTION_KEY_1.toString())) {
                        match |= true;
                    }
                }
                return match;
            }
        });

        // check for the encryptionKey2 propertySpec
        assertThat(mtuSecuritySupport.getSecurityProperties()).has(new Condition<List<PropertySpec>>() {
            @Override
            public boolean matches(List<PropertySpec> propertySpecs) {
                boolean match = false;
                for (PropertySpec propertySpec : propertySpecs) {
                    if (propertySpec.getName().equals(SecurityPropertySpecName.ENCRYPTION_KEY_2.toString())) {
                        match |= true;
                    }
                }
                return match;
            }
        });

        // check for the encryptionKey3 propertySpec
        assertThat(mtuSecuritySupport.getSecurityProperties()).has(new Condition<List<PropertySpec>>() {
            @Override
            public boolean matches(List<PropertySpec> propertySpecs) {
                boolean match = false;
                for (PropertySpec propertySpec : propertySpecs) {
                    if (propertySpec.getName().equals(SecurityPropertySpecName.ENCRYPTION_KEY_3.toString())) {
                        match |= true;
                    }
                }
                return match;
            }
        });
    }

    @Test
    public void getAuthenticationAccessLevelsTest() {
        Mtu155SecuritySupport mtu155SecuritySupport = newMtu155SecuritySupport();

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
        Mtu155SecuritySupport mtu155SecuritySupport = newMtu155SecuritySupport();

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
        Mtu155SecuritySupport mtu155SecuritySupport = newMtu155SecuritySupport();
        final TypedProperties securityProperties = TypedProperties.empty();
        String encryptionKey_2_Value = "MyEncryptionKey_2";
        securityProperties.setProperty(SecurityPropertySpecName.ENCRYPTION_KEY_2.toString(), encryptionKey_2_Value);
        String passwordValue = "MyPassword";
        securityProperties.setProperty(SecurityPropertySpecName.PASSWORD.toString(), passwordValue);
        DeviceProtocolSecurityPropertySet deviceProtocolSecurityPropertySet =
                new DeviceProtocolSecurityPropertySet() {
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
        Mtu155SecuritySupport mtu155SecuritySupport = newMtu155SecuritySupport();
        TypedProperties securityProperties = TypedProperties.empty();
        securityProperties.setProperty("SecurityLevel", "321");

        DeviceProtocolSecurityPropertySet securityPropertySet = mtu155SecuritySupport.convertFromTypedProperties(securityProperties);

        assertThat(securityPropertySet).isNotNull();
        assertThat(securityPropertySet.getEncryptionDeviceAccessLevel()).isEqualTo(321);
        assertThat(securityPropertySet.getAuthenticationDeviceAccessLevel()).isEqualTo(0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConvertTypedPropertiesToSecuritySetWithSecurityLevelIllegalAuthenticationLevel() throws Exception {
        Mtu155SecuritySupport mtu155SecuritySupport = newMtu155SecuritySupport();
        TypedProperties securityProperties = TypedProperties.empty();
        securityProperties.setProperty("SecurityLevel", "twee"); // illegal format

        mtu155SecuritySupport.convertFromTypedProperties(securityProperties);
    }

    @Test
    public void testConvertTypedPropertiesToSecuritySetMissingSecurityProperty() throws Exception {
        Mtu155SecuritySupport mtu155SecuritySupport = newMtu155SecuritySupport();
        TypedProperties securityProperties = TypedProperties.empty();

        DeviceProtocolSecurityPropertySet securityPropertySet = mtu155SecuritySupport.convertFromTypedProperties(securityProperties);
        assertThat(securityPropertySet.getEncryptionDeviceAccessLevel()).isEqualTo(1);
        assertThat(securityPropertySet.getAuthenticationDeviceAccessLevel()).isEqualTo(0);
    }

    @Test
    public void testPasswordConversion() {
        Mtu155SecuritySupport mtu155SecuritySupport = newMtu155SecuritySupport();
        final TypedProperties securityProperties = TypedProperties.empty();
        String passwordValue = "MyPassword";
        Password password = new Password(passwordValue);
        securityProperties.setProperty(SecurityPropertySpecName.PASSWORD.toString(), password);
        DeviceProtocolSecurityPropertySet deviceProtocolSecurityPropertySet =
                new DeviceProtocolSecurityPropertySet() {
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
        assertThat(legacyProperties.getProperty(SecurityPropertySpecName.PASSWORD.toString())).isEqualTo(passwordValue);
    }

    protected Mtu155SecuritySupport newMtu155SecuritySupport() {
        Mtu155SecuritySupport mtu155SecuritySupport = new Mtu155SecuritySupport();
        mtu155SecuritySupport.setPropertySpecService(this.propertySpecService);
        return mtu155SecuritySupport;
    }

}
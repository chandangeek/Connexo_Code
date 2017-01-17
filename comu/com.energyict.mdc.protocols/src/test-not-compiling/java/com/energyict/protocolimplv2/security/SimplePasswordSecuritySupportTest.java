package com.energyict.protocolimplv2.security;

import com.energyict.mdc.common.TypedProperties;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.protocol.api.security.DeviceAccessLevel;
import com.energyict.mdc.upl.security.DeviceProtocolSecurityPropertySet;
import com.elster.jupiter.datavault.DataVault;
import com.elster.jupiter.datavault.LegacyDataVaultProvider;
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
 * Tests the {@link com.energyict.protocolimplv2.securitysupport.SimplePasswordSecuritySupport} component
 * <p/>
 * Copyrights EnergyICT
 * Date: 11/01/13
 * Time: 14:56
 */
@RunWith(MockitoJUnitRunner.class)
public class SimplePasswordSecuritySupportTest {

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
        SimplePasswordSecuritySupport simplePasswordSecuritySupport = newSimplePasswordSecuritySupport();

        // assert that you only have one property to set
        assertThat(simplePasswordSecuritySupport.getSecurityProperties()).hasSize(1);


        // check for the password propertySpec
        assertThat(simplePasswordSecuritySupport.getSecurityProperties()).has(new Condition<List<PropertySpec>>() {
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
    }

    protected SimplePasswordSecuritySupport newSimplePasswordSecuritySupport() {
        SimplePasswordSecuritySupport simplePasswordSecuritySupport = new SimplePasswordSecuritySupport();
        simplePasswordSecuritySupport.setPropertySpecService(this.propertySpecService);
        return simplePasswordSecuritySupport;
    }

    @Test
    public void getAuthenticationAccessLevelsTest() {
        SimplePasswordSecuritySupport simplePasswordSecuritySupport = newSimplePasswordSecuritySupport();

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
        SimplePasswordSecuritySupport simplePasswordSecuritySupport = newSimplePasswordSecuritySupport();

        // assert that we don't have any encryption level
        assertThat(simplePasswordSecuritySupport.getEncryptionAccessLevels()).isEmpty();
    }

    @Test
    public void convertToTypedPropertiesTest(){
        SimplePasswordSecuritySupport simplePasswordSecuritySupport = newSimplePasswordSecuritySupport();
        final TypedProperties securityProperties = TypedProperties.empty();
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
                        return DeviceAccessLevel.NOT_USED_DEVICE_ACCESS_LEVEL_ID;
                    }

                    @Override
                    public TypedProperties getSecurityProperties() {
                        return securityProperties;
                    }
                };

        // business method
        TypedProperties legacyProperties = simplePasswordSecuritySupport.convertToTypedProperties(deviceProtocolSecurityPropertySet);

        // asserts
        assertNotNull(legacyProperties);
        assertThat(legacyProperties.getProperty("Password")).isEqualTo(passwordValue);
    }

    @Test
    public void testConvertFromTypedProperties() throws Exception {
        SimplePasswordSecuritySupport simplePasswordSecuritySupport = newSimplePasswordSecuritySupport();
        TypedProperties securityProperties = TypedProperties.empty();
        String passwordValue = "MyPassword";
        securityProperties.setProperty(DeviceSecurityProperty.PASSWORD.getPropertySpec(this.propertySpecService).getName(), passwordValue);

        DeviceProtocolSecurityPropertySet securityPropertySet = simplePasswordSecuritySupport.convertFromTypedProperties(securityProperties);
        assertThat(securityPropertySet).isNotNull();
        assertThat(securityPropertySet.getAuthenticationDeviceAccessLevel()).isEqualTo(0);
        assertThat(securityPropertySet.getEncryptionDeviceAccessLevel()).isEqualTo(-1);
        assertThat(securityPropertySet.getSecurityProperties()).isNotNull();
        assertThat(securityPropertySet.getSecurityProperties().size()).isEqualTo(1);
        assertThat(securityPropertySet.getSecurityProperties().getProperty(DeviceSecurityProperty.PASSWORD.getPropertySpec(this.propertySpecService).getName())).isEqualTo("MyPassword");
    }
}

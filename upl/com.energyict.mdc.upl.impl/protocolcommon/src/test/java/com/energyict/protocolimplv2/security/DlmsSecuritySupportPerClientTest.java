package com.energyict.protocolimplv2.security;

import com.energyict.mdc.upl.properties.Password;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.upl.security.DeviceProtocolSecurityPropertySet;
import com.energyict.mdc.upl.security.EncryptionDeviceAccessLevel;

import com.energyict.protocolimpl.properties.TypedProperties;
import org.fest.assertions.core.Condition;

import java.math.BigDecimal;
import java.util.List;

import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;

/**
 * Copyrights EnergyICT
 * Date: 19/06/13
 * Time: 10:05
 */
public class DlmsSecuritySupportPerClientTest {

    @Test
    public void getSecurityPropertiesTest() {
        DlmsSecuritySupportPerClient dlmsSecuritySupportPerClient = new DlmsSecuritySupportPerClient();

        // currently only 4 properties are necessary
        assertThat(dlmsSecuritySupportPerClient.getSecurityProperties()).hasSize(3*6);

        assertThat(dlmsSecuritySupportPerClient.getSecurityProperties()).has(new Condition<List<PropertySpec>>() {
            @Override
            public boolean matches(List<PropertySpec> propertySpecs) {
                boolean match = false;
                for (PropertySpec propertySpec : propertySpecs) {
                    if (propertySpec.getName().equals(SecurityPropertySpecName.PASSWORD_PUBLIC.toString())) {
                        match |= true;
                    }
                }
                return match;
            }
        });

        assertThat(dlmsSecuritySupportPerClient.getSecurityProperties()).has(new Condition<List<PropertySpec>>() {
            @Override
            public boolean matches(List<PropertySpec> propertySpecs) {
                boolean match = false;
                for (PropertySpec propertySpec : propertySpecs) {
                    if (propertySpec.getName().equals(SecurityPropertySpecName.PASSWORD_DATA.toString())) {
                        match |= true;
                    }
                }
                return match;
            }
        });

        assertThat(dlmsSecuritySupportPerClient.getSecurityProperties()).has(new Condition<List<PropertySpec>>() {
            @Override
            public boolean matches(List<PropertySpec> propertySpecs) {
                boolean match = false;
                for (PropertySpec propertySpec : propertySpecs) {
                    if (propertySpec.getName().equals(SecurityPropertySpecName.PASSWORD_EXT_DATA.toString())) {
                        match |= true;
                    }
                }
                return match;
            }
        });

        assertThat(dlmsSecuritySupportPerClient.getSecurityProperties()).has(new Condition<List<PropertySpec>>() {
            @Override
            public boolean matches(List<PropertySpec> propertySpecs) {
                boolean match = false;
                for (PropertySpec propertySpec : propertySpecs) {
                    if (propertySpec.getName().equals(SecurityPropertySpecName.PASSWORD_MANAGEMENT.toString())) {
                        match |= true;
                    }
                }
                return match;
            }
        });

        assertThat(dlmsSecuritySupportPerClient.getSecurityProperties()).has(new Condition<List<PropertySpec>>() {
            @Override
            public boolean matches(List<PropertySpec> propertySpecs) {
                boolean match = false;
                for (PropertySpec propertySpec : propertySpecs) {
                    if (propertySpec.getName().equals(SecurityPropertySpecName.PASSWORD_FIRMWARE.toString())) {
                        match |= true;
                    }
                }
                return match;
            }
        });

        assertThat(dlmsSecuritySupportPerClient.getSecurityProperties()).has(new Condition<List<PropertySpec>>() {
            @Override
            public boolean matches(List<PropertySpec> propertySpecs) {
                boolean match = false;
                for (PropertySpec propertySpec : propertySpecs) {
                    if (propertySpec.getName().equals(SecurityPropertySpecName.PASSWORD_MANUFACTURER.toString())) {
                        match |= true;
                    }
                }
                return match;
            }
        });

        assertThat(dlmsSecuritySupportPerClient.getSecurityProperties()).has(new Condition<List<PropertySpec>>() {
            @Override
            public boolean matches(List<PropertySpec> propertySpecs) {
                boolean match = false;
                for (PropertySpec propertySpec : propertySpecs) {
                    if (propertySpec.getName().equals(SecurityPropertySpecName.ENCRYPTION_KEY_PUBLIC.toString())) {
                        match |= true;
                    }
                }
                return match;
            }
        });


        assertThat(dlmsSecuritySupportPerClient.getSecurityProperties()).has(new Condition<List<PropertySpec>>() {
            @Override
            public boolean matches(List<PropertySpec> propertySpecs) {
                boolean match = false;
                for (PropertySpec propertySpec : propertySpecs) {
                    if (propertySpec.getName().equals(SecurityPropertySpecName.ENCRYPTION_KEY_DATA.toString())) {
                        match |= true;
                    }
                }
                return match;
            }
        });


        assertThat(dlmsSecuritySupportPerClient.getSecurityProperties()).has(new Condition<List<PropertySpec>>() {
            @Override
            public boolean matches(List<PropertySpec> propertySpecs) {
                boolean match = false;
                for (PropertySpec propertySpec : propertySpecs) {
                    if (propertySpec.getName().equals(SecurityPropertySpecName.ENCRYPTION_KEY_EXT_DATA.toString())) {
                        match |= true;
                    }
                }
                return match;
            }
        });


        assertThat(dlmsSecuritySupportPerClient.getSecurityProperties()).has(new Condition<List<PropertySpec>>() {
            @Override
            public boolean matches(List<PropertySpec> propertySpecs) {
                boolean match = false;
                for (PropertySpec propertySpec : propertySpecs) {
                    if (propertySpec.getName().equals(SecurityPropertySpecName.ENCRYPTION_KEY_MANAGEMENT.toString())) {
                        match |= true;
                    }
                }
                return match;
            }
        });


        assertThat(dlmsSecuritySupportPerClient.getSecurityProperties()).has(new Condition<List<PropertySpec>>() {
            @Override
            public boolean matches(List<PropertySpec> propertySpecs) {
                boolean match = false;
                for (PropertySpec propertySpec : propertySpecs) {
                    if (propertySpec.getName().equals(SecurityPropertySpecName.ENCRYPTION_KEY_FIRMWARE.toString())) {
                        match |= true;
                    }
                }
                return match;
            }
        });


        assertThat(dlmsSecuritySupportPerClient.getSecurityProperties()).has(new Condition<List<PropertySpec>>() {
            @Override
            public boolean matches(List<PropertySpec> propertySpecs) {
                boolean match = false;
                for (PropertySpec propertySpec : propertySpecs) {
                    if (propertySpec.getName().equals(SecurityPropertySpecName.ENCRYPTION_KEY_MANUFACTURER.toString())) {
                        match |= true;
                    }
                }
                return match;
            }
        });

        assertThat(dlmsSecuritySupportPerClient.getSecurityProperties()).has(new Condition<List<PropertySpec>>() {
            @Override
            public boolean matches(List<PropertySpec> propertySpecs) {
                boolean match = false;
                for (PropertySpec propertySpec : propertySpecs) {
                    if (propertySpec.getName().equals(SecurityPropertySpecName.AUTHENTICATION_KEY_PUBLIC.toString())) {
                        match |= true;
                    }
                }
                return match;
            }
        });


        assertThat(dlmsSecuritySupportPerClient.getSecurityProperties()).has(new Condition<List<PropertySpec>>() {
            @Override
            public boolean matches(List<PropertySpec> propertySpecs) {
                boolean match = false;
                for (PropertySpec propertySpec : propertySpecs) {
                    if (propertySpec.getName().equals(SecurityPropertySpecName.AUTHENTICATION_KEY_DATA.toString())) {
                        match |= true;
                    }
                }
                return match;
            }
        });


        assertThat(dlmsSecuritySupportPerClient.getSecurityProperties()).has(new Condition<List<PropertySpec>>() {
            @Override
            public boolean matches(List<PropertySpec> propertySpecs) {
                boolean match = false;
                for (PropertySpec propertySpec : propertySpecs) {
                    if (propertySpec.getName().equals(SecurityPropertySpecName.AUTHENTICATION_KEY_EXT_DATA.toString())) {
                        match |= true;
                    }
                }
                return match;
            }
        });


        assertThat(dlmsSecuritySupportPerClient.getSecurityProperties()).has(new Condition<List<PropertySpec>>() {
            @Override
            public boolean matches(List<PropertySpec> propertySpecs) {
                boolean match = false;
                for (PropertySpec propertySpec : propertySpecs) {
                    if (propertySpec.getName().equals(SecurityPropertySpecName.AUTHENTICATION_KEY_MANAGEMENT.toString())) {
                        match |= true;
                    }
                }
                return match;
            }
        });


        assertThat(dlmsSecuritySupportPerClient.getSecurityProperties()).has(new Condition<List<PropertySpec>>() {
            @Override
            public boolean matches(List<PropertySpec> propertySpecs) {
                boolean match = false;
                for (PropertySpec propertySpec : propertySpecs) {
                    if (propertySpec.getName().equals(SecurityPropertySpecName.AUTHENTICATION_KEY_FIRMWARE.toString())) {
                        match |= true;
                    }
                }
                return match;
            }
        });


        assertThat(dlmsSecuritySupportPerClient.getSecurityProperties()).has(new Condition<List<PropertySpec>>() {
            @Override
            public boolean matches(List<PropertySpec> propertySpecs) {
                boolean match = false;
                for (PropertySpec propertySpec : propertySpecs) {
                    if (propertySpec.getName().equals(SecurityPropertySpecName.AUTHENTICATION_KEY_MANUFACTURER.toString())) {
                        match |= true;
                    }
                }
                return match;
            }
        });

    }

    @Test
    public void getAuthenticationAccessLevelsTest() {
        DlmsSecuritySupportPerClient dlmsSecuritySupportPerClient = new DlmsSecuritySupportPerClient();

        // currently only 30 levels are supported
        assertThat(dlmsSecuritySupportPerClient.getAuthenticationAccessLevels()).hasSize(5 * 6);

        assertThat(dlmsSecuritySupportPerClient.getAuthenticationAccessLevels()).has(new Condition<List<AuthenticationDeviceAccessLevel>>() {
            @Override
            public boolean matches(List<AuthenticationDeviceAccessLevel> authenticationDeviceAccessLevels) {
                boolean match = false;
                for (AuthenticationDeviceAccessLevel authenticationDeviceAccessLevel : authenticationDeviceAccessLevels) {
                    if (authenticationDeviceAccessLevel.getClass().isAssignableFrom(DlmsSecuritySupportPerClient.NoAuthenticationPublic.class)) {
                        match |= true;
                    }
                }
                return match;
            }
        });

        assertThat(dlmsSecuritySupportPerClient.getAuthenticationAccessLevels()).has(new Condition<List<AuthenticationDeviceAccessLevel>>() {
            @Override
            public boolean matches(List<AuthenticationDeviceAccessLevel> authenticationDeviceAccessLevels) {
                boolean match = false;
                for (AuthenticationDeviceAccessLevel authenticationDeviceAccessLevel : authenticationDeviceAccessLevels) {
                    if (authenticationDeviceAccessLevel.getClass().isAssignableFrom(DlmsSecuritySupportPerClient.LowLevelAuthenticationPublic.class)) {
                        match |= true;
                    }
                }
                return match;
            }
        });

        assertThat(dlmsSecuritySupportPerClient.getAuthenticationAccessLevels()).has(new Condition<List<AuthenticationDeviceAccessLevel>>() {
            @Override
            public boolean matches(List<AuthenticationDeviceAccessLevel> authenticationDeviceAccessLevels) {
                boolean match = false;
                for (AuthenticationDeviceAccessLevel authenticationDeviceAccessLevel : authenticationDeviceAccessLevels) {
                    if (authenticationDeviceAccessLevel.getClass().isAssignableFrom(DlmsSecuritySupportPerClient.Md5AuthenticationPublic.class)) {
                        match |= true;
                    }
                }
                return match;
            }
        });

        assertThat(dlmsSecuritySupportPerClient.getAuthenticationAccessLevels()).has(new Condition<List<AuthenticationDeviceAccessLevel>>() {
            @Override
            public boolean matches(List<AuthenticationDeviceAccessLevel> authenticationDeviceAccessLevels) {
                boolean match = false;
                for (AuthenticationDeviceAccessLevel authenticationDeviceAccessLevel : authenticationDeviceAccessLevels) {
                    if (authenticationDeviceAccessLevel.getClass().isAssignableFrom(DlmsSecuritySupportPerClient.ShaAuthenticationPublic.class)) {
                        match |= true;
                    }
                }
                return match;
            }
        });

        assertThat(dlmsSecuritySupportPerClient.getAuthenticationAccessLevels()).has(new Condition<List<AuthenticationDeviceAccessLevel>>() {
            @Override
            public boolean matches(List<AuthenticationDeviceAccessLevel> authenticationDeviceAccessLevels) {
                boolean match = false;
                for (AuthenticationDeviceAccessLevel authenticationDeviceAccessLevel : authenticationDeviceAccessLevels) {
                    if (authenticationDeviceAccessLevel.getClass().isAssignableFrom(DlmsSecuritySupportPerClient.GmacAuthenticationPublic.class)) {
                        match |= true;
                    }
                }
                return match;
            }
        });
     assertThat(dlmsSecuritySupportPerClient.getAuthenticationAccessLevels()).has(new Condition<List<AuthenticationDeviceAccessLevel>>() {
            @Override
            public boolean matches(List<AuthenticationDeviceAccessLevel> authenticationDeviceAccessLevels) {
                boolean match = false;
                for (AuthenticationDeviceAccessLevel authenticationDeviceAccessLevel : authenticationDeviceAccessLevels) {
                    if (authenticationDeviceAccessLevel.getClass().isAssignableFrom(DlmsSecuritySupportPerClient.NoAuthenticationDataCollection.class)) {
                        match |= true;
                    }
                }
                return match;
            }
        });

        assertThat(dlmsSecuritySupportPerClient.getAuthenticationAccessLevels()).has(new Condition<List<AuthenticationDeviceAccessLevel>>() {
            @Override
            public boolean matches(List<AuthenticationDeviceAccessLevel> authenticationDeviceAccessLevels) {
                boolean match = false;
                for (AuthenticationDeviceAccessLevel authenticationDeviceAccessLevel : authenticationDeviceAccessLevels) {
                    if (authenticationDeviceAccessLevel.getClass().isAssignableFrom(DlmsSecuritySupportPerClient.LowLevelAuthenticationData.class)) {
                        match |= true;
                    }
                }
                return match;
            }
        });

        assertThat(dlmsSecuritySupportPerClient.getAuthenticationAccessLevels()).has(new Condition<List<AuthenticationDeviceAccessLevel>>() {
            @Override
            public boolean matches(List<AuthenticationDeviceAccessLevel> authenticationDeviceAccessLevels) {
                boolean match = false;
                for (AuthenticationDeviceAccessLevel authenticationDeviceAccessLevel : authenticationDeviceAccessLevels) {
                    if (authenticationDeviceAccessLevel.getClass().isAssignableFrom(DlmsSecuritySupportPerClient.Md5AuthenticationData.class)) {
                        match |= true;
                    }
                }
                return match;
            }
        });

        assertThat(dlmsSecuritySupportPerClient.getAuthenticationAccessLevels()).has(new Condition<List<AuthenticationDeviceAccessLevel>>() {
            @Override
            public boolean matches(List<AuthenticationDeviceAccessLevel> authenticationDeviceAccessLevels) {
                boolean match = false;
                for (AuthenticationDeviceAccessLevel authenticationDeviceAccessLevel : authenticationDeviceAccessLevels) {
                    if (authenticationDeviceAccessLevel.getClass().isAssignableFrom(DlmsSecuritySupportPerClient.ShaAuthenticationData.class)) {
                        match |= true;
                    }
                }
                return match;
            }
        });

        assertThat(dlmsSecuritySupportPerClient.getAuthenticationAccessLevels()).has(new Condition<List<AuthenticationDeviceAccessLevel>>() {
            @Override
            public boolean matches(List<AuthenticationDeviceAccessLevel> authenticationDeviceAccessLevels) {
                boolean match = false;
                for (AuthenticationDeviceAccessLevel authenticationDeviceAccessLevel : authenticationDeviceAccessLevels) {
                    if (authenticationDeviceAccessLevel.getClass().isAssignableFrom(DlmsSecuritySupportPerClient.GmacAuthenticationData.class)) {
                        match |= true;
                    }
                }
                return match;
            }
        });
     assertThat(dlmsSecuritySupportPerClient.getAuthenticationAccessLevels()).has(new Condition<List<AuthenticationDeviceAccessLevel>>() {
            @Override
            public boolean matches(List<AuthenticationDeviceAccessLevel> authenticationDeviceAccessLevels) {
                boolean match = false;
                for (AuthenticationDeviceAccessLevel authenticationDeviceAccessLevel : authenticationDeviceAccessLevels) {
                    if (authenticationDeviceAccessLevel.getClass().isAssignableFrom(DlmsSecuritySupportPerClient.NoAuthenticationExtendedDataCollection.class)) {
                        match |= true;
                    }
                }
                return match;
            }
        });

        assertThat(dlmsSecuritySupportPerClient.getAuthenticationAccessLevels()).has(new Condition<List<AuthenticationDeviceAccessLevel>>() {
            @Override
            public boolean matches(List<AuthenticationDeviceAccessLevel> authenticationDeviceAccessLevels) {
                boolean match = false;
                for (AuthenticationDeviceAccessLevel authenticationDeviceAccessLevel : authenticationDeviceAccessLevels) {
                    if (authenticationDeviceAccessLevel.getClass().isAssignableFrom(DlmsSecuritySupportPerClient.LowLevelAuthenticationExtendedData.class)) {
                        match |= true;
                    }
                }
                return match;
            }
        });

        assertThat(dlmsSecuritySupportPerClient.getAuthenticationAccessLevels()).has(new Condition<List<AuthenticationDeviceAccessLevel>>() {
            @Override
            public boolean matches(List<AuthenticationDeviceAccessLevel> authenticationDeviceAccessLevels) {
                boolean match = false;
                for (AuthenticationDeviceAccessLevel authenticationDeviceAccessLevel : authenticationDeviceAccessLevels) {
                    if (authenticationDeviceAccessLevel.getClass().isAssignableFrom(DlmsSecuritySupportPerClient.Md5AuthenticationExtendedData.class)) {
                        match |= true;
                    }
                }
                return match;
            }
        });

        assertThat(dlmsSecuritySupportPerClient.getAuthenticationAccessLevels()).has(new Condition<List<AuthenticationDeviceAccessLevel>>() {
            @Override
            public boolean matches(List<AuthenticationDeviceAccessLevel> authenticationDeviceAccessLevels) {
                boolean match = false;
                for (AuthenticationDeviceAccessLevel authenticationDeviceAccessLevel : authenticationDeviceAccessLevels) {
                    if (authenticationDeviceAccessLevel.getClass().isAssignableFrom(DlmsSecuritySupportPerClient.ShaAuthenticationExtendedData.class)) {
                        match |= true;
                    }
                }
                return match;
            }
        });

        assertThat(dlmsSecuritySupportPerClient.getAuthenticationAccessLevels()).has(new Condition<List<AuthenticationDeviceAccessLevel>>() {
            @Override
            public boolean matches(List<AuthenticationDeviceAccessLevel> authenticationDeviceAccessLevels) {
                boolean match = false;
                for (AuthenticationDeviceAccessLevel authenticationDeviceAccessLevel : authenticationDeviceAccessLevels) {
                    if (authenticationDeviceAccessLevel.getClass().isAssignableFrom(DlmsSecuritySupportPerClient.GmacAuthenticationExtendedData.class)) {
                        match |= true;
                    }
                }
                return match;
            }
        });
     assertThat(dlmsSecuritySupportPerClient.getAuthenticationAccessLevels()).has(new Condition<List<AuthenticationDeviceAccessLevel>>() {
            @Override
            public boolean matches(List<AuthenticationDeviceAccessLevel> authenticationDeviceAccessLevels) {
                boolean match = false;
                for (AuthenticationDeviceAccessLevel authenticationDeviceAccessLevel : authenticationDeviceAccessLevels) {
                    if (authenticationDeviceAccessLevel.getClass().isAssignableFrom(DlmsSecuritySupportPerClient.NoAuthenticationManagement.class)) {
                        match |= true;
                    }
                }
                return match;
            }
        });

        assertThat(dlmsSecuritySupportPerClient.getAuthenticationAccessLevels()).has(new Condition<List<AuthenticationDeviceAccessLevel>>() {
            @Override
            public boolean matches(List<AuthenticationDeviceAccessLevel> authenticationDeviceAccessLevels) {
                boolean match = false;
                for (AuthenticationDeviceAccessLevel authenticationDeviceAccessLevel : authenticationDeviceAccessLevels) {
                    if (authenticationDeviceAccessLevel.getClass().isAssignableFrom(DlmsSecuritySupportPerClient.LowLevelAuthenticationManagement.class)) {
                        match |= true;
                    }
                }
                return match;
            }
        });

        assertThat(dlmsSecuritySupportPerClient.getAuthenticationAccessLevels()).has(new Condition<List<AuthenticationDeviceAccessLevel>>() {
            @Override
            public boolean matches(List<AuthenticationDeviceAccessLevel> authenticationDeviceAccessLevels) {
                boolean match = false;
                for (AuthenticationDeviceAccessLevel authenticationDeviceAccessLevel : authenticationDeviceAccessLevels) {
                    if (authenticationDeviceAccessLevel.getClass().isAssignableFrom(DlmsSecuritySupportPerClient.Md5AuthenticationManagement.class)) {
                        match |= true;
                    }
                }
                return match;
            }
        });

        assertThat(dlmsSecuritySupportPerClient.getAuthenticationAccessLevels()).has(new Condition<List<AuthenticationDeviceAccessLevel>>() {
            @Override
            public boolean matches(List<AuthenticationDeviceAccessLevel> authenticationDeviceAccessLevels) {
                boolean match = false;
                for (AuthenticationDeviceAccessLevel authenticationDeviceAccessLevel : authenticationDeviceAccessLevels) {
                    if (authenticationDeviceAccessLevel.getClass().isAssignableFrom(DlmsSecuritySupportPerClient.ShaAuthenticationManagement.class)) {
                        match |= true;
                    }
                }
                return match;
            }
        });

        assertThat(dlmsSecuritySupportPerClient.getAuthenticationAccessLevels()).has(new Condition<List<AuthenticationDeviceAccessLevel>>() {
            @Override
            public boolean matches(List<AuthenticationDeviceAccessLevel> authenticationDeviceAccessLevels) {
                boolean match = false;
                for (AuthenticationDeviceAccessLevel authenticationDeviceAccessLevel : authenticationDeviceAccessLevels) {
                    if (authenticationDeviceAccessLevel.getClass().isAssignableFrom(DlmsSecuritySupportPerClient.GmacAuthenticationManagement.class)) {
                        match |= true;
                    }
                }
                return match;
            }
        });
     assertThat(dlmsSecuritySupportPerClient.getAuthenticationAccessLevels()).has(new Condition<List<AuthenticationDeviceAccessLevel>>() {
            @Override
            public boolean matches(List<AuthenticationDeviceAccessLevel> authenticationDeviceAccessLevels) {
                boolean match = false;
                for (AuthenticationDeviceAccessLevel authenticationDeviceAccessLevel : authenticationDeviceAccessLevels) {
                    if (authenticationDeviceAccessLevel.getClass().isAssignableFrom(DlmsSecuritySupportPerClient.NoAuthenticationFirmware.class)) {
                        match |= true;
                    }
                }
                return match;
            }
        });

        assertThat(dlmsSecuritySupportPerClient.getAuthenticationAccessLevels()).has(new Condition<List<AuthenticationDeviceAccessLevel>>() {
            @Override
            public boolean matches(List<AuthenticationDeviceAccessLevel> authenticationDeviceAccessLevels) {
                boolean match = false;
                for (AuthenticationDeviceAccessLevel authenticationDeviceAccessLevel : authenticationDeviceAccessLevels) {
                    if (authenticationDeviceAccessLevel.getClass().isAssignableFrom(DlmsSecuritySupportPerClient.LowLevelAuthenticationFirmware.class)) {
                        match |= true;
                    }
                }
                return match;
            }
        });

        assertThat(dlmsSecuritySupportPerClient.getAuthenticationAccessLevels()).has(new Condition<List<AuthenticationDeviceAccessLevel>>() {
            @Override
            public boolean matches(List<AuthenticationDeviceAccessLevel> authenticationDeviceAccessLevels) {
                boolean match = false;
                for (AuthenticationDeviceAccessLevel authenticationDeviceAccessLevel : authenticationDeviceAccessLevels) {
                    if (authenticationDeviceAccessLevel.getClass().isAssignableFrom(DlmsSecuritySupportPerClient.Md5AuthenticationFirmware.class)) {
                        match |= true;
                    }
                }
                return match;
            }
        });

        assertThat(dlmsSecuritySupportPerClient.getAuthenticationAccessLevels()).has(new Condition<List<AuthenticationDeviceAccessLevel>>() {
            @Override
            public boolean matches(List<AuthenticationDeviceAccessLevel> authenticationDeviceAccessLevels) {
                boolean match = false;
                for (AuthenticationDeviceAccessLevel authenticationDeviceAccessLevel : authenticationDeviceAccessLevels) {
                    if (authenticationDeviceAccessLevel.getClass().isAssignableFrom(DlmsSecuritySupportPerClient.ShaAuthenticationFirmware.class)) {
                        match |= true;
                    }
                }
                return match;
            }
        });

        assertThat(dlmsSecuritySupportPerClient.getAuthenticationAccessLevels()).has(new Condition<List<AuthenticationDeviceAccessLevel>>() {
            @Override
            public boolean matches(List<AuthenticationDeviceAccessLevel> authenticationDeviceAccessLevels) {
                boolean match = false;
                for (AuthenticationDeviceAccessLevel authenticationDeviceAccessLevel : authenticationDeviceAccessLevels) {
                    if (authenticationDeviceAccessLevel.getClass().isAssignableFrom(DlmsSecuritySupportPerClient.GmacAuthenticationFirmware.class)) {
                        match |= true;
                    }
                }
                return match;
            }
        });
     assertThat(dlmsSecuritySupportPerClient.getAuthenticationAccessLevels()).has(new Condition<List<AuthenticationDeviceAccessLevel>>() {
            @Override
            public boolean matches(List<AuthenticationDeviceAccessLevel> authenticationDeviceAccessLevels) {
                boolean match = false;
                for (AuthenticationDeviceAccessLevel authenticationDeviceAccessLevel : authenticationDeviceAccessLevels) {
                    if (authenticationDeviceAccessLevel.getClass().isAssignableFrom(DlmsSecuritySupportPerClient.NoAuthenticationManufacturer.class)) {
                        match |= true;
                    }
                }
                return match;
            }
        });

        assertThat(dlmsSecuritySupportPerClient.getAuthenticationAccessLevels()).has(new Condition<List<AuthenticationDeviceAccessLevel>>() {
            @Override
            public boolean matches(List<AuthenticationDeviceAccessLevel> authenticationDeviceAccessLevels) {
                boolean match = false;
                for (AuthenticationDeviceAccessLevel authenticationDeviceAccessLevel : authenticationDeviceAccessLevels) {
                    if (authenticationDeviceAccessLevel.getClass().isAssignableFrom(DlmsSecuritySupportPerClient.LowLevelAuthenticationManufacturer.class)) {
                        match |= true;
                    }
                }
                return match;
            }
        });

        assertThat(dlmsSecuritySupportPerClient.getAuthenticationAccessLevels()).has(new Condition<List<AuthenticationDeviceAccessLevel>>() {
            @Override
            public boolean matches(List<AuthenticationDeviceAccessLevel> authenticationDeviceAccessLevels) {
                boolean match = false;
                for (AuthenticationDeviceAccessLevel authenticationDeviceAccessLevel : authenticationDeviceAccessLevels) {
                    if (authenticationDeviceAccessLevel.getClass().isAssignableFrom(DlmsSecuritySupportPerClient.Md5AuthenticationManufacturer.class)) {
                        match |= true;
                    }
                }
                return match;
            }
        });

        assertThat(dlmsSecuritySupportPerClient.getAuthenticationAccessLevels()).has(new Condition<List<AuthenticationDeviceAccessLevel>>() {
            @Override
            public boolean matches(List<AuthenticationDeviceAccessLevel> authenticationDeviceAccessLevels) {
                boolean match = false;
                for (AuthenticationDeviceAccessLevel authenticationDeviceAccessLevel : authenticationDeviceAccessLevels) {
                    if (authenticationDeviceAccessLevel.getClass().isAssignableFrom(DlmsSecuritySupportPerClient.ShaAuthenticationManufacturer.class)) {
                        match |= true;
                    }
                }
                return match;
            }
        });

        assertThat(dlmsSecuritySupportPerClient.getAuthenticationAccessLevels()).has(new Condition<List<AuthenticationDeviceAccessLevel>>() {
            @Override
            public boolean matches(List<AuthenticationDeviceAccessLevel> authenticationDeviceAccessLevels) {
                boolean match = false;
                for (AuthenticationDeviceAccessLevel authenticationDeviceAccessLevel : authenticationDeviceAccessLevels) {
                    if (authenticationDeviceAccessLevel.getClass().isAssignableFrom(DlmsSecuritySupportPerClient.GmacAuthenticationManufacturer.class)) {
                        match |= true;
                    }
                }
                return match;
            }
        });
    }


    @Test
    public void getEncryptionAccessLevelsTest() {
        DlmsSecuritySupportPerClient dlmsSecuritySupportPerClient = new DlmsSecuritySupportPerClient();

        // currently only 24 levels are supported
        assertThat(dlmsSecuritySupportPerClient.getEncryptionAccessLevels()).hasSize(4 * 6);

        // check for the no encryption security
        assertThat(dlmsSecuritySupportPerClient.getEncryptionAccessLevels()).has(new Condition<List<EncryptionDeviceAccessLevel>>() {
            @Override
            public boolean matches(List<EncryptionDeviceAccessLevel> encryptionDeviceAccessLevels) {
                boolean match = false;
                for (EncryptionDeviceAccessLevel encryptionDeviceAccessLevel : encryptionDeviceAccessLevels) {
                    if (encryptionDeviceAccessLevel.getClass().isAssignableFrom(DlmsSecuritySupportPerClient.NoEncryptionPublic.class)) {
                        match |= true;
                    }
                }
                return match;
            }
        });

        // check for the message encryption security
        assertThat(dlmsSecuritySupportPerClient.getEncryptionAccessLevels()).has(new Condition<List<EncryptionDeviceAccessLevel>>() {
            @Override
            public boolean matches(List<EncryptionDeviceAccessLevel> encryptionDeviceAccessLevels) {
                boolean match = false;
                for (EncryptionDeviceAccessLevel encryptionDeviceAccessLevel : encryptionDeviceAccessLevels) {
                    if (encryptionDeviceAccessLevel.getClass().isAssignableFrom(DlmsSecuritySupportPerClient.MessageEncryptionPublic.class)) {
                        match |= true;
                    }
                }
                return match;
            }
        });

        // check for the message authentication security
        assertThat(dlmsSecuritySupportPerClient.getEncryptionAccessLevels()).has(new Condition<List<EncryptionDeviceAccessLevel>>() {
            @Override
            public boolean matches(List<EncryptionDeviceAccessLevel> encryptionDeviceAccessLevels) {
                boolean match = false;
                for (EncryptionDeviceAccessLevel encryptionDeviceAccessLevel : encryptionDeviceAccessLevels) {
                    if (encryptionDeviceAccessLevel.getClass().isAssignableFrom(DlmsSecuritySupportPerClient.MessageAuthenticationPublic.class)) {
                        match |= true;
                    }
                }
                return match;
            }
        });

        // check for the message encryption and authentication security
        assertThat(dlmsSecuritySupportPerClient.getEncryptionAccessLevels()).has(new Condition<List<EncryptionDeviceAccessLevel>>() {
            @Override
            public boolean matches(List<EncryptionDeviceAccessLevel> encryptionDeviceAccessLevels) {
                boolean match = false;
                for (EncryptionDeviceAccessLevel encryptionDeviceAccessLevel : encryptionDeviceAccessLevels) {
                    if (encryptionDeviceAccessLevel.getClass().isAssignableFrom(DlmsSecuritySupportPerClient.MessageEncryptionAndAuthenticationPublic.class)) {
                        match |= true;
                    }
                }
                return match;
            }
        });
        // check for the no encryption security
        assertThat(dlmsSecuritySupportPerClient.getEncryptionAccessLevels()).has(new Condition<List<EncryptionDeviceAccessLevel>>() {
            @Override
            public boolean matches(List<EncryptionDeviceAccessLevel> encryptionDeviceAccessLevels) {
                boolean match = false;
                for (EncryptionDeviceAccessLevel encryptionDeviceAccessLevel : encryptionDeviceAccessLevels) {
                    if (encryptionDeviceAccessLevel.getClass().isAssignableFrom(DlmsSecuritySupportPerClient.NoEncryptionData.class)) {
                        match |= true;
                    }
                }
                return match;
            }
        });

        // check for the message encryption security
        assertThat(dlmsSecuritySupportPerClient.getEncryptionAccessLevels()).has(new Condition<List<EncryptionDeviceAccessLevel>>() {
            @Override
            public boolean matches(List<EncryptionDeviceAccessLevel> encryptionDeviceAccessLevels) {
                boolean match = false;
                for (EncryptionDeviceAccessLevel encryptionDeviceAccessLevel : encryptionDeviceAccessLevels) {
                    if (encryptionDeviceAccessLevel.getClass().isAssignableFrom(DlmsSecuritySupportPerClient.MessageEncryptionData.class)) {
                        match |= true;
                    }
                }
                return match;
            }
        });

        // check for the message authentication security
        assertThat(dlmsSecuritySupportPerClient.getEncryptionAccessLevels()).has(new Condition<List<EncryptionDeviceAccessLevel>>() {
            @Override
            public boolean matches(List<EncryptionDeviceAccessLevel> encryptionDeviceAccessLevels) {
                boolean match = false;
                for (EncryptionDeviceAccessLevel encryptionDeviceAccessLevel : encryptionDeviceAccessLevels) {
                    if (encryptionDeviceAccessLevel.getClass().isAssignableFrom(DlmsSecuritySupportPerClient.MessageAuthenticationData.class)) {
                        match |= true;
                    }
                }
                return match;
            }
        });

        // check for the message encryption and authentication security
        assertThat(dlmsSecuritySupportPerClient.getEncryptionAccessLevels()).has(new Condition<List<EncryptionDeviceAccessLevel>>() {
            @Override
            public boolean matches(List<EncryptionDeviceAccessLevel> encryptionDeviceAccessLevels) {
                boolean match = false;
                for (EncryptionDeviceAccessLevel encryptionDeviceAccessLevel : encryptionDeviceAccessLevels) {
                    if (encryptionDeviceAccessLevel.getClass().isAssignableFrom(DlmsSecuritySupportPerClient.MessageEncryptionAndAuthenticationData.class)) {
                        match |= true;
                    }
                }
                return match;
            }
        });
        // check for the no encryption security
        assertThat(dlmsSecuritySupportPerClient.getEncryptionAccessLevels()).has(new Condition<List<EncryptionDeviceAccessLevel>>() {
            @Override
            public boolean matches(List<EncryptionDeviceAccessLevel> encryptionDeviceAccessLevels) {
                boolean match = false;
                for (EncryptionDeviceAccessLevel encryptionDeviceAccessLevel : encryptionDeviceAccessLevels) {
                    if (encryptionDeviceAccessLevel.getClass().isAssignableFrom(DlmsSecuritySupportPerClient.NoEncryptionExtendedData.class)) {
                        match |= true;
                    }
                }
                return match;
            }
        });

        // check for the message encryption security
        assertThat(dlmsSecuritySupportPerClient.getEncryptionAccessLevels()).has(new Condition<List<EncryptionDeviceAccessLevel>>() {
            @Override
            public boolean matches(List<EncryptionDeviceAccessLevel> encryptionDeviceAccessLevels) {
                boolean match = false;
                for (EncryptionDeviceAccessLevel encryptionDeviceAccessLevel : encryptionDeviceAccessLevels) {
                    if (encryptionDeviceAccessLevel.getClass().isAssignableFrom(DlmsSecuritySupportPerClient.MessageEncryptionExtendedData.class)) {
                        match |= true;
                    }
                }
                return match;
            }
        });

        // check for the message authentication security
        assertThat(dlmsSecuritySupportPerClient.getEncryptionAccessLevels()).has(new Condition<List<EncryptionDeviceAccessLevel>>() {
            @Override
            public boolean matches(List<EncryptionDeviceAccessLevel> encryptionDeviceAccessLevels) {
                boolean match = false;
                for (EncryptionDeviceAccessLevel encryptionDeviceAccessLevel : encryptionDeviceAccessLevels) {
                    if (encryptionDeviceAccessLevel.getClass().isAssignableFrom(DlmsSecuritySupportPerClient.MessageAuthenticationExtendedData.class)) {
                        match |= true;
                    }
                }
                return match;
            }
        });

        // check for the message encryption and authentication security
        assertThat(dlmsSecuritySupportPerClient.getEncryptionAccessLevels()).has(new Condition<List<EncryptionDeviceAccessLevel>>() {
            @Override
            public boolean matches(List<EncryptionDeviceAccessLevel> encryptionDeviceAccessLevels) {
                boolean match = false;
                for (EncryptionDeviceAccessLevel encryptionDeviceAccessLevel : encryptionDeviceAccessLevels) {
                    if (encryptionDeviceAccessLevel.getClass().isAssignableFrom(DlmsSecuritySupportPerClient.MessageEncryptionAndAuthenticationExtendedData.class)) {
                        match |= true;
                    }
                }
                return match;
            }
        });
        // check for the no encryption security
        assertThat(dlmsSecuritySupportPerClient.getEncryptionAccessLevels()).has(new Condition<List<EncryptionDeviceAccessLevel>>() {
            @Override
            public boolean matches(List<EncryptionDeviceAccessLevel> encryptionDeviceAccessLevels) {
                boolean match = false;
                for (EncryptionDeviceAccessLevel encryptionDeviceAccessLevel : encryptionDeviceAccessLevels) {
                    if (encryptionDeviceAccessLevel.getClass().isAssignableFrom(DlmsSecuritySupportPerClient.NoEncryptionManagement.class)) {
                        match |= true;
                    }
                }
                return match;
            }
        });

        // check for the message encryption security
        assertThat(dlmsSecuritySupportPerClient.getEncryptionAccessLevels()).has(new Condition<List<EncryptionDeviceAccessLevel>>() {
            @Override
            public boolean matches(List<EncryptionDeviceAccessLevel> encryptionDeviceAccessLevels) {
                boolean match = false;
                for (EncryptionDeviceAccessLevel encryptionDeviceAccessLevel : encryptionDeviceAccessLevels) {
                    if (encryptionDeviceAccessLevel.getClass().isAssignableFrom(DlmsSecuritySupportPerClient.MessageEncryptionManagement.class)) {
                        match |= true;
                    }
                }
                return match;
            }
        });

        // check for the message authentication security
        assertThat(dlmsSecuritySupportPerClient.getEncryptionAccessLevels()).has(new Condition<List<EncryptionDeviceAccessLevel>>() {
            @Override
            public boolean matches(List<EncryptionDeviceAccessLevel> encryptionDeviceAccessLevels) {
                boolean match = false;
                for (EncryptionDeviceAccessLevel encryptionDeviceAccessLevel : encryptionDeviceAccessLevels) {
                    if (encryptionDeviceAccessLevel.getClass().isAssignableFrom(DlmsSecuritySupportPerClient.MessageAuthenticationManagement.class)) {
                        match |= true;
                    }
                }
                return match;
            }
        });

        // check for the message encryption and authentication security
        assertThat(dlmsSecuritySupportPerClient.getEncryptionAccessLevels()).has(new Condition<List<EncryptionDeviceAccessLevel>>() {
            @Override
            public boolean matches(List<EncryptionDeviceAccessLevel> encryptionDeviceAccessLevels) {
                boolean match = false;
                for (EncryptionDeviceAccessLevel encryptionDeviceAccessLevel : encryptionDeviceAccessLevels) {
                    if (encryptionDeviceAccessLevel.getClass().isAssignableFrom(DlmsSecuritySupportPerClient.MessageEncryptionAndAuthenticationManagement.class)) {
                        match |= true;
                    }
                }
                return match;
            }
        });
        // check for the no encryption security
        assertThat(dlmsSecuritySupportPerClient.getEncryptionAccessLevels()).has(new Condition<List<EncryptionDeviceAccessLevel>>() {
            @Override
            public boolean matches(List<EncryptionDeviceAccessLevel> encryptionDeviceAccessLevels) {
                boolean match = false;
                for (EncryptionDeviceAccessLevel encryptionDeviceAccessLevel : encryptionDeviceAccessLevels) {
                    if (encryptionDeviceAccessLevel.getClass().isAssignableFrom(DlmsSecuritySupportPerClient.NoEncryptionFirmware.class)) {
                        match |= true;
                    }
                }
                return match;
            }
        });

        // check for the message encryption security
        assertThat(dlmsSecuritySupportPerClient.getEncryptionAccessLevels()).has(new Condition<List<EncryptionDeviceAccessLevel>>() {
            @Override
            public boolean matches(List<EncryptionDeviceAccessLevel> encryptionDeviceAccessLevels) {
                boolean match = false;
                for (EncryptionDeviceAccessLevel encryptionDeviceAccessLevel : encryptionDeviceAccessLevels) {
                    if (encryptionDeviceAccessLevel.getClass().isAssignableFrom(DlmsSecuritySupportPerClient.MessageEncryptionFirmware.class)) {
                        match |= true;
                    }
                }
                return match;
            }
        });

        // check for the message authentication security
        assertThat(dlmsSecuritySupportPerClient.getEncryptionAccessLevels()).has(new Condition<List<EncryptionDeviceAccessLevel>>() {
            @Override
            public boolean matches(List<EncryptionDeviceAccessLevel> encryptionDeviceAccessLevels) {
                boolean match = false;
                for (EncryptionDeviceAccessLevel encryptionDeviceAccessLevel : encryptionDeviceAccessLevels) {
                    if (encryptionDeviceAccessLevel.getClass().isAssignableFrom(DlmsSecuritySupportPerClient.MessageAuthenticationFirmware.class)) {
                        match |= true;
                    }
                }
                return match;
            }
        });

        // check for the message encryption and authentication security
        assertThat(dlmsSecuritySupportPerClient.getEncryptionAccessLevels()).has(new Condition<List<EncryptionDeviceAccessLevel>>() {
            @Override
            public boolean matches(List<EncryptionDeviceAccessLevel> encryptionDeviceAccessLevels) {
                boolean match = false;
                for (EncryptionDeviceAccessLevel encryptionDeviceAccessLevel : encryptionDeviceAccessLevels) {
                    if (encryptionDeviceAccessLevel.getClass().isAssignableFrom(DlmsSecuritySupportPerClient.MessageEncryptionAndAuthenticationFirmware.class)) {
                        match |= true;
                    }
                }
                return match;
            }
        });
        // check for the no encryption security
        assertThat(dlmsSecuritySupportPerClient.getEncryptionAccessLevels()).has(new Condition<List<EncryptionDeviceAccessLevel>>() {
            @Override
            public boolean matches(List<EncryptionDeviceAccessLevel> encryptionDeviceAccessLevels) {
                boolean match = false;
                for (EncryptionDeviceAccessLevel encryptionDeviceAccessLevel : encryptionDeviceAccessLevels) {
                    if (encryptionDeviceAccessLevel.getClass().isAssignableFrom(DlmsSecuritySupportPerClient.NoEncryptionManufacturer.class)) {
                        match |= true;
                    }
                }
                return match;
            }
        });

        // check for the message encryption security
        assertThat(dlmsSecuritySupportPerClient.getEncryptionAccessLevels()).has(new Condition<List<EncryptionDeviceAccessLevel>>() {
            @Override
            public boolean matches(List<EncryptionDeviceAccessLevel> encryptionDeviceAccessLevels) {
                boolean match = false;
                for (EncryptionDeviceAccessLevel encryptionDeviceAccessLevel : encryptionDeviceAccessLevels) {
                    if (encryptionDeviceAccessLevel.getClass().isAssignableFrom(DlmsSecuritySupportPerClient.MessageEncryptionManufacturer.class)) {
                        match |= true;
                    }
                }
                return match;
            }
        });

        // check for the message authentication security
        assertThat(dlmsSecuritySupportPerClient.getEncryptionAccessLevels()).has(new Condition<List<EncryptionDeviceAccessLevel>>() {
            @Override
            public boolean matches(List<EncryptionDeviceAccessLevel> encryptionDeviceAccessLevels) {
                boolean match = false;
                for (EncryptionDeviceAccessLevel encryptionDeviceAccessLevel : encryptionDeviceAccessLevels) {
                    if (encryptionDeviceAccessLevel.getClass().isAssignableFrom(DlmsSecuritySupportPerClient.MessageAuthenticationManufacturer.class)) {
                        match |= true;
                    }
                }
                return match;
            }
        });

        // check for the message encryption and authentication security
        assertThat(dlmsSecuritySupportPerClient.getEncryptionAccessLevels()).has(new Condition<List<EncryptionDeviceAccessLevel>>() {
            @Override
            public boolean matches(List<EncryptionDeviceAccessLevel> encryptionDeviceAccessLevels) {
                boolean match = false;
                for (EncryptionDeviceAccessLevel encryptionDeviceAccessLevel : encryptionDeviceAccessLevels) {
                    if (encryptionDeviceAccessLevel.getClass().isAssignableFrom(DlmsSecuritySupportPerClient.MessageEncryptionAndAuthenticationManufacturer.class)) {
                        match |= true;
                    }
                }
                return match;
            }
        });
    }

    @Test
    public void testClientMacAddressConversion() {
        DlmsSecuritySupportPerClient dlmsSecuritySupportPerClient = new DlmsSecuritySupportPerClient();
        TypedProperties securityProperties = TypedProperties.empty();
        securityProperties.setProperty(DeviceSecurityProperty.CLIENT_MAC_ADDRESS.getPropertySpec().getName(), "64");

        DeviceProtocolSecurityPropertySet deviceProtocolSecurityPropertySet = dlmsSecuritySupportPerClient.convertFromTypedProperties(securityProperties);
        assertThat(deviceProtocolSecurityPropertySet.getAuthenticationDeviceAccessLevel()).isEqualTo(18);
        assertThat(deviceProtocolSecurityPropertySet.getEncryptionDeviceAccessLevel()).isEqualTo(12);
    }

    @Test
    public void testClientMacAddressConversionWithEmptyProperty() {
        DlmsSecuritySupportPerClient dlmsSecuritySupportPerClient = new DlmsSecuritySupportPerClient();
        TypedProperties securityProperties = TypedProperties.empty();

        DeviceProtocolSecurityPropertySet deviceProtocolSecurityPropertySet = dlmsSecuritySupportPerClient.convertFromTypedProperties(securityProperties);
        assertThat(deviceProtocolSecurityPropertySet.getAuthenticationDeviceAccessLevel()).isEqualTo(0);
        assertThat(deviceProtocolSecurityPropertySet.getEncryptionDeviceAccessLevel()).isEqualTo(0);
    }

    @Test
    public void clientMacAddressConversionWithBigDecimalValueTest() {
        DlmsSecuritySupportPerClient dlmsSecuritySupportPerClient = new DlmsSecuritySupportPerClient();
        TypedProperties securityProperties = TypedProperties.empty();
        securityProperties.setProperty(DeviceSecurityProperty.CLIENT_MAC_ADDRESS.getPropertySpec().getName(), new BigDecimal(80));

        DeviceProtocolSecurityPropertySet deviceProtocolSecurityPropertySet = dlmsSecuritySupportPerClient.convertFromTypedProperties(securityProperties);
        assertThat(deviceProtocolSecurityPropertySet.getAuthenticationDeviceAccessLevel()).isEqualTo(24);
        assertThat(deviceProtocolSecurityPropertySet.getEncryptionDeviceAccessLevel()).isEqualTo(16);
    }

    @Test
    public void testSecuredPropertySetConversionWithDifferentTypedPropertiesInAuthenticationAndEncryption() throws Exception {
        DlmsSecuritySupportPerClient dlmsSecuritySupportPerClient = new DlmsSecuritySupportPerClient();
        TypedProperties securityProperties = TypedProperties.empty();
        securityProperties.setProperty("SecurityLevel", "1:3");
        securityProperties.setProperty(DeviceSecurityProperty.BINARY_PASSWORD.getPropertySpec().getName(), "unused");
        securityProperties.setProperty(DeviceSecurityProperty.CLIENT_MAC_ADDRESS.getPropertySpec().getName(), "32");
        securityProperties.setProperty(SecurityPropertySpecName.ENCRYPTION_KEY_DATA.toString(), "some encryption key");
        securityProperties.setProperty(SecurityPropertySpecName.AUTHENTICATION_KEY_DATA.toString(), "some authentication key");
        securityProperties.setProperty(SecurityPropertySpecName.PASSWORD_DATA.toString(), "some password");
        securityProperties.setProperty(DeviceSecurityProperty.ANSI_C12_USER.getPropertySpec().getName(), "unused");

        DeviceProtocolSecurityPropertySet deviceProtocolSecurityPropertySet = dlmsSecuritySupportPerClient.convertFromTypedProperties(securityProperties);
        assertThat(deviceProtocolSecurityPropertySet.getAuthenticationDeviceAccessLevel()).isEqualTo(DlmsSecuritySupportPerClient.AuthenticationAccessLevelIds.DATA_CLIENT_LOW_LEVEL_AUTHENTICATION.getAccessLevel());
        assertThat(deviceProtocolSecurityPropertySet.getEncryptionDeviceAccessLevel()).isEqualTo(DlmsSecuritySupportPerClient.EncryptionAccessLevelIds.DATA_CLIENT_MESSAGE_ENCRYPTION_AUTHENTICATION.getAccessLevel());
        assertThat(deviceProtocolSecurityPropertySet.getSecurityProperties().size()).isEqualTo(3);
        assertThat(deviceProtocolSecurityPropertySet.getSecurityProperties().getProperty(DeviceSecurityProperty.CLIENT_MAC_ADDRESS.getPropertySpec().getName())).isNull();
        assertThat(deviceProtocolSecurityPropertySet.getSecurityProperties().getProperty(SecurityPropertySpecName.PASSWORD_DATA.toString())).isEqualTo("some password");
        assertThat(deviceProtocolSecurityPropertySet.getSecurityProperties().getProperty(SecurityPropertySpecName.ENCRYPTION_KEY_DATA.toString())).isEqualTo("some encryption key");
        assertThat(deviceProtocolSecurityPropertySet.getSecurityProperties().getProperty(SecurityPropertySpecName.AUTHENTICATION_KEY_DATA.toString())).isEqualTo("some authentication key");
        assertThat(deviceProtocolSecurityPropertySet.getSecurityProperties().getProperty(DeviceSecurityProperty.ANSI_C12_USER.getPropertySpec().getName())).isNull();
        assertThat(deviceProtocolSecurityPropertySet.getSecurityProperties().getProperty(DeviceSecurityProperty.BINARY_PASSWORD.getPropertySpec().getName())).isNull();
    }


    @Test
    public void testPasswordConversion() {
        DlmsSecuritySupportPerClient dlmsSecuritySupportPerClient = new DlmsSecuritySupportPerClient();
        final TypedProperties securityProperties = TypedProperties.empty();
        String passwordValue = "MyPassword";
        Password password = new SimplePassword(passwordValue);
        securityProperties.setProperty(SecurityPropertySpecName.PASSWORD_DATA.toString(), password);
        DeviceProtocolSecurityPropertySet deviceProtocolSecurityPropertySet =
                new DeviceProtocolSecurityPropertySet() {
                    @Override
                    public int getAuthenticationDeviceAccessLevel() {
                        return DlmsSecuritySupportPerClient.AuthenticationAccessLevelIds.DATA_CLIENT_GMAC_AUTHENTICATION.getAccessLevel();
                    }

                    @Override
                    public int getEncryptionDeviceAccessLevel() {
                        return DlmsSecuritySupportPerClient.EncryptionAccessLevelIds.DATA_CLIENT_NO_MESSAGE_ENCRYPTION.getAccessLevel();
                    }

                    @Override
                    public TypedProperties getSecurityProperties() {
                        return securityProperties;
                    }
                };

        // business method
        TypedProperties legacyProperties = TypedProperties.copyOf(dlmsSecuritySupportPerClient.convertToTypedProperties(deviceProtocolSecurityPropertySet));

        assertThat(legacyProperties.getProperty(SecurityPropertySpecName.PASSWORD.toString())).isEqualTo(passwordValue);
    }


    @Test
    public void testConvertTypedPropertiesToSecuritySetMissingSecurityProperty() throws Exception {
        DlmsSecuritySupportPerClient dlmsSecuritySupportPerClient = new DlmsSecuritySupportPerClient();
        TypedProperties securityProperties = TypedProperties.empty();

        DeviceProtocolSecurityPropertySet securityPropertySet = dlmsSecuritySupportPerClient.convertFromTypedProperties(securityProperties);
        assertThat(securityPropertySet.getEncryptionDeviceAccessLevel()).isEqualTo(0);
        assertThat(securityPropertySet.getAuthenticationDeviceAccessLevel()).isEqualTo(0);
    }


    @Test(expected = IllegalArgumentException.class)
    public void testConvertTypedPropertiesToSecuritySetWithSecurityLevelIllegalAuthenticationLevel() throws Exception {
        DlmsSecuritySupportPerClient dlmsSecuritySupportPerClient = new DlmsSecuritySupportPerClient();
        TypedProperties securityProperties = TypedProperties.empty();
        securityProperties.setProperty("SecurityLevel", "1A2:21"); // illegal int value

        DeviceProtocolSecurityPropertySet securityPropertySet = dlmsSecuritySupportPerClient.convertFromTypedProperties(securityProperties);
        securityPropertySet.getAuthenticationDeviceAccessLevel();

    }

    @Test(expected = IllegalArgumentException.class)
    public void testConvertTypedPropertiesToSecuritySetWithSecurityLevelIllegalEncryptionLevel() throws Exception {
        DlmsSecuritySupportPerClient dlmsSecuritySupportPerClient = new DlmsSecuritySupportPerClient();
        TypedProperties securityProperties = TypedProperties.empty();
        securityProperties.setProperty("SecurityLevel", "12:2A1"); // illegal int value

        DeviceProtocolSecurityPropertySet securityPropertySet = dlmsSecuritySupportPerClient.convertFromTypedProperties(securityProperties);
        securityPropertySet.getEncryptionDeviceAccessLevel();
    }

    @Test(expected = IllegalStateException.class)
    public void testConvertTypedPropertiesToSecuritySetWithSecurityLevelIllegalFormat() throws Exception {
        DlmsSecuritySupportPerClient dlmsSecuritySupportPerClient = new DlmsSecuritySupportPerClient();
        TypedProperties securityProperties = TypedProperties.empty();
        securityProperties.setProperty("SecurityLevel", "12;21"); // illegal format

        dlmsSecuritySupportPerClient.convertFromTypedProperties(securityProperties);
    }

    @Test
    public void convertToTypedPropertiesTest() {
        DlmsSecuritySupportPerClient dlmsSecuritySupportPerClient = new DlmsSecuritySupportPerClient();
        final TypedProperties securityProperties = TypedProperties.empty();
        String encryptionKeyValue = "MyEncryptionKey";
        securityProperties.setProperty(SecurityPropertySpecName.ENCRYPTION_KEY_MANAGEMENT.toString(), encryptionKeyValue);
        String authenticationKeyValue = "MyAuthenticationKey";
        securityProperties.setProperty(SecurityPropertySpecName.AUTHENTICATION_KEY_MANAGEMENT.toString(), authenticationKeyValue);
        String passwordValue = "MyPassword";
        securityProperties.setProperty(SecurityPropertySpecName.PASSWORD_MANAGEMENT.toString(), passwordValue);
        DeviceProtocolSecurityPropertySet deviceProtocolSecurityPropertySet =
                new DeviceProtocolSecurityPropertySet() {
                    @Override
                    public int getAuthenticationDeviceAccessLevel() {
                        return DlmsSecuritySupportPerClient.AuthenticationAccessLevelIds.MANAGEMENT_CLIENT_GMAC_AUTHENTICATION.getAccessLevel();
                    }

                    @Override
                    public int getEncryptionDeviceAccessLevel() {
                        return DlmsSecuritySupportPerClient.EncryptionAccessLevelIds.MANAGEMENT_CLIENT_NO_MESSAGE_ENCRYPTION.getAccessLevel();
                    }

                    @Override
                    public TypedProperties getSecurityProperties() {
                        return securityProperties;
                    }
                };

        // business method
        TypedProperties legacyProperties = TypedProperties.copyOf(dlmsSecuritySupportPerClient.convertToTypedProperties(deviceProtocolSecurityPropertySet));

        // asserts
        assertNotNull(legacyProperties);
        assertThat(legacyProperties.getProperty("SecurityLevel")).isEqualTo("5:0");
        assertThat(legacyProperties.getProperty("DataTransportEncryptionKey")).isEqualTo(encryptionKeyValue);
        assertThat(legacyProperties.getProperty("DataTransportAuthenticationKey")).isEqualTo(authenticationKeyValue);
        assertThat(legacyProperties.getProperty("ClientMacAddress")).isEqualTo(64);
        assertThat(legacyProperties.getProperty("Password")).isEqualTo(passwordValue);
    }
}

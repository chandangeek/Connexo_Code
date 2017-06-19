/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.security;

import com.energyict.mdc.upl.TypedProperties;
import com.energyict.mdc.upl.security.DeviceProtocolSecurityPropertySet;

import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * Tests the {@link PasswordWithUserIdentificationSecuritySupport} component
 *
 * @author stijn
 * @since 15.05.17 - 15:58
 */
public class PasswordWithUserIdentificationSecuritySupportTest extends AbstractSecuritySupportTest {

    @Test
    public void testConvertToTypedProperties() throws Exception {
        PasswordWithUserIdentificationSecuritySupport securitySupport = new PasswordWithUserIdentificationSecuritySupport(propertySpecService);
        TypedProperties securityProperties = TypedProperties.empty();
        String passwordValue = "MyPassword";
        String deviceAccessIdentifier = "MyIdentifier";
        securityProperties.setProperty(DeviceSecurityProperty.PASSWORD.getPropertySpec(propertySpecService).getName(), passwordValue);

        DeviceProtocolSecurityPropertySet securityPropertySet = new DeviceProtocolSecurityPropertySetImpl(
                deviceAccessIdentifier,
                PasswordWithUserIdentificationSecuritySupport.STANDARD_AUTH_DEVICE_ACCESS_LEVEL,
                PasswordWithUserIdentificationSecuritySupport.STANDARD_ENCRYPTION_DEVICE_ACCESS_LEVEL,
                0,
                0,
                0,
                securityProperties
        );

        com.energyict.mdc.upl.properties.TypedProperties properties = securitySupport.convertToTypedProperties(securityPropertySet);

        assertThat(properties).isNotNull();
        assertThat(properties.size()).isEqualTo(3);
        assertThat(properties.getProperty(DeviceSecurityProperty.PASSWORD.getPropertySpec(propertySpecService).getName())).isEqualTo(passwordValue);
        assertThat(properties.getProperty(DeviceSecurityProperty.DEVICE_ACCESS_IDENTIFIER.getPropertySpec(propertySpecService).getName()))
                .isEqualTo(deviceAccessIdentifier);
        assertThat(properties.getProperty(PasswordWithUserIdentificationSecuritySupport.LEGACY_DEVICE_ACCESS_IDENTIFIER_PROPERTY))
                .isEqualTo(deviceAccessIdentifier);
    }

    @Test
    public void testConvertFromTypedProperties() throws Exception {
        PasswordWithUserIdentificationSecuritySupport securitySupport = new PasswordWithUserIdentificationSecuritySupport(propertySpecService);
        TypedProperties securityProperties = TypedProperties.empty();
        String passwordValue = "MyPassword";
        String deviceAccessIdentifier = "MyIdentifier";
        securityProperties.setProperty(DeviceSecurityProperty.PASSWORD.getPropertySpec(propertySpecService).getName(), passwordValue);
        securityProperties.setProperty(DeviceSecurityProperty.DEVICE_ACCESS_IDENTIFIER.getPropertySpec(propertySpecService).getName(), deviceAccessIdentifier);
        securityProperties.setProperty(PasswordWithUserIdentificationSecuritySupport.LEGACY_DEVICE_ACCESS_IDENTIFIER_PROPERTY, "otherValue");

        DeviceProtocolSecurityPropertySet securityPropertySet = securitySupport.convertFromTypedProperties(securityProperties);
        assertThat(securityPropertySet).isNotNull();
        assertThat(securityPropertySet.getAuthenticationDeviceAccessLevel()).isEqualTo(PasswordWithUserIdentificationSecuritySupport.STANDARD_AUTH_DEVICE_ACCESS_LEVEL);
        assertThat(securityPropertySet.getEncryptionDeviceAccessLevel()).isEqualTo(PasswordWithUserIdentificationSecuritySupport.STANDARD_ENCRYPTION_DEVICE_ACCESS_LEVEL);
        assertThat(securityPropertySet.getSecurityProperties()).isNotNull();
        assertThat(securityPropertySet.getSecurityProperties().size()).isEqualTo(1);
        assertThat(securityPropertySet.getSecurityProperties().getProperty(DeviceSecurityProperty.PASSWORD.getPropertySpec(propertySpecService).getName())).isEqualTo(passwordValue);
    }

    @Test
    public void testConvertFromLegacyTypedProperties() throws Exception {
        PasswordWithUserIdentificationSecuritySupport securitySupport = new PasswordWithUserIdentificationSecuritySupport(propertySpecService);
        TypedProperties securityProperties = TypedProperties.empty();
        String passwordValue = "MyPassword";
        String deviceAccessIdentifier = "MyIdentifier";
        securityProperties.setProperty(DeviceSecurityProperty.PASSWORD.getPropertySpec(propertySpecService).getName(), passwordValue);
        securityProperties.setProperty(PasswordWithUserIdentificationSecuritySupport.LEGACY_DEVICE_ACCESS_IDENTIFIER_PROPERTY, deviceAccessIdentifier);

        DeviceProtocolSecurityPropertySet securityPropertySet = securitySupport.convertFromTypedProperties(securityProperties);
        assertThat(securityPropertySet).isNotNull();
        assertThat(securityPropertySet.getAuthenticationDeviceAccessLevel()).isEqualTo(PasswordWithUserIdentificationSecuritySupport.STANDARD_AUTH_DEVICE_ACCESS_LEVEL);
        assertThat(securityPropertySet.getEncryptionDeviceAccessLevel()).isEqualTo(PasswordWithUserIdentificationSecuritySupport.STANDARD_ENCRYPTION_DEVICE_ACCESS_LEVEL);
        assertThat(securityPropertySet.getSecurityProperties()).isNotNull();
        assertThat(securityPropertySet.getSecurityProperties().size()).isEqualTo(1);
        assertThat(securityPropertySet.getSecurityProperties().getProperty(DeviceSecurityProperty.PASSWORD.getPropertySpec(propertySpecService).getName())).isEqualTo(passwordValue);
    }
}
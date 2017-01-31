/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.security;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.common.Password;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.device.BaseDevice;
import com.energyict.mdc.protocol.api.legacy.MeterProtocol;
import com.energyict.mdc.protocol.api.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.protocol.api.security.DeviceProtocolSecurityCapabilities;
import com.energyict.mdc.protocol.api.security.DeviceProtocolSecurityPropertySet;
import com.energyict.mdc.protocol.api.security.EncryptionDeviceAccessLevel;
import com.energyict.mdc.protocol.api.security.LegacySecurityPropertyConverter;
import com.energyict.protocols.mdc.services.impl.TranslationKeys;
import com.energyict.protocols.naming.SecurityPropertySpecName;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class PasswordWithUserIdentificationSecuritySupport implements DeviceProtocolSecurityCapabilities, LegacySecurityPropertyConverter {

    private static final int STANDARD_AUTH_DEVICE_ACCESS_LEVEL = 10;
    private static final int STANDARD_ENCRYPTION_DEVICE_ACCESS_LEVEL = 20;

    private final PropertySpecService propertySpecService;
    private final Thesaurus thesaurus;

    @Inject
    public PasswordWithUserIdentificationSecuritySupport(PropertySpecService propertySpecService, Thesaurus thesaurus) {
        super();
        this.propertySpecService = propertySpecService;
        this.thesaurus = thesaurus;
    }

    @Override
    public Optional<CustomPropertySet<BaseDevice, ? extends PersistentDomainExtension<BaseDevice>>> getCustomPropertySet() {
        return Optional.of(new BasicAuthenticationCustomPropertySet(this.thesaurus, this.propertySpecService));
    }

    @Override
    public List<AuthenticationDeviceAccessLevel> getAuthenticationAccessLevels() {
        return Collections.<AuthenticationDeviceAccessLevel>singletonList(new StandardAuthenticationAccessLevel());
    }

    @Override
    public List<EncryptionDeviceAccessLevel> getEncryptionAccessLevels() {
        return Collections.<EncryptionDeviceAccessLevel>singletonList(new StandardEncryptionAccessLevel());
    }

    @Override
    public TypedProperties convertToTypedProperties(DeviceProtocolSecurityPropertySet deviceProtocolSecurityPropertySet) {
        TypedProperties typedProperties = TypedProperties.empty();
        if (deviceProtocolSecurityPropertySet != null) {
            typedProperties.setAllProperties(deviceProtocolSecurityPropertySet.getSecurityProperties());
            // override the password (as it is provided as a Password object instead of a String
            final Object property = deviceProtocolSecurityPropertySet.getSecurityProperties().getProperty(SecurityPropertySpecName.PASSWORD.getKey(), new Password(""));
            if (Password.class.isAssignableFrom(property.getClass())) {
                typedProperties.setProperty(SecurityPropertySpecName.PASSWORD.getKey(), ((Password) property).getValue());
            } else {
                typedProperties.setProperty(SecurityPropertySpecName.PASSWORD.getKey(), property);
            }
        }
        return typedProperties;
    }

    @Override
    public DeviceProtocolSecurityPropertySet convertFromTypedProperties(TypedProperties typedProperties) {
        final TypedProperties securityRelatedTypedProperties = TypedProperties.empty();
        overrideDeviceAccessIdentifierPropertyIfAbsent(typedProperties);

        securityRelatedTypedProperties.setAllProperties(LegacyPropertiesExtractor.getSecurityRelatedProperties(typedProperties, STANDARD_AUTH_DEVICE_ACCESS_LEVEL, getAuthenticationAccessLevels()));
        securityRelatedTypedProperties.setAllProperties(LegacyPropertiesExtractor.getSecurityRelatedProperties(typedProperties, STANDARD_ENCRYPTION_DEVICE_ACCESS_LEVEL, getEncryptionAccessLevels()));
        return new DeviceProtocolSecurityPropertySet() {
            @Override
            public int getAuthenticationDeviceAccessLevel() {
                return STANDARD_AUTH_DEVICE_ACCESS_LEVEL;
            }

            @Override
            public int getEncryptionDeviceAccessLevel() {
                return STANDARD_ENCRYPTION_DEVICE_ACCESS_LEVEL;
            }

            @Override
            public TypedProperties getSecurityProperties() {
                return securityRelatedTypedProperties;
            }
        };
    }

    private void overrideDeviceAccessIdentifierPropertyIfAbsent(TypedProperties typedProperties) {
        Object deviceAccessIdentifier = typedProperties.getProperty(DeviceSecurityProperty.DEVICE_ACCESS_IDENTIFIER.getPropertySpec(propertySpecService, this.thesaurus).getName());
        if (deviceAccessIdentifier == null) {
            deviceAccessIdentifier = typedProperties.getProperty(MeterProtocol.NODEID);
        }
        if (deviceAccessIdentifier != null) {
            typedProperties.setProperty(DeviceSecurityProperty.DEVICE_ACCESS_IDENTIFIER.getPropertySpec(propertySpecService, this.thesaurus).getName(), deviceAccessIdentifier);
        }
    }

    /**
     * Standard authentication level that requires a password and an access identifier
     */
    protected class StandardAuthenticationAccessLevel implements AuthenticationDeviceAccessLevel {

        @Override
        public int getId() {
            return STANDARD_AUTH_DEVICE_ACCESS_LEVEL;
        }

        @Override
        public String getTranslation() {
            return thesaurus.getFormat(TranslationKeys.PASSWORDWITHLEVELSECURITYSUPPORT_ACCESSLEVEL_10).format();
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            return Arrays.asList(
                    BasicAuthenticationSecurityProperties.ActualFields.USER_NAME.propertySpec(propertySpecService, thesaurus),
                    BasicAuthenticationSecurityProperties.ActualFields.PASSWORD.propertySpec(propertySpecService, thesaurus));
        }
    }

    /**
     * Standard encryption level that requires a password and an encryption identifier
     */
    protected class StandardEncryptionAccessLevel implements EncryptionDeviceAccessLevel {


        @Override
        public int getId() {
            return STANDARD_ENCRYPTION_DEVICE_ACCESS_LEVEL;
        }

        @Override
        public String getTranslation() {
            return thesaurus.getFormat(TranslationKeys.PASSWORDWITHLEVELSECURITYSUPPORT_ACCESSLEVEL_20).format();
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            return Arrays.asList(
                    BasicAuthenticationSecurityProperties.ActualFields.USER_NAME.propertySpec(propertySpecService, thesaurus),
                    BasicAuthenticationSecurityProperties.ActualFields.PASSWORD.propertySpec(propertySpecService, thesaurus));
        }
    }

}
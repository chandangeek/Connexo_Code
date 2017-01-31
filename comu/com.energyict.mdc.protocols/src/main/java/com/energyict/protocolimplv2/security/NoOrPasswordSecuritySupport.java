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
import com.energyict.mdc.protocol.api.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.protocol.api.security.DeviceAccessLevel;
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

public class NoOrPasswordSecuritySupport implements DeviceProtocolSecurityCapabilities, LegacySecurityPropertyConverter {

    private final PropertySpecService propertySpecService;
    private final Thesaurus thesaurus;

    @Inject
    public NoOrPasswordSecuritySupport(PropertySpecService propertySpecService, Thesaurus thesaurus) {
        super();
        this.propertySpecService = propertySpecService;
        this.thesaurus = thesaurus;
    }

    protected PropertySpecService getPropertySpecService() {
        return propertySpecService;
    }

    protected Thesaurus getThesaurus() {
        return thesaurus;
    }

    @Override
    public Optional<CustomPropertySet<BaseDevice, ? extends PersistentDomainExtension<BaseDevice>>> getCustomPropertySet() {
        return Optional.of(new NoOrPasswordCustomPropertySet(this.thesaurus, this.propertySpecService));
    }

    @Override
    public List<AuthenticationDeviceAccessLevel> getAuthenticationAccessLevels() {
        return Arrays.asList(new NoAuthenticationAccessLevel(), new StandardAuthenticationAccessLevel());
    }

    @Override
    public List<EncryptionDeviceAccessLevel> getEncryptionAccessLevels() {
        return Collections.emptyList();
    }

    @Override
    public TypedProperties convertToTypedProperties(DeviceProtocolSecurityPropertySet deviceProtocolSecurityPropertySet) {
        TypedProperties typedProperties = TypedProperties.empty();
        if (deviceProtocolSecurityPropertySet != null) {
            typedProperties.setAllProperties(deviceProtocolSecurityPropertySet.getSecurityProperties());
            // override the password (as it is provided as a Password object instead of a String)
            final Object property = deviceProtocolSecurityPropertySet.getSecurityProperties().getProperty(SecurityPropertySpecName.PASSWORD.getKey(), new Password(""));
            if (property instanceof Password) {
                typedProperties.setProperty(SecurityPropertySpecName.PASSWORD.getKey(), ((Password) property).getValue());
            } else {
                typedProperties.setProperty(SecurityPropertySpecName.PASSWORD.getKey(), property);
            }
        }
        return typedProperties;
    }


    @Override
    public DeviceProtocolSecurityPropertySet convertFromTypedProperties(TypedProperties typedProperties) {
        String passwordProperty = typedProperties.getStringProperty(DeviceSecurityProperty.PASSWORD.getPropertySpec(propertySpecService, this.thesaurus).getName());
        final AuthenticationDeviceAccessLevel authenticationDeviceAccessLevel;
        if (passwordProperty == null) {
            authenticationDeviceAccessLevel = new NoAuthenticationAccessLevel();
        }
        else {
            authenticationDeviceAccessLevel = new StandardAuthenticationAccessLevel();
        }

        final TypedProperties securityRelatedTypedProperties = TypedProperties.empty();
        securityRelatedTypedProperties.setAllProperties(LegacyPropertiesExtractor.getSecurityRelatedProperties(typedProperties, authenticationDeviceAccessLevel.getId(), getAuthenticationAccessLevels()));

        return new DeviceProtocolSecurityPropertySet() {
            @Override
            public int getAuthenticationDeviceAccessLevel() {
                return authenticationDeviceAccessLevel.getId();
            }

            @Override
            public int getEncryptionDeviceAccessLevel() {
                return DeviceAccessLevel.NOT_USED_DEVICE_ACCESS_LEVEL_ID;
            }

            @Override
            public TypedProperties getSecurityProperties() {
                return securityRelatedTypedProperties;
            }
        };
    }

    /**
     * No authentication level that requires nothing
     */
    protected class NoAuthenticationAccessLevel implements AuthenticationDeviceAccessLevel {

        @Override
        public int getId() {
            return 0;
        }

        @Override
        public String getTranslation() {
            return thesaurus.getFormat(TranslationKeys.NOORPASSWORDSECURITYSUPPORT_AUTHENTICATIONLEVEL_0).format();
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            return Collections.emptyList();
        }
    }

    /**
     * Standard authentication level that requires a password
     */
    protected class StandardAuthenticationAccessLevel implements AuthenticationDeviceAccessLevel {

        @Override
        public int getId() {
            return 1;
        }

        @Override
        public String getTranslation() {
            return thesaurus.getFormat(TranslationKeys.NOORPASSWORDSECURITYSUPPORT_AUTHENTICATIONLEVEL_1).format();
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            return Collections.singletonList(DeviceSecurityProperty.PASSWORD.getPropertySpec(propertySpecService, thesaurus));
        }
    }

}
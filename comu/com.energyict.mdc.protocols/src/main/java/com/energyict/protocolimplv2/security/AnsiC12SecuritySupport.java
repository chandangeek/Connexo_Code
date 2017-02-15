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

public class AnsiC12SecuritySupport implements DeviceProtocolSecurityCapabilities, LegacySecurityPropertyConverter {

    private static final String SECURITY_LEVEL_PROPERTY_NAME = "SecurityLevel";

    private final PropertySpecService propertySpecService;
    private final Thesaurus thesaurus;

    @Inject
    public AnsiC12SecuritySupport(PropertySpecService propertySpecService, Thesaurus thesaurus) {
        super();
        this.propertySpecService = propertySpecService;
        this.thesaurus = thesaurus;
    }

    PropertySpecService getPropertySpecService() {
        return propertySpecService;
    }

    @Override
    public Optional<CustomPropertySet<BaseDevice, ? extends PersistentDomainExtension<BaseDevice>>> getCustomPropertySet() {
        return Optional.of(new AnsiC12CustomPropertySet(this.thesaurus, this.propertySpecService));
    }

    @Override
    public List<AuthenticationDeviceAccessLevel> getAuthenticationAccessLevels() {
        return Arrays.asList(
                new UnRestrictedAuthentication(),
                new RestrictedAuthentication(),
                new ReadOnlyAuthentication()
        );
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
            // override the password (as it is provided as a Password object instead of a String
            final Object property = deviceProtocolSecurityPropertySet.getSecurityProperties().getProperty(SecurityPropertySpecName.PASSWORD.getKey(), new Password(""));
            if (Password.class.isAssignableFrom(property.getClass())) {
                typedProperties.setProperty(SecurityPropertySpecName.PASSWORD.getKey(), ((Password) property).getValue());
            } else {
                typedProperties.setProperty(SecurityPropertySpecName.PASSWORD.getKey(), property);
            }
            typedProperties.setProperty(SECURITY_LEVEL_PROPERTY_NAME, String.valueOf(deviceProtocolSecurityPropertySet.getAuthenticationDeviceAccessLevel()));
        }
        return typedProperties;
    }

    @Override
    public DeviceProtocolSecurityPropertySet convertFromTypedProperties(TypedProperties typedProperties) {
        String authenticationDeviceAccessLevelProperty = typedProperties.getStringProperty(SECURITY_LEVEL_PROPERTY_NAME);
        final int authenticationDeviceAccessLevel;
        if (authenticationDeviceAccessLevelProperty != null) {
            authenticationDeviceAccessLevel = Integer.valueOf(authenticationDeviceAccessLevelProperty);
        }
        else {
            authenticationDeviceAccessLevel = new RestrictedAuthentication().getId();
        }

        final TypedProperties securityRelatedTypedProperties = TypedProperties.empty();

        if (authenticationDeviceAccessLevelProperty==null) {
            securityRelatedTypedProperties.setProperty(DeviceSecurityProperty.ANSI_C12_USER.name(), "");
            securityRelatedTypedProperties.setProperty(DeviceSecurityProperty.ANSI_C12_USER_ID.name(), 0);
        } else {
            securityRelatedTypedProperties.setAllProperties(LegacyPropertiesExtractor.getSecurityRelatedProperties(typedProperties, authenticationDeviceAccessLevel, getAuthenticationAccessLevels()));
        }


        return new DeviceProtocolSecurityPropertySet() {
            @Override
            public int getAuthenticationDeviceAccessLevel() {
                return authenticationDeviceAccessLevel;
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
     * UnRestricted ANSI C12 authentication level
     */
    protected class UnRestrictedAuthentication implements AuthenticationDeviceAccessLevel {

        @Override
        public int getId() {
            return 0;
        }

        @Override
        public String getTranslation() {
            return thesaurus.getFormat(TranslationKeys.ANSIC12SECURITYSUPPORT_AUTHENTICATIONLEVEL_0).format();
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            return Arrays.asList(
                    DeviceSecurityProperty.PASSWORD.getPropertySpec(getPropertySpecService(), thesaurus),
                    DeviceSecurityProperty.ANSI_C12_USER.getPropertySpec(getPropertySpecService(), thesaurus),
                    DeviceSecurityProperty.ANSI_C12_USER_ID.getPropertySpec(getPropertySpecService(), thesaurus)
            );
        }
    }

    /**
     * Restricted ANSI C12 authentication level
     */
    protected class RestrictedAuthentication implements AuthenticationDeviceAccessLevel {

        @Override
        public int getId() {
            return 1;
        }

        @Override
        public String getTranslation() {
            return thesaurus.getFormat(TranslationKeys.ANSIC12SECURITYSUPPORT_ENCRYPTIONLEVEL_1).format();
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            return Arrays.asList(
                    DeviceSecurityProperty.PASSWORD.getPropertySpec(getPropertySpecService(), thesaurus),
                    DeviceSecurityProperty.ANSI_C12_USER.getPropertySpec(getPropertySpecService(), thesaurus),
                    DeviceSecurityProperty.ANSI_C12_USER_ID.getPropertySpec(getPropertySpecService(), thesaurus)
            );
        }
    }

    /**
     * Read Only ANSI C12 authentication level
     */
    protected class ReadOnlyAuthentication implements AuthenticationDeviceAccessLevel {

        @Override
        public int getId() {
            return 2;
        }

        @Override
        public String getTranslation() {
            return thesaurus.getFormat(TranslationKeys.ANSIC12SECURITYSUPPORT_ENCRYPTIONLEVEL_2).format();
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            return Arrays.asList(
                    DeviceSecurityProperty.PASSWORD.getPropertySpec(getPropertySpecService(), thesaurus),
                    DeviceSecurityProperty.ANSI_C12_USER.getPropertySpec(getPropertySpecService(), thesaurus),
                    DeviceSecurityProperty.ANSI_C12_USER_ID.getPropertySpec(getPropertySpecService(), thesaurus)
            );
        }
    }

}
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

public class IEC1107SecuritySupport implements DeviceProtocolSecurityCapabilities, LegacySecurityPropertyConverter {

    private static final String SECURITY_LEVEL_PROPERTY_NAME = "SecurityLevel";
    private static final String DEFAULT_SECURITY_LEVEL_VALUE = "1";

    private final PropertySpecService propertySpecService;
    private final Thesaurus thesaurus;

    @Inject
    public IEC1107SecuritySupport(PropertySpecService propertySpecService, Thesaurus thesaurus) {
        super();
        this.propertySpecService = propertySpecService;
        this.thesaurus = thesaurus;
    }

    /**
     * Summarizes the used ID for the AuthenticationLevels.
     */
    private enum AccessLevelIds {
        NO_AUTHENTICATION(0),
        LEVEL_ONE(1),
        LEVEL_TWO(2),
        LEVEL_THREE(3);

        private final int accessLevel;

        AccessLevelIds(int accessLevel) {
            this.accessLevel = accessLevel;
        }

        private int getAccessLevel() {
            return this.accessLevel;
        }

    }

    @Override
    public Optional<CustomPropertySet<BaseDevice, ? extends PersistentDomainExtension<BaseDevice>>> getCustomPropertySet() {
        return Optional.of(new IEC1107CustomPropertySet(this.thesaurus, this.propertySpecService));
    }

    @Override
    public List<AuthenticationDeviceAccessLevel> getAuthenticationAccessLevels() {
        return Arrays.asList(
                new NoAuthentication(),
                new LevelOneAuthentication(),
                new LevelTwoAuthentication(),
                new LevelThreeAuthentication());
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
        String securityLevelProperty = typedProperties.getTypedProperty(SECURITY_LEVEL_PROPERTY_NAME, DEFAULT_SECURITY_LEVEL_VALUE);
        final int authenticationLevel = Integer.valueOf(securityLevelProperty);
        final TypedProperties securityRelatedTypedProperties = TypedProperties.empty();
        securityRelatedTypedProperties.setAllProperties(LegacyPropertiesExtractor.getSecurityRelatedProperties(typedProperties, authenticationLevel, getAuthenticationAccessLevels()));
        return new DeviceProtocolSecurityPropertySet() {
            @Override
            public int getAuthenticationDeviceAccessLevel() {
                return authenticationLevel;
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
     * An authentication level which indicate that no authentication is required
     * for communication with the device.
     */
    protected class NoAuthentication implements AuthenticationDeviceAccessLevel {

        @Override
        public int getId() {
            return AccessLevelIds.NO_AUTHENTICATION.accessLevel;
        }

        @Override
        public String getTranslation() {
            return thesaurus.getFormat(TranslationKeys.IEC1107SECURITYSUPPORT_AUTHENTICATIONLEVEL_0).format();
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            return Collections.emptyList();
        }
    }

    /**
     * Authentication for level 1 security
     */
    protected class LevelOneAuthentication implements AuthenticationDeviceAccessLevel {

        @Override
        public int getId() {
            return AccessLevelIds.LEVEL_ONE.getAccessLevel();
        }

        @Override
        public String getTranslation() {
            return thesaurus.getFormat(TranslationKeys.IEC1107SECURITYSUPPORT_AUTHENTICATIONLEVEL_1).format();
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            return Collections.singletonList(DeviceSecurityProperty.PASSWORD.getPropertySpec(propertySpecService, thesaurus));
        }
    }

    /**
     * Authentication for level 2 security
     */
    protected class LevelTwoAuthentication implements AuthenticationDeviceAccessLevel {

        @Override
        public int getId() {
            return AccessLevelIds.LEVEL_TWO.getAccessLevel();
        }

        @Override
        public String getTranslation() {
            return thesaurus.getFormat(TranslationKeys.IEC1107SECURITYSUPPORT_AUTHENTICATIONLEVEL_2).format();
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            return Collections.singletonList(DeviceSecurityProperty.PASSWORD.getPropertySpec(propertySpecService, thesaurus));
        }
    }

    /**
     * Authentication for level 1 security
     */
    protected class LevelThreeAuthentication implements AuthenticationDeviceAccessLevel {

        @Override
        public int getId() {
            return AccessLevelIds.LEVEL_THREE.getAccessLevel();
        }

        @Override
        public String getTranslation() {
            return thesaurus.getFormat(TranslationKeys.IEC1107SECURITYSUPPORT_AUTHENTICATIONLEVEL_3).format();
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            return Collections.singletonList(DeviceSecurityProperty.PASSWORD.getPropertySpec(propertySpecService, thesaurus));
        }
    }

}
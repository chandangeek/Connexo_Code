/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.security;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.common.Password;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.device.BaseDevice;
import com.energyict.mdc.protocol.api.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.protocol.api.security.DeviceProtocolSecurityCapabilities;
import com.energyict.mdc.protocol.api.security.DeviceProtocolSecurityPropertySet;
import com.energyict.mdc.protocol.api.security.EncryptionDeviceAccessLevel;
import com.energyict.mdc.protocol.api.security.LegacySecurityPropertyConverter;
import com.energyict.mdc.protocol.api.security.SecurityProperty;
import com.energyict.protocols.mdc.services.impl.TranslationKeys;
import com.energyict.protocols.naming.SecurityPropertySpecName;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class Mtu155SecuritySupport implements DeviceProtocolSecurityCapabilities, LegacySecurityPropertyConverter {

    private static final String SECURITY_LEVEL_PROPERTY_NAME = "SecurityLevel";

    private final PropertySpecService propertySpecService;
    private final Thesaurus thesaurus;

    @Inject
    public Mtu155SecuritySupport(PropertySpecService propertySpecService, Thesaurus thesaurus) {
        super();
        this.propertySpecService = propertySpecService;
        this.thesaurus = thesaurus;
    }

    /**
     * Summarizes the used ID for the Encryption- and AuthenticationLevels.
     */
    private enum AccessLevelIds {
        KEYT(0, TranslationKeys.MTU155SECURITYSUPPORT_ENCRYPTIONLEVEL_0),
        KEYC(1,TranslationKeys.MTU155SECURITYSUPPORT_ENCRYPTIONLEVEL_1),
        KEYF(2, TranslationKeys.MTU155SECURITYSUPPORT_ENCRYPTIONLEVEL_2);

        private final int accessLevel;
        private final TranslationKey translationKey;

        AccessLevelIds(int accessLevel, TranslationKey translationKey) {
            this.accessLevel = accessLevel;
            this.translationKey = translationKey;
        }

        int getAccessLevel() {
            return this.accessLevel;
        }

        TranslationKey getTranslationKey() {
            return translationKey;
        }
    }

    @Override
    public Optional<CustomPropertySet<BaseDevice, ? extends PersistentDomainExtension<BaseDevice>>> getCustomPropertySet() {
        return Optional.of(new MTU155CustomPropertySet(this.thesaurus, this.propertySpecService));
    }

    @Override
    public List<AuthenticationDeviceAccessLevel> getAuthenticationAccessLevels() {
        return Collections.<AuthenticationDeviceAccessLevel>singletonList(new SimpleAuthentication());
    }

    @Override
    public List<EncryptionDeviceAccessLevel> getEncryptionAccessLevels() {
        return Arrays.asList(new KeyCEncryption(), new KeyFEncryption(), new KeyTEncryption());
    }

    @Override
    public TypedProperties convertToTypedProperties(DeviceProtocolSecurityPropertySet deviceProtocolSecurityPropertySet) {
        TypedProperties typedProperties = TypedProperties.empty();
        if (deviceProtocolSecurityPropertySet != null) {
            // override the password (as it is provided as a Password object instead of a String
            final Object property = deviceProtocolSecurityPropertySet.getSecurityProperties().getProperty(SecurityPropertySpecName.PASSWORD.getKey(), new Password(""));
            if (Password.class.isAssignableFrom(property.getClass())) {
                typedProperties.setProperty(SecurityPropertySpecName.PASSWORD.getKey(), ((Password) property).getValue());
            } else {
                typedProperties.setProperty(SecurityPropertySpecName.PASSWORD.getKey(), property);
            }
            typedProperties.setProperty(SECURITY_LEVEL_PROPERTY_NAME, String.valueOf(deviceProtocolSecurityPropertySet.getEncryptionDeviceAccessLevel()));
            typedProperties.setProperty("KeyC",
                    deviceProtocolSecurityPropertySet.getSecurityProperties().getProperty(SecurityPropertySpecName.ENCRYPTION_KEY_2.getKey(), ""));
            typedProperties.setProperty("KeyT",
                    deviceProtocolSecurityPropertySet.getSecurityProperties().getProperty(SecurityPropertySpecName.ENCRYPTION_KEY_1.getKey(), ""));
            typedProperties.setProperty("KeyF",
                    deviceProtocolSecurityPropertySet.getSecurityProperties().getProperty(SecurityPropertySpecName.ENCRYPTION_KEY_3.getKey(), ""));
        }
        return typedProperties;
    }

    public TypedProperties convertToTypedProperties(List<SecurityProperty> securityProperties) {
        TypedProperties typedProperties = TypedProperties.empty();
        if (!securityProperties.isEmpty()) {

            TypedProperties typedSecurityProperties = TypedProperties.empty();
            for (SecurityProperty property : securityProperties) {
                typedSecurityProperties.setProperty(property.getName(), property.getValue());
            }

            // override the password (as it is provided as a Password object instead of a String
            final Object property = typedSecurityProperties.getProperty(SecurityPropertySpecName.PASSWORD.getKey(), new Password(""));
            if (Password.class.isAssignableFrom(property.getClass())) {
                typedProperties.setProperty(SecurityPropertySpecName.PASSWORD.getKey(), ((Password) property).getValue());
            } else {
                typedProperties.setProperty(SecurityPropertySpecName.PASSWORD.getKey(), property);
            }

            typedProperties.setProperty(SECURITY_LEVEL_PROPERTY_NAME, String.valueOf(securityProperties.get(0).getEncryptionDeviceAccessLevel().getId()));
            typedProperties.setProperty("KeyC",
                    typedSecurityProperties.getProperty(SecurityPropertySpecName.ENCRYPTION_KEY_2.getKey(), ""));
            typedProperties.setProperty("KeyT",
                    typedSecurityProperties.getProperty(SecurityPropertySpecName.ENCRYPTION_KEY_1.getKey(), ""));
            typedProperties.setProperty("KeyF",
                    typedSecurityProperties.getProperty(SecurityPropertySpecName.ENCRYPTION_KEY_3.getKey(), ""));

        }
        return typedProperties;
    }

    @Override
    public DeviceProtocolSecurityPropertySet convertFromTypedProperties(TypedProperties typedProperties) {
        String securityLevelProperty = typedProperties.getStringProperty(SECURITY_LEVEL_PROPERTY_NAME);
        final int encryptionDeviceAccessLevel;
        if (securityLevelProperty != null) {
            encryptionDeviceAccessLevel = getSecurityLevelIntegerValue(securityLevelProperty);
        }
        else {
            encryptionDeviceAccessLevel = new KeyCEncryption().getId();
        }

        final TypedProperties securityRelatedTypedProperties = TypedProperties.empty();
        securityRelatedTypedProperties.setAllProperties(LegacyPropertiesExtractor.getSecurityRelatedProperties(typedProperties, 0, getAuthenticationAccessLevels()));
        securityRelatedTypedProperties.setAllProperties(LegacyPropertiesExtractor.getSecurityRelatedProperties(typedProperties, encryptionDeviceAccessLevel, getEncryptionAccessLevels()));


        return new DeviceProtocolSecurityPropertySet() {
            @Override
            public int getAuthenticationDeviceAccessLevel() {
                return 0;
            }

            @Override
            public int getEncryptionDeviceAccessLevel() {
                return encryptionDeviceAccessLevel;
            }

            @Override
            public TypedProperties getSecurityProperties() {
                return securityRelatedTypedProperties;
            }
        };
    }

    private Integer getSecurityLevelIntegerValue(String securityLevelProperty) {
        try {
            return Integer.valueOf(securityLevelProperty);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(String.format("SecurityLevel property contains a non-numeric value : %s",
                    securityLevelProperty));
        }
    }

    private PropertySpec getPasswordPropertySpec() {
        return MTU155SecurityProperties.ActualFields.PASSWORD.propertySpec(this.propertySpecService, this.thesaurus);
    }

    private PropertySpec getEncryptionKeyTPropertySpec() {
        return MTU155SecurityProperties.ActualFields.TEMPORARY_ENCRYPTION_KEY.propertySpec(this.propertySpecService, this.thesaurus);
    }

    private PropertySpec getEncryptionKeyCPropertySpec() {
        return MTU155SecurityProperties.ActualFields.SERVICE_ENCRYPTION_KEY.propertySpec(this.propertySpecService, this.thesaurus);
    }

    private PropertySpec getEncryptionKeyFPropertySpec() {
        return MTU155SecurityProperties.ActualFields.FACTORY_ENCRYPTION_KEY.propertySpec(this.propertySpecService, this.thesaurus);
    }

    /**
     * A simple authentication level that requires a single password
     */
    protected class SimpleAuthentication implements AuthenticationDeviceAccessLevel {

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
            return Collections.singletonList(getPasswordPropertySpec());
        }
    }

    protected class KeyCEncryption implements EncryptionDeviceAccessLevel {

        @Override
        public int getId() {
            return AccessLevelIds.KEYC.accessLevel;
        }

        @Override
        public String getTranslation() {
            return thesaurus.getFormat(AccessLevelIds.KEYC.getTranslationKey()).format();
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            return Collections.singletonList(getEncryptionKeyCPropertySpec());
        }
    }

    protected class KeyTEncryption implements EncryptionDeviceAccessLevel {

        @Override
        public int getId() {
            return AccessLevelIds.KEYT.getAccessLevel();
        }

        @Override
        public String getTranslation() {
            return thesaurus.getFormat(AccessLevelIds.KEYT.getTranslationKey()).format();
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            return Collections.singletonList(getEncryptionKeyTPropertySpec());
        }
    }

    protected class KeyFEncryption implements EncryptionDeviceAccessLevel {

        @Override
        public int getId() {
            return AccessLevelIds.KEYF.getAccessLevel();
        }

        @Override
        public String getTranslation() {
            return thesaurus.getFormat(AccessLevelIds.KEYF.getTranslationKey()).format();
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            return Collections.singletonList(getEncryptionKeyFPropertySpec());
        }
    }

}
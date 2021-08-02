package com.energyict.protocolimplv2.umi.ei4;

import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.upl.security.DeviceProtocolSecurityPropertySet;
import com.energyict.mdc.upl.security.EncryptionDeviceAccessLevel;
import com.energyict.mdc.upl.security.LegacyDeviceProtocolSecurityCapabilities;
import com.energyict.mdc.upl.security.LegacySecurityPropertyConverter;

import com.energyict.protocolimplv2.security.AbstractSecuritySupport;
import com.energyict.protocolimplv2.security.LegacyPropertiesExtractor;
import com.energyict.protocolimplv2.umi.security.SecurityScheme;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class EI4UmiSecuritySupport extends AbstractSecuritySupport implements LegacyDeviceProtocolSecurityCapabilities, LegacySecurityPropertyConverter {
    private static final String SECURITY_LEVEL_PROPERTY_NAME = "SecurityLevel";
    private static final String authenticationTranslationKeyConstant = "EI4UmiSecuritySupport.authenticationlevel.";
    private static final String encryptionTranslationKeyConstant = "EI4UmiSecuritySupport.encryptionlevel.";

    public EI4UmiSecuritySupport(PropertySpecService propertySpecService) {
        super(propertySpecService);
    }

    protected PropertySpecService getPropertySpecService() {
        return propertySpecService;
    }

    @Override
    public List<PropertySpec> getSecurityProperties() {
        return new ArrayList<>();
    }

    @Override
    public List<AuthenticationDeviceAccessLevel> getAuthenticationAccessLevels() {
        return Arrays.asList(new HighLevelAuthentication());
    }

    @Override
    public List<EncryptionDeviceAccessLevel> getEncryptionAccessLevels() {
        return Arrays.asList(new MessageEncryption());
    }

    @Override
    public Optional<PropertySpec> getClientSecurityPropertySpec() {
        return Optional.empty();
    }

    @Override
    public List<String> getLegacySecurityProperties() {
        return Arrays.asList(SECURITY_LEVEL_PROPERTY_NAME);
    }

    @Override
    public com.energyict.mdc.upl.TypedProperties convertToTypedProperties(DeviceProtocolSecurityPropertySet deviceProtocolSecurityPropertySet) {
        com.energyict.mdc.upl.TypedProperties typedProperties = com.energyict.mdc.upl.TypedProperties.empty();
        if (deviceProtocolSecurityPropertySet != null) {
            typedProperties.setAllProperties(deviceProtocolSecurityPropertySet.getSecurityProperties());
            typedProperties.setProperty(SECURITY_LEVEL_PROPERTY_NAME,
                    deviceProtocolSecurityPropertySet.getAuthenticationDeviceAccessLevel() + ":" +
                            deviceProtocolSecurityPropertySet.getEncryptionDeviceAccessLevel());
        }
        return typedProperties;
    }

    @Override
    public DeviceProtocolSecurityPropertySet convertFromTypedProperties(com.energyict.mdc.upl.properties.TypedProperties typedProperties) {
        return this.convertFromTypedProperties(com.energyict.mdc.upl.TypedProperties.copyOf(typedProperties));
    }

    private DeviceProtocolSecurityPropertySet convertFromTypedProperties(final com.energyict.mdc.upl.TypedProperties oldTypedProperties) {
        String securityLevelProperty = oldTypedProperties.getStringProperty(SECURITY_LEVEL_PROPERTY_NAME);
        if (securityLevelProperty == null) {
            securityLevelProperty = "0:0";
        }
        if (!securityLevelProperty.contains(":")) {
            securityLevelProperty += ":0";
        }

        String authLevelStr = securityLevelProperty.substring(0, securityLevelProperty.indexOf(':'));
        String encryptionLevelStr = securityLevelProperty.substring(securityLevelProperty.indexOf(':') + 1);
        final int authenticationLevel = Integer.parseInt(authLevelStr);
        final int encryptionLevel = Integer.parseInt(encryptionLevelStr);

        final com.energyict.mdc.upl.TypedProperties result = com.energyict.mdc.upl.TypedProperties.empty();
        result.setAllProperties(LegacyPropertiesExtractor.getSecurityRelatedProperties(oldTypedProperties,
                authenticationLevel, getAuthenticationAccessLevels()));
        result.setAllProperties(LegacyPropertiesExtractor.getSecurityRelatedProperties(oldTypedProperties,
                encryptionLevel, getEncryptionAccessLevels()));

        return new DeviceProtocolSecurityPropertySet() {
            @Override
            public String getName() {
                return "security";
            }

            @Override
            public Object getClient() {
                return null;
            }

            @Override
            public int getAuthenticationDeviceAccessLevel() {
                return authenticationLevel;
            }

            @Override
            public int getEncryptionDeviceAccessLevel() {
                return encryptionLevel;
            }

            @Override
            public com.energyict.mdc.upl.TypedProperties getSecurityProperties() {
                return result;
            }
        };
    }

    @Override
    public Optional<PropertySpec> getSecurityPropertySpec(String name) {
        for (PropertySpec securityProperty : getSecurityProperties()) {
            if (securityProperty.getName().equals(name)) {
                return Optional.of(securityProperty);
            }
        }
        return Optional.empty();
    }

    protected class HighLevelAuthentication implements AuthenticationDeviceAccessLevel {
        @Override
        public int getId() {
            return SecurityScheme.ASYMMETRIC.getId();
        }

        @Override
        public String getTranslationKey() {
            return authenticationTranslationKeyConstant + getId();
        }

        @Override
        public String getDefaultTranslation() {
            return "High level authentication";
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            return Collections.emptyList();
        }
    }

    protected class MessageEncryption implements EncryptionDeviceAccessLevel {

        @Override
        public int getId() {
            return SecurityScheme.ASYMMETRIC.getId();
        }

        @Override
        public String getTranslationKey() {
            return encryptionTranslationKeyConstant + getId();
        }

        @Override
        public String getDefaultTranslation() {
            return "Message encryption";
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            return Collections.emptyList();
        }
    }
}

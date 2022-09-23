package com.energyict.protocolimplv2.security;

import com.energyict.mdc.upl.TypedProperties;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.upl.security.DeviceAccessLevel;
import com.energyict.mdc.upl.security.DeviceProtocolSecurityPropertySet;
import com.energyict.mdc.upl.security.EncryptionDeviceAccessLevel;
import com.energyict.mdc.upl.security.LegacyDeviceProtocolSecurityCapabilities;
import com.energyict.mdc.upl.security.LegacySecurityPropertyConverter;
import com.energyict.mdc.upl.security.SecurityPropertySpecTranslationKeys;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Provides general security <b>capabilities</b> for a Coap protocol.
 */
public class CoapSecuritySupport extends AbstractSecuritySupport implements LegacyDeviceProtocolSecurityCapabilities, LegacySecurityPropertyConverter {

    private static final String SECURITY_LEVEL_PROPERTY_NAME = "SecurityLevel";
    private static final String ENCRYPTION_KEY_PROPERTY_NAME = "DataTransportEncryptionKey";
    private static final String encryptionTranslationKeyConstant = "DlmsSecuritySupport.encryptionlevel.";

    public CoapSecuritySupport(PropertySpecService propertySpecService) {
        super(propertySpecService);
    }

    protected PropertySpecService getPropertySpecService() {
        return propertySpecService;
    }

    @Override
    public List<PropertySpec> getSecurityProperties() {
        List<PropertySpec> propertySpecs = new ArrayList<>();
        propertySpecs.add(DeviceSecurityProperty.ENCRYPTION_KEY.getPropertySpec(this.propertySpecService));
        return propertySpecs;
    }

    @Override
    public List<AuthenticationDeviceAccessLevel> getAuthenticationAccessLevels() {
        return Collections.emptyList();
    }

    @Override
    public List<EncryptionDeviceAccessLevel> getEncryptionAccessLevels() {
        return Arrays.asList(
                new NoMessageEncryption(),
                new MessageEncryption());
    }

    @Override
    public Optional<PropertySpec> getClientSecurityPropertySpec() {
        return Optional.empty();
    }

    @Override
    public List<String> getLegacySecurityProperties() {
        return Collections.singletonList(SECURITY_LEVEL_PROPERTY_NAME);
    }

    @Override
    public TypedProperties convertToTypedProperties(DeviceProtocolSecurityPropertySet deviceProtocolSecurityPropertySet) {
        TypedProperties typedProperties = TypedProperties.empty();
        if (deviceProtocolSecurityPropertySet != null) {
            typedProperties.setAllProperties(deviceProtocolSecurityPropertySet.getSecurityProperties());
            typedProperties.setProperty(SECURITY_LEVEL_PROPERTY_NAME, String.valueOf(deviceProtocolSecurityPropertySet.getAuthenticationDeviceAccessLevel()));
        }
        return typedProperties;
    }

    @Override
    public DeviceProtocolSecurityPropertySet convertFromTypedProperties(com.energyict.mdc.upl.properties.TypedProperties typedProperties) {
        return this.convertFromTypedProperties(TypedProperties.copyOf(typedProperties));
    }

    protected DeviceProtocolSecurityPropertySet convertFromTypedProperties(TypedProperties typedProperties) {
        String encryptionDeviceAccessLevelProperty = typedProperties.getStringProperty(SECURITY_LEVEL_PROPERTY_NAME);
        final int encryptionDeviceAccessLevel;
        if (encryptionDeviceAccessLevelProperty != null) {
            encryptionDeviceAccessLevel = Integer.valueOf(encryptionDeviceAccessLevelProperty);
        } else {
            encryptionDeviceAccessLevel = new CoapSecuritySupport.MessageEncryption().getId();
        }

        final TypedProperties securityRelatedTypedProperties = TypedProperties.empty();

        if (encryptionDeviceAccessLevelProperty == null) {
            securityRelatedTypedProperties.setProperty(SecurityPropertySpecTranslationKeys.ENCRYPTION_KEY.toString(), "");
        } else {
            securityRelatedTypedProperties.setAllProperties(LegacyPropertiesExtractor.getSecurityRelatedProperties(typedProperties, encryptionDeviceAccessLevel, getEncryptionAccessLevels()));
        }

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
                return DeviceAccessLevel.NOT_USED_DEVICE_ACCESS_LEVEL_ID;
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

    protected String getLegacySecurityLevelDefault() {
        return "0";
    }

    /**
     * Summarizes the used ID for the EncryptionLevels.
     */
    protected enum EncryptionAccessLevelIds {
        NO_MESSAGE_ENCRYPTION(0),
        MESSAGE_ENCRYPTION(1);

        private final int accessLevel;

        EncryptionAccessLevelIds(int accessLevel) {
            this.accessLevel = accessLevel;
        }

        int getAccessLevel() {
            return accessLevel;
        }
    }

    /**
     * An encryption level where no encryption is done, no properties must be set
     */
    protected class NoMessageEncryption implements EncryptionDeviceAccessLevel {

        @Override
        public int getId() {
            return EncryptionAccessLevelIds.NO_MESSAGE_ENCRYPTION.accessLevel;
        }

        @Override
        public String getTranslationKey() {
            return encryptionTranslationKeyConstant + getId();
        }

        @Override
        public String getDefaultTranslation() {
            return "No message encryption";
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            return Collections.emptyList();
        }
    }

    /**
     * An encryption level where all messages are encrypted using
     * the encryption key
     */
    protected class MessageEncryption implements EncryptionDeviceAccessLevel {

        @Override
        public int getId() {
            return EncryptionAccessLevelIds.MESSAGE_ENCRYPTION.accessLevel;
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
            List<PropertySpec> propertySpecs = new ArrayList<>();
            propertySpecs.add(DeviceSecurityProperty.ENCRYPTION_KEY.getPropertySpec(propertySpecService));
            return propertySpecs;
        }
    }
}

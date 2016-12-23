package com.energyict.protocolimplv2.security;

import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.security.DeviceProtocolSecurityPropertySet;
import com.energyict.mdc.upl.security.EncryptionDeviceAccessLevel;

import com.energyict.protocolimpl.properties.TypedProperties;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Provides general security <b>capabilities</b> for an Ansi C12 protocol with
 * <i>extended security functionality</i>
 * <p/>
 * Copyrights EnergyICT
 * Date: 28/01/13
 * Time: 11:30
 */
public class ExtendedAnsiC12SecuritySupport extends AnsiC12SecuritySupport {

    private static final String SECURITY_MODE_LEGACY_PROPERTY = "SecurityMode";
    private final String encryptionTranslationKeyConstant = "AnsiC12SecuritySupport.encryptionlevel.";

    @Override
    public List<PropertySpec> getSecurityProperties() {
        return Arrays.asList(
                DeviceSecurityProperty.PASSWORD.getPropertySpec(),
                DeviceSecurityProperty.BINARY_PASSWORD.getPropertySpec(),
                DeviceSecurityProperty.ANSI_C12_USER.getPropertySpec(),
                DeviceSecurityProperty.ANSI_C12_USER_ID.getPropertySpec(),
                DeviceSecurityProperty.ANSI_CALLED_AP_TITLE.getPropertySpec(),
                DeviceSecurityProperty.ANSI_SECURITY_KEY.getPropertySpec()
        );
    }

    @Override
    public List<String> getLegacySecurityProperties() {
        return Arrays.asList(
                SECURITY_LEVEL_PROPERTY_NAME,
                SECURITY_MODE_LEGACY_PROPERTY
        );
    }

    @Override
    public String getSecurityRelationTypeName() {
        return SecurityRelationTypeName.EXTENDED_ANSI_C12_SECURITY.toString();
    }


    @Override
    public List<EncryptionDeviceAccessLevel> getEncryptionAccessLevels() {
        return Arrays.asList(new NoMessageEncryption(),
                new ClearTextAuthenticationMessageEncryption(),
                new CipherTextAuthenticationMessageEncryption());
    }

    @Override
    public com.energyict.mdc.upl.properties.TypedProperties convertToTypedProperties(DeviceProtocolSecurityPropertySet deviceProtocolSecurityPropertySet) {
        TypedProperties typedProperties = (TypedProperties) super.convertToTypedProperties(deviceProtocolSecurityPropertySet);
        if (deviceProtocolSecurityPropertySet != null) {
            typedProperties.setProperty(SECURITY_MODE_LEGACY_PROPERTY, String.valueOf(deviceProtocolSecurityPropertySet.getEncryptionDeviceAccessLevel()));
        }
        return typedProperties;
    }

    @Override
    protected DeviceProtocolSecurityPropertySet convertFromTypedProperties(TypedProperties typedProperties) {
        final DeviceProtocolSecurityPropertySet deviceProtocolSecurityPropertySet = super.convertFromTypedProperties(typedProperties);
        String encryptionDeviceAccessLevelProperty = typedProperties.getStringProperty("SecurityMode");
        final int encryptionDeviceAccessLevel;
        if (encryptionDeviceAccessLevelProperty != null) {
            encryptionDeviceAccessLevel = Integer.valueOf(encryptionDeviceAccessLevelProperty);
        } else {
            encryptionDeviceAccessLevel = new NoMessageEncryption().getId();
        }
        final TypedProperties securityRelatedTypedProperties = TypedProperties.copyOf(deviceProtocolSecurityPropertySet.getSecurityProperties());
        securityRelatedTypedProperties.setAllProperties(LegacyPropertiesExtractor.getSecurityRelatedProperties(typedProperties, encryptionDeviceAccessLevel, getEncryptionAccessLevels()));

        return new DeviceProtocolSecurityPropertySet() {
            @Override
            public int getAuthenticationDeviceAccessLevel() {
                return deviceProtocolSecurityPropertySet.getAuthenticationDeviceAccessLevel();
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

    /**
     * An encryption level where no encryption is done, no properties must be set
     */
    protected class NoMessageEncryption implements EncryptionDeviceAccessLevel {

        @Override
        public int getId() {
            return 0;
        }

        @Override
        public String getTranslationKey() {
            return encryptionTranslationKeyConstant + getId();
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            return Collections.emptyList();
        }
    }

    /**
     * An encryption level where clear text data is authenticated
     */
    protected class ClearTextAuthenticationMessageEncryption implements EncryptionDeviceAccessLevel {

        @Override
        public int getId() {
            return 1;
        }

        @Override
        public String getTranslationKey() {
            return encryptionTranslationKeyConstant + getId();
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            return Arrays.asList(
                    DeviceSecurityProperty.ANSI_SECURITY_KEY.getPropertySpec(),
                    DeviceSecurityProperty.ANSI_CALLED_AP_TITLE.getPropertySpec());
        }
    }

    /**
     * An encryption level where clear text data is authenticated
     */
    protected class CipherTextAuthenticationMessageEncryption implements EncryptionDeviceAccessLevel {

        @Override
        public int getId() {
            return 2;
        }

        @Override
        public String getTranslationKey() {
            return encryptionTranslationKeyConstant + getId();
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            return Arrays.asList(
                    DeviceSecurityProperty.ANSI_SECURITY_KEY.getPropertySpec(),
                    DeviceSecurityProperty.ANSI_CALLED_AP_TITLE.getPropertySpec());
        }
    }
}

package com.energyict.protocolimplv2.security;

import com.energyict.cpo.PropertySpec;
import com.energyict.cpo.TypedProperties;
import com.energyict.mdc.protocol.security.DeviceProtocolSecurityPropertySet;
import com.energyict.mdc.protocol.security.EncryptionDeviceAccessLevel;
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

    private final String encryptionTranslationKeyConstant = "AnsiC12SecuritySupport.encryptionlevel.";

    @Override
    public List<PropertySpec> getSecurityProperties() {
        return Arrays.asList(
                DeviceSecurityProperty.PASSWORD.getPropertySpec(),
                DeviceSecurityProperty.ANSI_C12_USER.getPropertySpec(),
                DeviceSecurityProperty.ANSI_C12_USER_ID.getPropertySpec(),
                DeviceSecurityProperty.ANSI_CALLED_AP_TITLE.getPropertySpec(),
                DeviceSecurityProperty.BINARY_PASSWORD.getPropertySpec(),
                DeviceSecurityProperty.ENCRYPTION_KEY.getPropertySpec()
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
    public TypedProperties convertToTypedProperties(DeviceProtocolSecurityPropertySet deviceProtocolSecurityPropertySet) {
        TypedProperties typedProperties = super.convertToTypedProperties(deviceProtocolSecurityPropertySet);
        if (deviceProtocolSecurityPropertySet != null) {
            typedProperties.setProperty("SecurityKey", deviceProtocolSecurityPropertySet.getSecurityProperties().getProperty(SecurityPropertySpecName.ENCRYPTION_KEY.toString(), ""));
            typedProperties.setProperty("SecurityMode", String.valueOf(deviceProtocolSecurityPropertySet.getEncryptionDeviceAccessLevel()));
            typedProperties.setProperty("CalledAPTitle", String.valueOf(deviceProtocolSecurityPropertySet.getSecurityProperties().getProperty(SecurityPropertySpecName.ANSI_CALLED_AP_TITLE.toString(), "")));
        }
        return typedProperties;
    }

    @Override
    public DeviceProtocolSecurityPropertySet convertFromTypedProperties(TypedProperties typedProperties) {
        final DeviceProtocolSecurityPropertySet deviceProtocolSecurityPropertySet = super.convertFromTypedProperties(typedProperties);
        String encryptionDeviceAccessLevelProperty = typedProperties.getStringProperty("SecurityKey");
        final int encryptionDeviceAccessLevel=encryptionDeviceAccessLevelProperty!=null?
                Integer.valueOf(encryptionDeviceAccessLevelProperty):
                new NoMessageEncryption().getId();
        final TypedProperties securityRelatedTypedProperties = deviceProtocolSecurityPropertySet.getSecurityProperties();
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
                    DeviceSecurityProperty.ENCRYPTION_KEY.getPropertySpec(),
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
                    DeviceSecurityProperty.ENCRYPTION_KEY.getPropertySpec(),
                    DeviceSecurityProperty.ANSI_CALLED_AP_TITLE.getPropertySpec());
        }
    }
}

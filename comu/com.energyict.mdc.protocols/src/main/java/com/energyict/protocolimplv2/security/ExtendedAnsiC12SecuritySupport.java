package com.energyict.protocolimplv2.security;

import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.common.TypedProperties;
import com.elster.jupiter.properties.PropertySpec;

import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.security.DeviceProtocolSecurityPropertySet;
import com.energyict.mdc.protocol.api.security.EncryptionDeviceAccessLevel;
import com.energyict.protocols.mdc.services.impl.TranslationKeys;

import javax.inject.Inject;
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

    private final Thesaurus thesaurus;

    @Inject
    public ExtendedAnsiC12SecuritySupport(PropertySpecService propertySpecService, Thesaurus thesaurus) {
        super(propertySpecService, thesaurus);
        this.thesaurus = thesaurus;
    }

    @Override
    public List<PropertySpec> getSecurityPropertySpecs() {
        return Arrays.asList(
                DeviceSecurityProperty.PASSWORD.getPropertySpec(this.getPropertySpecService()),
                DeviceSecurityProperty.ANSI_C12_USER.getPropertySpec(this.getPropertySpecService()),
                DeviceSecurityProperty.ANSI_C12_USER_ID.getPropertySpec(this.getPropertySpecService()),
                DeviceSecurityProperty.ANSI_CALLED_AP_TITLE.getPropertySpec(this.getPropertySpecService()),
                DeviceSecurityProperty.BINARY_PASSWORD.getPropertySpec(this.getPropertySpecService()),
                DeviceSecurityProperty.ENCRYPTION_KEY.getPropertySpec(this.getPropertySpecService())
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
        public String getTranslation() {
            return thesaurus.getFormat(TranslationKeys.ANSIC12SECURITYSUPPORT_ENCRYPTIONLEVEL_0).format();
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
        public String getTranslation() {
            return thesaurus.getFormat(TranslationKeys.ANSIC12SECURITYSUPPORT_ENCRYPTIONLEVEL_1).format();
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            return Arrays.asList(
                    DeviceSecurityProperty.ENCRYPTION_KEY.getPropertySpec(getPropertySpecService()),
                    DeviceSecurityProperty.ANSI_CALLED_AP_TITLE.getPropertySpec(getPropertySpecService()));
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
        public String getTranslation() {
            return thesaurus.getFormat(TranslationKeys.ANSIC12SECURITYSUPPORT_ENCRYPTIONLEVEL_2).format();
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            return Arrays.asList(
                    DeviceSecurityProperty.ENCRYPTION_KEY.getPropertySpec(getPropertySpecService()),
                    DeviceSecurityProperty.ANSI_CALLED_AP_TITLE.getPropertySpec(getPropertySpecService()));
        }
    }

}
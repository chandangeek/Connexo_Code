package com.energyict.protocolimplv2.security;

import com.energyict.comserver.adapters.common.LegacySecurityPropertyConverter;
import com.energyict.cpo.PropertySpec;
import com.energyict.cpo.TypedProperties;
import com.energyict.mdc.protocol.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.protocol.security.DeviceProtocolSecurityPropertySet;
import com.energyict.mdc.protocol.security.EncryptionDeviceAccessLevel;
import com.energyict.mdc.protocol.security.DeviceProtocolSecurityCapabilities;

import java.util.Arrays;
import java.util.List;

/**
 * Provides general security <b>capabilities</b> for a DLMS protocol.
 * <p/>
 * Each DLMS protocol can use this setup as a base for providing his
 * capabilities to the HeadEnd.
 * <p/>
 * Copyrights EnergyICT
 * Date: 10/01/13
 * Time: 16:39
 */
public class DlmsSecuritySupport implements DeviceProtocolSecurityCapabilities, LegacySecurityPropertyConverter {

    private final String authenticationTranslationKeyConstant = "DlmsSecuritySupport.authenticationlevel.";
    private final String encryptionTranslationKeyConstant = "DlmsSecuritySupport.encryptionlevel.";

    /**
     * Summarizes the used ID for the Encryption- and AuthenticationLevels.
     */
    protected enum AccessLevelIds {
        NO_AUTHENTICATION(0),
        LOW_LEVEL_AUTHENTICATION(1),
        MANUFACTURER_SPECIFIC_AUTHENTICATION(2),
        MD5_AUTHENTICATION(3),
        SHA1_AUTHENTICATION(4),
        GMAC_AUTHENTICATION(5),
        NO_MESSAGE_ENCRYPTION(0),
        MESSAGE_ENCRYPTION(1),
        MESSAGE_AUTHENTICATION(2),
        MESSAGE_ENCRYPTION_AUTHENTICATION(3);

        private final int accessLevel;

        private AccessLevelIds(int accessLevel) {
            this.accessLevel = accessLevel;
        }

        protected int getAccessLevel() {
            return this.accessLevel;
        }

    }

    @Override
    public List<PropertySpec> getSecurityProperties() {
        return Arrays.asList(
                DeviceSecurityProperty.PASSWORD.getPropertySpec(),
                DeviceSecurityProperty.ENCRYPTION_KEY.getPropertySpec(),
                DeviceSecurityProperty.AUTHENTICATION_KEY.getPropertySpec(),
                DeviceSecurityProperty.CLIENT_MAC_ADDRESS.getPropertySpec()
        );
    }

    @Override
    public String getSecurityRelationTypeName() {
        return SecurityRelationTypeName.DLMS_SECURITY.toString();
    }

    @Override
    public List<AuthenticationDeviceAccessLevel> getAuthenticationAccessLevels() {
        return Arrays.asList(
                new NoAuthentication(),
                new LowLevelAuthentication(),
                new ManufactureAuthentication(),
                new Md5Authentication(),
                new Sha1Authentication(),
                new GmacAuthentication());
    }

    @Override
    public List<EncryptionDeviceAccessLevel> getEncryptionAccessLevels() {
        return Arrays.asList(
                new NoMessageEncryption(),
                new MessageAuthentication(),
                new MessageEncryption(),
                new MessageEncryptionAndAuthentication());
    }

    @Override
    public PropertySpec getSecurityPropertySpec(String name) {
        for (PropertySpec securityProperty : getSecurityProperties()) {
            if (securityProperty.getName().equals(name)) {
                return securityProperty;
            }
        }
        return null;
    }

    @Override
    public TypedProperties convertToTypedProperties(DeviceProtocolSecurityPropertySet deviceProtocolSecurityPropertySet) {
        TypedProperties typedProperties = TypedProperties.empty();
        if(deviceProtocolSecurityPropertySet != null){
            typedProperties.setAllProperties(deviceProtocolSecurityPropertySet.getSecurityProperties());
            // override the password (as it is provided as a Password object instead of a String
            typedProperties.setProperty(SecurityPropertySpecName.PASSWORD.toString(),
                    deviceProtocolSecurityPropertySet.getSecurityProperties().getProperty(SecurityPropertySpecName.PASSWORD.toString(), ""));
            typedProperties.setProperty("SecurityLevel",
                    deviceProtocolSecurityPropertySet.getAuthenticationDeviceAccessLevel() +
                            ":" +
                            deviceProtocolSecurityPropertySet.getEncryptionDeviceAccessLevel());
            typedProperties.setProperty("DataTransportEncryptionKey",
                    deviceProtocolSecurityPropertySet.getSecurityProperties().getProperty(SecurityPropertySpecName.ENCRYPTION_KEY.toString(), ""));
            typedProperties.setProperty("DataTransportAuthenticationKey",
                    deviceProtocolSecurityPropertySet.getSecurityProperties().getProperty(SecurityPropertySpecName.AUTHENTICATION_KEY.toString(), ""));
        }
        return typedProperties;
    }

    /**
     * An encryption level where no encryption is done, no properties must be set
     */
    protected class NoMessageEncryption implements EncryptionDeviceAccessLevel {

        @Override
        public int getId() {
            return AccessLevelIds.NO_MESSAGE_ENCRYPTION.accessLevel;
        }

        @Override
        public String getTranslationKey() {
            return encryptionTranslationKeyConstant + getId();
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            return Arrays.asList(DeviceSecurityProperty.CLIENT_MAC_ADDRESS.getPropertySpec());
        }
    }

    /**
     * An encryption level where all APDU's are encrypted using
     * the encryption key and authentication key
     */
    protected class MessageEncryption implements EncryptionDeviceAccessLevel {

        @Override
        public int getId() {
            return AccessLevelIds.MESSAGE_ENCRYPTION.accessLevel;
        }

        @Override
        public String getTranslationKey() {
            return encryptionTranslationKeyConstant + getId();
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            return Arrays.asList(
                    DeviceSecurityProperty.CLIENT_MAC_ADDRESS.getPropertySpec(),
                    DeviceSecurityProperty.ENCRYPTION_KEY.getPropertySpec(),
                    DeviceSecurityProperty.AUTHENTICATION_KEY.getPropertySpec());
        }
    }

    /**
     * An encryption level where all APDU's are authenticated using
     * the encryption key and authentication key
     */
    protected class MessageAuthentication implements EncryptionDeviceAccessLevel {

        @Override
        public int getId() {
            return AccessLevelIds.MESSAGE_AUTHENTICATION.accessLevel;
        }

        @Override
        public String getTranslationKey() {
            return encryptionTranslationKeyConstant + getId();
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            return Arrays.asList(
                    DeviceSecurityProperty.CLIENT_MAC_ADDRESS.getPropertySpec(),
                    DeviceSecurityProperty.ENCRYPTION_KEY.getPropertySpec(),
                    DeviceSecurityProperty.AUTHENTICATION_KEY.getPropertySpec());
        }
    }

    /**
     * An encryption level where all APDU's are authenticated <b>AND</b>
     * encrypted using the encryption key and authentication key
     */
    protected class MessageEncryptionAndAuthentication implements EncryptionDeviceAccessLevel {

        @Override
        public int getId() {
            return AccessLevelIds.MESSAGE_ENCRYPTION_AUTHENTICATION.accessLevel;
        }

        @Override
        public String getTranslationKey() {
            return encryptionTranslationKeyConstant + getId();
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            return Arrays.asList(
                    DeviceSecurityProperty.CLIENT_MAC_ADDRESS.getPropertySpec(),
                    DeviceSecurityProperty.ENCRYPTION_KEY.getPropertySpec(),
                    DeviceSecurityProperty.AUTHENTICATION_KEY.getPropertySpec());
        }
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
        public String getTranslationKey() {
            return authenticationTranslationKeyConstant + getId();
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            return Arrays.asList(DeviceSecurityProperty.CLIENT_MAC_ADDRESS.getPropertySpec());
        }
    }

    /**
     * An authentication level which indicates that a plain text password
     * can be used to authenticate ourselves with the device.
     */
    protected class LowLevelAuthentication implements AuthenticationDeviceAccessLevel {

        @Override
        public int getId() {
            return AccessLevelIds.LOW_LEVEL_AUTHENTICATION.accessLevel;
        }

        @Override
        public String getTranslationKey() {
            return authenticationTranslationKeyConstant + getId();
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            return Arrays.asList(
                    DeviceSecurityProperty.CLIENT_MAC_ADDRESS.getPropertySpec(),
                    DeviceSecurityProperty.PASSWORD.getPropertySpec());
        }
    }

    /**
     * An authentication level which indicates that a manufacturer specific
     * algorithm has to be used to authenticate with the device
     * <p/>
     * If this level should be used by your protocol, then make sure the provide the necessary properties.
     * As this is a manufacturer specific level, we can not <b>guess</b> what properties will be required.
     */
    protected class ManufactureAuthentication implements AuthenticationDeviceAccessLevel {

        @Override
        public int getId() {
            return AccessLevelIds.MANUFACTURER_SPECIFIC_AUTHENTICATION.accessLevel;
        }

        @Override
        public String getTranslationKey() {
            return authenticationTranslationKeyConstant + getId();
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            return Arrays.asList(DeviceSecurityProperty.CLIENT_MAC_ADDRESS.getPropertySpec());
        }
    }

    /**
     * An authentication level specifying that an MD5 algorithm will be
     * used together with the password to authenticate ourselves with
     * the device.
     */
    protected class Md5Authentication implements AuthenticationDeviceAccessLevel {

        @Override
        public int getId() {
            return AccessLevelIds.MD5_AUTHENTICATION.accessLevel;
        }

        @Override
        public String getTranslationKey() {
            return authenticationTranslationKeyConstant + getId();
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            return Arrays.asList(
                    DeviceSecurityProperty.CLIENT_MAC_ADDRESS.getPropertySpec(),
                    DeviceSecurityProperty.PASSWORD.getPropertySpec());
        }
    }

    /**
     * An authentication level specifying that an SHA1 algorithm will be
     * used together with the password to authenticate ourselves with
     * the device.
     */
    protected class Sha1Authentication implements AuthenticationDeviceAccessLevel {

        @Override
        public int getId() {
            return AccessLevelIds.SHA1_AUTHENTICATION.accessLevel;
        }

        @Override
        public String getTranslationKey() {
            return authenticationTranslationKeyConstant + getId();
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            return Arrays.asList(
                    DeviceSecurityProperty.CLIENT_MAC_ADDRESS.getPropertySpec(),
                    DeviceSecurityProperty.PASSWORD.getPropertySpec());

        }
    }

    /**
     * An authentication level specifying that an GMAC algorithm will be
     * used together with the specific challenge to authenticate ourselves with
     * the device.
     */
    protected class GmacAuthentication implements AuthenticationDeviceAccessLevel {

        @Override
        public int getId() {
            return AccessLevelIds.GMAC_AUTHENTICATION.accessLevel;
        }

        @Override
        public String getTranslationKey() {
            return authenticationTranslationKeyConstant + getId();
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            return Arrays.asList(
                    DeviceSecurityProperty.CLIENT_MAC_ADDRESS.getPropertySpec(),
                    DeviceSecurityProperty.ENCRYPTION_KEY.getPropertySpec(),
                    DeviceSecurityProperty.AUTHENTICATION_KEY.getPropertySpec());
        }
    }
}
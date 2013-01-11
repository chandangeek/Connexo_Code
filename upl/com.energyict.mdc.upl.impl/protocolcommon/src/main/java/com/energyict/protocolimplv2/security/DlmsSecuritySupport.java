package com.energyict.protocolimplv2.security;

import com.energyict.cpo.PropertySpec;
import com.energyict.mdc.protocol.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.protocol.security.EncryptionDeviceAccessLevel;
import com.energyict.mdc.protocol.tasks.support.DeviceSecuritySupport;

import java.util.Arrays;
import java.util.Collections;
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
public class DlmsSecuritySupport implements DeviceSecuritySupport {

    /**
     * Summarizes the used ID for the Encryption- and AuthenticationLevels.
     * These do not match the DLMS levels, they are just for internal usage
     */
    private enum AccessLevelIds {
        NO_AUTHENTICATION(10),
        LOW_LEVEL_AUTHENTICATION(20),
        MANUFACTURER_SPECIFIC_AUTHENTICATION(30),
        MD5_AUTHENTICATION(40),
        SHA1_AUTHENTICATION(50),
        GMAC_AUTHENTICATION(60),
        NO_MESSAGE_ENCRYPTION(100),
        MESSAGE_ENCRYPTION(200),
        MESSAGE_AUTHENTICATION(300),
        MESSAGE_ENCRYPTION_AUTHENTICATION(400);

        private final int accessLevel;

        private AccessLevelIds(int accessLevel) {
            this.accessLevel = accessLevel;
        }
    }

    @Override
    public List<PropertySpec> getSecurityProperties() {
        return Arrays.asList(
                DeviceSecurityProperty.PASSWORD.getPropertySpec(),
                DeviceSecurityProperty.ENCRYPTION_KEY.getPropertySpec(),
                DeviceSecurityProperty.AUTHENTICATION_KEY.getPropertySpec(),
                DeviceSecurityProperty.CLIENT_IDENTIFIER.getPropertySpec()
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
            return "DlmsSecuritySupport.accesslevel.100";
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            return Collections.emptyList();
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
            return "DlmsSecuritySupport.accesslevel.200";
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            return Arrays.asList(
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
            return "DlmsSecuritySupport.accesslevel.300";
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            return Arrays.asList(
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
            return "DlmsSecuritySupport.accesslevel.400";
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            return Arrays.asList(
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
            return "DlmsSecuritySupport.accesslevel.10";
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            return Collections.emptyList();
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
            return "DlmsSecuritySupport.accesslevel.20";
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            return Arrays.asList(DeviceSecurityProperty.PASSWORD.getPropertySpec());
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
            return "DlmsSecuritySupport.accesslevel.30";
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            return Collections.emptyList();
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
            return "DlmsSecuritySupport.accesslevel.40";
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            return Arrays.asList(DeviceSecurityProperty.PASSWORD.getPropertySpec());
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
            return "DlmsSecuritySupport.accesslevel.50";
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            return Arrays.asList(DeviceSecurityProperty.PASSWORD.getPropertySpec());

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
            return "DlmsSecuritySupport.accesslevel.60";
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            return Arrays.asList(
                    DeviceSecurityProperty.ENCRYPTION_KEY.getPropertySpec(),
                    DeviceSecurityProperty.AUTHENTICATION_KEY.getPropertySpec());

        }
    }
}

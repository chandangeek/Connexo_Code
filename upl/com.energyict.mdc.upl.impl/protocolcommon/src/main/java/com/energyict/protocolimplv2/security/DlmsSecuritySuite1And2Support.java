package com.energyict.protocolimplv2.security;

import com.energyict.cpo.PropertySpec;
import com.energyict.mdc.protocol.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.protocol.security.DeviceProtocolSecurityCapabilities;
import com.energyict.mdc.protocol.security.EncryptionDeviceAccessLevel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 28/01/2016 - 17:39
 */
public class DlmsSecuritySuite1And2Support implements DeviceProtocolSecurityCapabilities {

    private static final String authenticationTranslationKeyConstant = "DlmsSecuritySupport.authenticationlevel.";
    private static final String encryptionTranslationKeyConstant = "DlmsSecuritySupport.encryptionlevel.";

    @Override
    public List<PropertySpec> getSecurityProperties() {
        List<PropertySpec> propertySpecs = new ArrayList<>();
        propertySpecs.add(DeviceSecurityProperty.PASSWORD.getPropertySpec());
        propertySpecs.add(DeviceSecurityProperty.ENCRYPTION_KEY.getPropertySpec());
        propertySpecs.add(DeviceSecurityProperty.AUTHENTICATION_KEY.getPropertySpec());
        propertySpecs.add(DeviceSecurityProperty.SERVER_SIGNATURE_CERTIFICATE.getPropertySpec());
        propertySpecs.add(DeviceSecurityProperty.SERVER_KEY_AGREEMENT_CERTIFICATE.getPropertySpec());
        propertySpecs.add(getClientMacAddressPropertySpec());
        return propertySpecs;
    }

    @Override
    public String getSecurityRelationTypeName() {
        return SecurityRelationTypeName.DLMS_SUITE_1_AND_2_SECURITY.toString();
    }

    @Override
    public List<AuthenticationDeviceAccessLevel> getAuthenticationAccessLevels() {
        DlmsSecuritySupport dlmsSecuritySupport = new DlmsSecuritySupport();

        return Arrays.asList(
                dlmsSecuritySupport.new NoAuthentication(),
                dlmsSecuritySupport.new LowLevelAuthentication(),
                dlmsSecuritySupport.new Md5Authentication(),
                dlmsSecuritySupport.new Sha1Authentication(),
                dlmsSecuritySupport.new GmacAuthentication(),
                new Sha256Authentication(),
                new ECDSAAuthentication());
    }

    @Override
    public List<EncryptionDeviceAccessLevel> getEncryptionAccessLevels() {
        DlmsSecuritySupport dlmsSecuritySupport = new DlmsSecuritySupport();
        return Arrays.asList(
                dlmsSecuritySupport.new NoMessageEncryption(),
                dlmsSecuritySupport.new MessageAuthentication(),
                dlmsSecuritySupport.new MessageEncryption(),
                dlmsSecuritySupport.new MessageEncryptionAndAuthentication(),
                //TODO somehow implement the other 62 combinations ==> Rob??
                new MessageEncryptionAndAuthenticationAndSigning());
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

    protected PropertySpec getClientMacAddressPropertySpec() {
        return DeviceSecurityProperty.CLIENT_MAC_ADDRESS.getPropertySpec();
    }

    /**
     * Summarizes the used ID for the AuthenticationLevels.
     */
    protected enum AuthenticationAccessLevelIds {
        NO_AUTHENTICATION(0),
        LOW_LEVEL_AUTHENTICATION(1),
        MANUFACTURER_SPECIFIC_AUTHENTICATION(2),
        MD5_AUTHENTICATION(3),
        SHA1_AUTHENTICATION(4),
        GMAC_AUTHENTICATION(5),
        SHA256_AUTHENTICATION(6),
        ECDSA_AUTHENTICATION(7);

        private final int accessLevel;

        AuthenticationAccessLevelIds(int accessLevel) {
            this.accessLevel = accessLevel;
        }

        protected int getAccessLevel() {
            return this.accessLevel;
        }

    }

    /**
     * Summarizes the used ID for the EncryptionLevels.
     */
    protected enum EncryptionAccessLevelIds {
        NO_MESSAGE_ENCRYPTION(0),
        MESSAGE_AUTHENTICATION(1),
        MESSAGE_ENCRYPTION(2),
        MESSAGE_ENCRYPTION_AUTHENTICATION(3),
        MESSAGE_ENCRYPTION_AUTHENTICATION_AND_SIGNING(3);   //TODO assign proper numbers !!

        private final int accessLevel;

        EncryptionAccessLevelIds(int accessLevel) {
            this.accessLevel = accessLevel;
        }

        protected int getAccessLevel() {
            return this.accessLevel;
        }
    }

    /**
     * All APDUs (requests and responses) are to be encrypted, authenticated and signed.
     */
    protected class MessageEncryptionAndAuthenticationAndSigning implements EncryptionDeviceAccessLevel {

        @Override
        public int getId() {
            return EncryptionAccessLevelIds.MESSAGE_ENCRYPTION_AUTHENTICATION_AND_SIGNING.accessLevel;
        }

        @Override
        public String getTranslationKey() {
            return encryptionTranslationKeyConstant + getId();
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            List<PropertySpec> propertySpecs = new ArrayList<>();
            propertySpecs.add(getClientMacAddressPropertySpec());
            propertySpecs.add(DeviceSecurityProperty.ENCRYPTION_KEY.getPropertySpec());
            propertySpecs.add(DeviceSecurityProperty.AUTHENTICATION_KEY.getPropertySpec());
            propertySpecs.add(DeviceSecurityProperty.SERVER_SIGNATURE_CERTIFICATE.getPropertySpec());
            propertySpecs.add(DeviceSecurityProperty.SERVER_KEY_AGREEMENT_CERTIFICATE.getPropertySpec());
            return propertySpecs;
        }
    }

    /**
     * An authentication level specifying that a SHA256 hashing algorithm will be
     * used together with the specific challenge to authenticate ourselves with
     * the device.
     */
    protected class Sha256Authentication implements AuthenticationDeviceAccessLevel {

        @Override
        public int getId() {
            return AuthenticationAccessLevelIds.SHA256_AUTHENTICATION.accessLevel;
        }

        @Override
        public String getTranslationKey() {
            return authenticationTranslationKeyConstant + getId();
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            List<PropertySpec> propertySpecs = new ArrayList<>();
            propertySpecs.add(getClientMacAddressPropertySpec());
            propertySpecs.add(DeviceSecurityProperty.PASSWORD.getPropertySpec());
            return propertySpecs;
        }
    }

    /**
     * An authentication level specifying that ECDSA (digital signature) will be used
     * to authenticate ourselves with the device.
     * <p/>
     * For the digital signing, the following general properties are additionally used:
     * - our private signing key
     * - our signing certificate
     * This client private key and client certificate is ours and is the same for every DLMS communication.
     */
    protected class ECDSAAuthentication implements AuthenticationDeviceAccessLevel {

        @Override
        public int getId() {
            return AuthenticationAccessLevelIds.ECDSA_AUTHENTICATION.accessLevel;
        }

        @Override
        public String getTranslationKey() {
            return authenticationTranslationKeyConstant + getId();
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            List<PropertySpec> propertySpecs = new ArrayList<>();
            propertySpecs.add(getClientMacAddressPropertySpec());
            propertySpecs.add(DeviceSecurityProperty.SERVER_SIGNATURE_CERTIFICATE.getPropertySpec());
            return propertySpecs;
        }
    }
}
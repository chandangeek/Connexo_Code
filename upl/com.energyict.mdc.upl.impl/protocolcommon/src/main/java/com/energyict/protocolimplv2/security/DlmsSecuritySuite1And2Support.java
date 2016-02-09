package com.energyict.protocolimplv2.security;

import com.energyict.cpo.PropertySpec;
import com.energyict.mdc.protocol.security.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 28/01/2016 - 17:39
 */
public class DlmsSecuritySuite1And2Support implements AdvancedDeviceProtocolSecurityCapabilities {

    private static final String authenticationTranslationKeyConstant = "DlmsSecuritySupport.authenticationlevel.";
    private static final String SIGNING_KEY_CONSTANT = "DlmsSecuritySuite1And2Support.message.signing.";
    private static final String ENCRYPTION_KEY_CONSTANT = "DlmsSecuritySuite1And2Support.message.encryption.";
    private static final String AUTHENTICATION_KEY_CONSTANT = "DlmsSecuritySuite1And2Support.message.authentication.";
    private static final String SECURITY_SUITE_TRANSLATION_KEY_CONSTANT = "security.suite.";

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

    /**
     * Empty list, this is specified by the security suites
     */
    @Override
    public List<AuthenticationDeviceAccessLevel> getAuthenticationAccessLevels() {
        List<AuthenticationDeviceAccessLevel> authenticationAccessLevels = new ArrayList<>(new DlmsSecuritySupport().getAuthenticationAccessLevels());
        authenticationAccessLevels.add(new Sha256Authentication());
        authenticationAccessLevels.add(new ECDSAAuthentication());
        return authenticationAccessLevels;   //TODO remove this, replace by return new ArrayList<>();
    }

    /**
     * Return an empty list, this is specified by the security suites
     */
    @Override
    public List<EncryptionDeviceAccessLevel> getEncryptionAccessLevels() {
        return new DlmsSecuritySupport().getEncryptionAccessLevels();   //TODO remove this, replace by return new ArrayList<>();
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

    @Override
    public List<SecuritySuite> getSecuritySuites() {
        ArrayList<SecuritySuite> securitySuites = new ArrayList<>();
        securitySuites.add(new SecuritySuite0());
        securitySuites.add(new SecuritySuite1());
        securitySuites.add(new SecuritySuite2());
        return securitySuites;
    }

    private enum SecuritySuitLevelIds {
        SECURITY_SUITE_0(0),
        SECURITY_SUITE_1(1),
        SECURITY_SUITE_2(2);

        private final int id;

        SecuritySuitLevelIds(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }
    }

    /**
     * Summarizes the used ID for the AuthenticationLevels.
     */
    public enum AuthenticationAccessLevelIds {
        SHA256_AUTHENTICATION(6),
        ECDSA_AUTHENTICATION(7);

        private final int accessLevel;

        AuthenticationAccessLevelIds(int accessLevel) {
            this.accessLevel = accessLevel;
        }

        public int getAccessLevel() {
            return this.accessLevel;
        }
    }

    public enum MessageAuthenticationLevels {
        NoMessageAuthenticated(0),
        OnlyRequestsAuthenticated(1),
        OnlyResponsesAuthenticated(2),
        RequestsAndResponsesAuthenticated(3);


        private final int id;

        MessageAuthenticationLevels(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }
    }

    public enum MessageEncryptionLevels {
        NoMessageEncrypted(0),
        OnlyRequestsEncrypted(1),
        OnlyResponsesEncrypted(2),
        RequestsAndResponsesEncrypted(3);


        private final int id;

        MessageEncryptionLevels(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }
    }

    public enum MessageSigningLevels {
        NoMessageSigned(0),
        OnlyRequestsSigned(1),
        OnlyResponsesSigned(2),
        RequestsAndResponsesSigned(3);


        private final int id;

        MessageSigningLevels(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }
    }

    private class SecuritySuite0 implements SecuritySuite {

        @Override
        public int getId() {
            return SecuritySuitLevelIds.SECURITY_SUITE_0.getId();
        }

        @Override
        public String getTranslationKey() {
            return SECURITY_SUITE_TRANSLATION_KEY_CONSTANT + getId();
        }

        @Override
        public List<EncryptionDeviceAccessLevel> getEncryptionAccessLevels() {
            DlmsSecuritySupport dlmsSecuritySupport = new DlmsSecuritySupport();
            return Arrays.asList(
                    dlmsSecuritySupport.new NoMessageEncryption(),
                    dlmsSecuritySupport.new MessageAuthentication(),
                    dlmsSecuritySupport.new MessageEncryption(),
                    dlmsSecuritySupport.new MessageEncryptionAndAuthentication()
            );
        }

        @Override
        public List<AuthenticationDeviceAccessLevel> getAuthenticationAccessLevels() {
            DlmsSecuritySupport dlmsSecuritySupport = new DlmsSecuritySupport();
            return Arrays.asList(
                    dlmsSecuritySupport.new NoAuthentication(),
                    dlmsSecuritySupport.new LowLevelAuthentication(),
                    dlmsSecuritySupport.new Md5Authentication(),
                    dlmsSecuritySupport.new Sha1Authentication(),
                    dlmsSecuritySupport.new GmacAuthentication()
            );
        }

        @Override
        public List<MessageAuthenticationLevel> getMessageAuthenticationLevels() {
            return new ArrayList<>();   //Not used in suite 0
        }

        @Override
        public List<MessageEncryptionLevel> getMessageEncryptionLevels() {
            return new ArrayList<>();   //Not used in suite 0
        }

        @Override
        public List<MessageSigningLevel> getMessageSigningLevels() {
            return new ArrayList<>();   //Not used in suite 0
        }
    }

    private class SecuritySuite1 implements SecuritySuite {

        @Override
        public int getId() {
            return SecuritySuitLevelIds.SECURITY_SUITE_1.getId();
        }

        @Override
        public String getTranslationKey() {
            return SECURITY_SUITE_TRANSLATION_KEY_CONSTANT + getId();
        }

        @Override
        public List<EncryptionDeviceAccessLevel> getEncryptionAccessLevels() {
            return new ArrayList<>();   //Not used in suite 1
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
        public List<MessageAuthenticationLevel> getMessageAuthenticationLevels() {
            ArrayList<MessageAuthenticationLevel> messageAuthenticationLevels = new ArrayList<>();
            messageAuthenticationLevels.add(new NoMessageAuthenticated());
            messageAuthenticationLevels.add(new OnlyRequestsAuthenticated());
            messageAuthenticationLevels.add(new OnlyResponsesAuthenticated());
            messageAuthenticationLevels.add(new RequestsAndResponsesAuthenticated());
            return messageAuthenticationLevels;
        }

        @Override
        public List<MessageEncryptionLevel> getMessageEncryptionLevels() {
            ArrayList<MessageEncryptionLevel> messageEncryptionLevels = new ArrayList<>();
            messageEncryptionLevels.add(new NoMessageEncrypted());
            messageEncryptionLevels.add(new OnlyRequestsEncrypted());
            messageEncryptionLevels.add(new OnlyResponsesEncrypted());
            messageEncryptionLevels.add(new RequestsAndResponsesEncrypted());
            return messageEncryptionLevels;
        }

        @Override
        public List<MessageSigningLevel> getMessageSigningLevels() {
            ArrayList<MessageSigningLevel> messageSigningLevels = new ArrayList<>();
            messageSigningLevels.add(new NoMessageSigned());
            messageSigningLevels.add(new OnlyRequestsSigned());
            messageSigningLevels.add(new OnlyResponsesSigned());
            messageSigningLevels.add(new RequestsAndResponsesSigned());
            return messageSigningLevels;
        }
    }

    /**
     * Suite 2 uses the same levels and properties as suite 1.
     */
    private class SecuritySuite2 extends SecuritySuite1 {

        @Override
        public int getId() {
            return SecuritySuitLevelIds.SECURITY_SUITE_2.getId();
        }

        @Override
        public String getTranslationKey() {
            return SECURITY_SUITE_TRANSLATION_KEY_CONSTANT + getId();
        }
    }

    private class NoMessageAuthenticated implements MessageAuthenticationLevel {

        @Override
        public int getId() {
            return MessageAuthenticationLevels.NoMessageAuthenticated.getId();
        }

        @Override
        public String getTranslationKey() {
            return AUTHENTICATION_KEY_CONSTANT + getId();
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            List<PropertySpec> propertySpecs = new ArrayList<>();
            propertySpecs.add(getClientMacAddressPropertySpec());
            return propertySpecs;
        }
    }

    private class OnlyRequestsAuthenticated implements MessageAuthenticationLevel {

        @Override
        public int getId() {
            return MessageAuthenticationLevels.OnlyRequestsAuthenticated.getId();
        }

        @Override
        public String getTranslationKey() {
            return AUTHENTICATION_KEY_CONSTANT + getId();
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            List<PropertySpec> propertySpecs = new ArrayList<>();
            propertySpecs.add(getClientMacAddressPropertySpec());
            propertySpecs.add(DeviceSecurityProperty.ENCRYPTION_KEY.getPropertySpec());
            propertySpecs.add(DeviceSecurityProperty.AUTHENTICATION_KEY.getPropertySpec());
            return propertySpecs;
        }
    }

    private class OnlyResponsesAuthenticated implements MessageAuthenticationLevel {

        @Override
        public int getId() {
            return MessageAuthenticationLevels.OnlyResponsesAuthenticated.getId();
        }

        @Override
        public String getTranslationKey() {
            return AUTHENTICATION_KEY_CONSTANT + getId();
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            List<PropertySpec> propertySpecs = new ArrayList<>();
            propertySpecs.add(getClientMacAddressPropertySpec());
            propertySpecs.add(DeviceSecurityProperty.ENCRYPTION_KEY.getPropertySpec());
            propertySpecs.add(DeviceSecurityProperty.AUTHENTICATION_KEY.getPropertySpec());
            return propertySpecs;
        }
    }

    private class RequestsAndResponsesAuthenticated implements MessageAuthenticationLevel {

        @Override
        public int getId() {
            return MessageAuthenticationLevels.RequestsAndResponsesAuthenticated.getId();
        }

        @Override
        public String getTranslationKey() {
            return AUTHENTICATION_KEY_CONSTANT + getId();
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            List<PropertySpec> propertySpecs = new ArrayList<>();
            propertySpecs.add(getClientMacAddressPropertySpec());
            propertySpecs.add(DeviceSecurityProperty.ENCRYPTION_KEY.getPropertySpec());
            propertySpecs.add(DeviceSecurityProperty.AUTHENTICATION_KEY.getPropertySpec());
            return propertySpecs;
        }
    }

    private class NoMessageEncrypted implements MessageEncryptionLevel {

        @Override
        public int getId() {
            return MessageEncryptionLevels.NoMessageEncrypted.getId();
        }

        @Override
        public String getTranslationKey() {
            return ENCRYPTION_KEY_CONSTANT + getId();
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            List<PropertySpec> propertySpecs = new ArrayList<>();
            propertySpecs.add(getClientMacAddressPropertySpec());
            return propertySpecs;
        }
    }

    private class OnlyRequestsEncrypted implements MessageEncryptionLevel {

        @Override
        public int getId() {
            return MessageEncryptionLevels.OnlyRequestsEncrypted.getId();
        }

        @Override
        public String getTranslationKey() {
            return ENCRYPTION_KEY_CONSTANT + getId();
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            List<PropertySpec> propertySpecs = new ArrayList<>();
            propertySpecs.add(getClientMacAddressPropertySpec());
            propertySpecs.add(DeviceSecurityProperty.ENCRYPTION_KEY.getPropertySpec());
            propertySpecs.add(DeviceSecurityProperty.AUTHENTICATION_KEY.getPropertySpec());
            propertySpecs.add(DeviceSecurityProperty.SERVER_KEY_AGREEMENT_CERTIFICATE.getPropertySpec());
            return propertySpecs;
        }
    }

    private class OnlyResponsesEncrypted implements MessageEncryptionLevel {

        @Override
        public int getId() {
            return MessageEncryptionLevels.OnlyResponsesEncrypted.getId();
        }

        @Override
        public String getTranslationKey() {
            return ENCRYPTION_KEY_CONSTANT + getId();
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            List<PropertySpec> propertySpecs = new ArrayList<>();
            propertySpecs.add(getClientMacAddressPropertySpec());
            propertySpecs.add(DeviceSecurityProperty.ENCRYPTION_KEY.getPropertySpec());
            propertySpecs.add(DeviceSecurityProperty.AUTHENTICATION_KEY.getPropertySpec());
            propertySpecs.add(DeviceSecurityProperty.SERVER_KEY_AGREEMENT_CERTIFICATE.getPropertySpec());
            return propertySpecs;
        }
    }

    private class RequestsAndResponsesEncrypted implements MessageEncryptionLevel {

        @Override
        public int getId() {
            return MessageEncryptionLevels.RequestsAndResponsesEncrypted.getId();
        }

        @Override
        public String getTranslationKey() {
            return ENCRYPTION_KEY_CONSTANT + getId();
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            List<PropertySpec> propertySpecs = new ArrayList<>();
            propertySpecs.add(getClientMacAddressPropertySpec());
            propertySpecs.add(DeviceSecurityProperty.ENCRYPTION_KEY.getPropertySpec());
            propertySpecs.add(DeviceSecurityProperty.AUTHENTICATION_KEY.getPropertySpec());
            propertySpecs.add(DeviceSecurityProperty.SERVER_KEY_AGREEMENT_CERTIFICATE.getPropertySpec());
            return propertySpecs;
        }
    }

    private class NoMessageSigned implements MessageSigningLevel {

        @Override
        public int getId() {
            return MessageSigningLevels.NoMessageSigned.getId();
        }

        @Override
        public String getTranslationKey() {
            return SIGNING_KEY_CONSTANT + getId();
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            List<PropertySpec> propertySpecs = new ArrayList<>();
            propertySpecs.add(getClientMacAddressPropertySpec());
            return propertySpecs;
        }
    }

    private class OnlyRequestsSigned implements MessageSigningLevel {

        @Override
        public int getId() {
            return MessageSigningLevels.OnlyRequestsSigned.getId();
        }

        @Override
        public String getTranslationKey() {
            return SIGNING_KEY_CONSTANT + getId();
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            List<PropertySpec> propertySpecs = new ArrayList<>();
            propertySpecs.add(getClientMacAddressPropertySpec());
            propertySpecs.add(DeviceSecurityProperty.SERVER_SIGNATURE_CERTIFICATE.getPropertySpec());
            return propertySpecs;
        }
    }

    private class OnlyResponsesSigned implements MessageSigningLevel {

        @Override
        public int getId() {
            return MessageSigningLevels.OnlyResponsesSigned.getId();
        }

        @Override
        public String getTranslationKey() {
            return SIGNING_KEY_CONSTANT + getId();
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            List<PropertySpec> propertySpecs = new ArrayList<>();
            propertySpecs.add(getClientMacAddressPropertySpec());
            propertySpecs.add(DeviceSecurityProperty.SERVER_SIGNATURE_CERTIFICATE.getPropertySpec());
            return propertySpecs;
        }
    }

    private class RequestsAndResponsesSigned implements MessageSigningLevel {

        @Override
        public int getId() {
            return MessageSigningLevels.RequestsAndResponsesSigned.getId();
        }

        @Override
        public String getTranslationKey() {
            return SIGNING_KEY_CONSTANT + getId();
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            List<PropertySpec> propertySpecs = new ArrayList<>();
            propertySpecs.add(getClientMacAddressPropertySpec());
            propertySpecs.add(DeviceSecurityProperty.SERVER_SIGNATURE_CERTIFICATE.getPropertySpec());
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
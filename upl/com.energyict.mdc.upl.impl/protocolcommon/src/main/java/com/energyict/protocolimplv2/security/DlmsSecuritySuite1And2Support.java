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
    private static final String REQUEST_SECURITY_TRANSLATION_CONSTANT = "DlmsSecuritySuite1And2Support.request.security.";
    private static final String RESPONSE_SECURITY_TRANSLATION_CONSTANT = "DlmsSecuritySuite1And2Support.responses.security.";
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
        return new ArrayList<>();
    }

    /**
     * Return an empty list, this is specified by the security suites
     */
    @Override
    public List<EncryptionDeviceAccessLevel> getEncryptionAccessLevels() {
        return new ArrayList<>();
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

    public enum RequestSecurityLevels {
        NoSecurityForRequests(0),
        RequestsAuthenticated(1),
        RequestsEncrypted(2),
        RequestsEncryptedAndAuthenticated(3),
        RequestsSigned(4),
        RequestsAuthenticatedAndSigned(5),
        RequestsEncryptedAndSigned(6),
        RequestsEncryptedAndAuthenticatedAndSigned(7);

        private final int id;

        RequestSecurityLevels(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }
    }

    public enum ResponseSecurityLevels {
        NoSecurityForResponses(0),
        ResponsesAuthenticated(1),
        ResponsesEncrypted(2),
        ResponsesEncryptedAndAuthenticated(3),
        ResponsesSigned(4),
        ResponsesAuthenticatedAndSigned(5),
        ResponsesEncryptedAndSigned(6),
        ResponsesEncryptedAndAuthenticatedAndSigned(7);

        private final int id;

        ResponseSecurityLevels(int id) {
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
        public List<RequestSecurityLevel> getRequestSecurityLevels() {
            return new ArrayList<>();   //Not used in suite 0
        }

        @Override
        public List<ResponseSecurityLevel> getResponseSecurityLevels() {
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
        public List<RequestSecurityLevel> getRequestSecurityLevels() {
            ArrayList<RequestSecurityLevel> requestSecurityLevels = new ArrayList<>();
            requestSecurityLevels.add(new NoSecurityForRequests());
            requestSecurityLevels.add(new RequestsAuthenticated());
            requestSecurityLevels.add(new RequestsEncrypted());
            requestSecurityLevels.add(new RequestsEncryptedAndAuthenticated());
            requestSecurityLevels.add(new RequestsSigned());
            requestSecurityLevels.add(new RequestsAuthenticatedAndSigned());
            requestSecurityLevels.add(new RequestsEncryptedAndSigned());
            requestSecurityLevels.add(new RequestsEncryptedAndAuthenticatedAndSigned());
            return requestSecurityLevels;
        }

        @Override
        public List<ResponseSecurityLevel> getResponseSecurityLevels() {
            ArrayList<ResponseSecurityLevel> responseSecurityLevels = new ArrayList<>();
            responseSecurityLevels.add(new NoSecurityForResponses());
            responseSecurityLevels.add(new ResponsesAuthenticated());
            responseSecurityLevels.add(new ResponsesEncrypted());
            responseSecurityLevels.add(new ResponsesEncryptedAndAuthenticated());
            responseSecurityLevels.add(new ResponsesSigned());
            responseSecurityLevels.add(new ResponsesAuthenticatedAndSigned());
            responseSecurityLevels.add(new ResponsesEncryptedAndSigned());
            responseSecurityLevels.add(new ResponsesEncryptedAndAuthenticatedAndSigned());
            return responseSecurityLevels;
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

    private class NoSecurityForRequests implements RequestSecurityLevel {

        @Override
        public int getId() {
            return RequestSecurityLevels.NoSecurityForRequests.getId();
        }

        @Override
        public String getTranslationKey() {
            return REQUEST_SECURITY_TRANSLATION_CONSTANT + getId();
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            List<PropertySpec> propertySpecs = new ArrayList<>();
            propertySpecs.add(getClientMacAddressPropertySpec());
            return propertySpecs;
        }

    }

    private class RequestsAuthenticated implements RequestSecurityLevel {

        @Override
        public int getId() {
            return RequestSecurityLevels.RequestsAuthenticated.getId();
        }

        @Override
        public String getTranslationKey() {
            return REQUEST_SECURITY_TRANSLATION_CONSTANT + getId();
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

    private class RequestsEncrypted implements RequestSecurityLevel {

        @Override
        public int getId() {
            return RequestSecurityLevels.RequestsEncrypted.getId();
        }

        @Override
        public String getTranslationKey() {
            return REQUEST_SECURITY_TRANSLATION_CONSTANT + getId();
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

    private class RequestsEncryptedAndAuthenticated implements RequestSecurityLevel {

        @Override
        public int getId() {
            return RequestSecurityLevels.RequestsEncryptedAndAuthenticated.getId();
        }

        @Override
        public String getTranslationKey() {
            return REQUEST_SECURITY_TRANSLATION_CONSTANT + getId();
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

    private class RequestsSigned implements RequestSecurityLevel {

        @Override
        public int getId() {
            return RequestSecurityLevels.RequestsSigned.getId();
        }

        @Override
        public String getTranslationKey() {
            return REQUEST_SECURITY_TRANSLATION_CONSTANT + getId();
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            List<PropertySpec> propertySpecs = new ArrayList<>();
            propertySpecs.add(getClientMacAddressPropertySpec());
            propertySpecs.add(DeviceSecurityProperty.SERVER_SIGNATURE_CERTIFICATE.getPropertySpec());
            return propertySpecs;
        }
    }

    private class RequestsAuthenticatedAndSigned implements RequestSecurityLevel {

        @Override
        public int getId() {
            return RequestSecurityLevels.RequestsAuthenticatedAndSigned.getId();
        }

        @Override
        public String getTranslationKey() {
            return REQUEST_SECURITY_TRANSLATION_CONSTANT + getId();
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            List<PropertySpec> propertySpecs = new ArrayList<>();
            propertySpecs.add(getClientMacAddressPropertySpec());
            propertySpecs.add(DeviceSecurityProperty.ENCRYPTION_KEY.getPropertySpec());
            propertySpecs.add(DeviceSecurityProperty.AUTHENTICATION_KEY.getPropertySpec());
            propertySpecs.add(DeviceSecurityProperty.SERVER_KEY_AGREEMENT_CERTIFICATE.getPropertySpec());
            propertySpecs.add(DeviceSecurityProperty.SERVER_SIGNATURE_CERTIFICATE.getPropertySpec());
            return propertySpecs;
        }
    }

    private class RequestsEncryptedAndSigned implements RequestSecurityLevel {

        @Override
        public int getId() {
            return RequestSecurityLevels.RequestsEncryptedAndSigned.getId();
        }

        @Override
        public String getTranslationKey() {
            return REQUEST_SECURITY_TRANSLATION_CONSTANT + getId();
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            List<PropertySpec> propertySpecs = new ArrayList<>();
            propertySpecs.add(getClientMacAddressPropertySpec());
            propertySpecs.add(DeviceSecurityProperty.ENCRYPTION_KEY.getPropertySpec());
            propertySpecs.add(DeviceSecurityProperty.AUTHENTICATION_KEY.getPropertySpec());
            propertySpecs.add(DeviceSecurityProperty.SERVER_KEY_AGREEMENT_CERTIFICATE.getPropertySpec());
            propertySpecs.add(DeviceSecurityProperty.SERVER_SIGNATURE_CERTIFICATE.getPropertySpec());
            return propertySpecs;
        }
    }

    private class RequestsEncryptedAndAuthenticatedAndSigned implements RequestSecurityLevel {

        @Override
        public int getId() {
            return RequestSecurityLevels.RequestsEncryptedAndAuthenticatedAndSigned.getId();
        }

        @Override
        public String getTranslationKey() {
            return REQUEST_SECURITY_TRANSLATION_CONSTANT + getId();
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            List<PropertySpec> propertySpecs = new ArrayList<>();
            propertySpecs.add(getClientMacAddressPropertySpec());
            propertySpecs.add(DeviceSecurityProperty.ENCRYPTION_KEY.getPropertySpec());
            propertySpecs.add(DeviceSecurityProperty.AUTHENTICATION_KEY.getPropertySpec());
            propertySpecs.add(DeviceSecurityProperty.SERVER_KEY_AGREEMENT_CERTIFICATE.getPropertySpec());
            propertySpecs.add(DeviceSecurityProperty.SERVER_SIGNATURE_CERTIFICATE.getPropertySpec());
            return propertySpecs;
        }
    }

    private class NoSecurityForResponses implements ResponseSecurityLevel {

        @Override
        public int getId() {
            return ResponseSecurityLevels.NoSecurityForResponses.getId();
        }

        @Override
        public String getTranslationKey() {
            return RESPONSE_SECURITY_TRANSLATION_CONSTANT + getId();
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            List<PropertySpec> propertySpecs = new ArrayList<>();
            propertySpecs.add(getClientMacAddressPropertySpec());
            return propertySpecs;
        }
    }

    private class ResponsesAuthenticated implements ResponseSecurityLevel {

        @Override
        public int getId() {
            return ResponseSecurityLevels.ResponsesAuthenticated.getId();
        }

        @Override
        public String getTranslationKey() {
            return RESPONSE_SECURITY_TRANSLATION_CONSTANT + getId();
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

    private class ResponsesEncrypted implements ResponseSecurityLevel {

        @Override
        public int getId() {
            return ResponseSecurityLevels.ResponsesEncrypted.getId();
        }

        @Override
        public String getTranslationKey() {
            return RESPONSE_SECURITY_TRANSLATION_CONSTANT + getId();
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

    private class ResponsesEncryptedAndAuthenticated implements ResponseSecurityLevel {

        @Override
        public int getId() {
            return ResponseSecurityLevels.ResponsesEncryptedAndAuthenticated.getId();
        }

        @Override
        public String getTranslationKey() {
            return RESPONSE_SECURITY_TRANSLATION_CONSTANT + getId();
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

    private class ResponsesSigned implements ResponseSecurityLevel {

        @Override
        public int getId() {
            return ResponseSecurityLevels.ResponsesSigned.getId();
        }

        @Override
        public String getTranslationKey() {
            return RESPONSE_SECURITY_TRANSLATION_CONSTANT + getId();
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            List<PropertySpec> propertySpecs = new ArrayList<>();
            propertySpecs.add(getClientMacAddressPropertySpec());
            propertySpecs.add(DeviceSecurityProperty.SERVER_SIGNATURE_CERTIFICATE.getPropertySpec());
            return propertySpecs;
        }
    }

    private class ResponsesAuthenticatedAndSigned implements ResponseSecurityLevel {

        @Override
        public int getId() {
            return ResponseSecurityLevels.ResponsesAuthenticatedAndSigned.getId();
        }

        @Override
        public String getTranslationKey() {
            return RESPONSE_SECURITY_TRANSLATION_CONSTANT + getId();
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            List<PropertySpec> propertySpecs = new ArrayList<>();
            propertySpecs.add(getClientMacAddressPropertySpec());
            propertySpecs.add(DeviceSecurityProperty.ENCRYPTION_KEY.getPropertySpec());
            propertySpecs.add(DeviceSecurityProperty.AUTHENTICATION_KEY.getPropertySpec());
            propertySpecs.add(DeviceSecurityProperty.SERVER_KEY_AGREEMENT_CERTIFICATE.getPropertySpec());
            propertySpecs.add(DeviceSecurityProperty.SERVER_SIGNATURE_CERTIFICATE.getPropertySpec());
            return propertySpecs;
        }
    }

    private class ResponsesEncryptedAndSigned implements ResponseSecurityLevel {

        @Override
        public int getId() {
            return ResponseSecurityLevels.ResponsesEncryptedAndSigned.getId();
        }

        @Override
        public String getTranslationKey() {
            return RESPONSE_SECURITY_TRANSLATION_CONSTANT + getId();
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            List<PropertySpec> propertySpecs = new ArrayList<>();
            propertySpecs.add(getClientMacAddressPropertySpec());
            propertySpecs.add(DeviceSecurityProperty.ENCRYPTION_KEY.getPropertySpec());
            propertySpecs.add(DeviceSecurityProperty.AUTHENTICATION_KEY.getPropertySpec());
            propertySpecs.add(DeviceSecurityProperty.SERVER_KEY_AGREEMENT_CERTIFICATE.getPropertySpec());
            propertySpecs.add(DeviceSecurityProperty.SERVER_SIGNATURE_CERTIFICATE.getPropertySpec());
            return propertySpecs;
        }
    }

    private class ResponsesEncryptedAndAuthenticatedAndSigned implements ResponseSecurityLevel {

        @Override
        public int getId() {
            return ResponseSecurityLevels.ResponsesEncryptedAndAuthenticatedAndSigned.getId();
        }

        @Override
        public String getTranslationKey() {
            return RESPONSE_SECURITY_TRANSLATION_CONSTANT + getId();
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            List<PropertySpec> propertySpecs = new ArrayList<>();
            propertySpecs.add(getClientMacAddressPropertySpec());
            propertySpecs.add(DeviceSecurityProperty.ENCRYPTION_KEY.getPropertySpec());
            propertySpecs.add(DeviceSecurityProperty.AUTHENTICATION_KEY.getPropertySpec());
            propertySpecs.add(DeviceSecurityProperty.SERVER_KEY_AGREEMENT_CERTIFICATE.getPropertySpec());
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
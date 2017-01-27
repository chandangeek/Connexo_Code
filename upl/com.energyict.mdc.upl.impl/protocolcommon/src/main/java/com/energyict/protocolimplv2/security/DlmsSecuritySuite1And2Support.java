package com.energyict.protocolimplv2.security;

import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.security.AdvancedDeviceProtocolSecurityCapabilities;
import com.energyict.mdc.upl.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.upl.security.EncryptionDeviceAccessLevel;
import com.energyict.mdc.upl.security.RequestSecurityLevel;
import com.energyict.mdc.upl.security.ResponseSecurityLevel;
import com.energyict.mdc.upl.security.SecuritySuite;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 28/01/2016 - 17:39
 */
public class DlmsSecuritySuite1And2Support extends AbstractSecuritySupport implements AdvancedDeviceProtocolSecurityCapabilities {

    private static final String authenticationTranslationKeyConstant = "DlmsSecuritySupport.authenticationlevel.";
    private static final String REQUEST_SECURITY_TRANSLATION_CONSTANT = "DlmsSecuritySuite1And2Support.request.security.";
    private static final String RESPONSE_SECURITY_TRANSLATION_CONSTANT = "DlmsSecuritySuite1And2Support.responses.security.";
    private static final String SECURITY_SUITE_TRANSLATION_KEY_CONSTANT = "security.suite.";

    public DlmsSecuritySuite1And2Support(PropertySpecService propertySpecService) {
        super(propertySpecService);
    }

    @Override
    public List<PropertySpec> getSecurityProperties() {
        List<PropertySpec> propertySpecs = new ArrayList<>();
        propertySpecs.add(DeviceSecurityProperty.PASSWORD.getPropertySpec(this.propertySpecService));
        propertySpecs.add(DeviceSecurityProperty.ENCRYPTION_KEY.getPropertySpec(this.propertySpecService));
        propertySpecs.add(DeviceSecurityProperty.AUTHENTICATION_KEY.getPropertySpec(this.propertySpecService));
        propertySpecs.add(DeviceSecurityProperty.SERVER_SIGNATURE_CERTIFICATE.getPropertySpec(this.propertySpecService));
        propertySpecs.add(DeviceSecurityProperty.SERVER_KEY_AGREEMENT_CERTIFICATE.getPropertySpec(this.propertySpecService));
        propertySpecs.add(getClientMacAddressPropertySpec());
        return propertySpecs;
    }

    /**
     * A list of all possible authentication levels.
     * Note that not all these levels are supported in all security suites.
     */
    @Override
    public List<AuthenticationDeviceAccessLevel> getAuthenticationAccessLevels() {
        List<AuthenticationDeviceAccessLevel> authenticationAccessLevels = new ArrayList<>(new DlmsSecuritySupport(propertySpecService).getAuthenticationAccessLevels());
        authenticationAccessLevels.add(new Sha256Authentication());
        authenticationAccessLevels.add(new ECDSAAuthentication());
        return authenticationAccessLevels;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<RequestSecurityLevel> getRequestSecurityLevels() {
        return Arrays.asList(
                new NoSecurityForRequests(),
                new RequestsAuthenticated(),
                new RequestsEncrypted(),
                new RequestsEncryptedAndAuthenticated(),
                new RequestsSigned(),
                new RequestsAuthenticatedAndSigned(),
                new RequestsEncryptedAndSigned(),
                new RequestsEncryptedAndAuthenticatedAndSigned());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ResponseSecurityLevel> getResponseSecurityLevels() {
        return Arrays.asList(
                new NoSecurityForResponses(),
                new ResponsesAuthenticated(),
                new ResponsesEncrypted(),
                new ResponsesEncryptedAndAuthenticated(),
                new ResponsesSigned(),
                new ResponsesAuthenticatedAndSigned(),
                new ResponsesEncryptedAndSigned(),
                new ResponsesEncryptedAndAuthenticatedAndSigned());
    }

    /**
     * A list of all possible encryption levels.
     * Note that not all these levels are supported in all security suites.
     */
    @Override
    public List<EncryptionDeviceAccessLevel> getEncryptionAccessLevels() {
        return new DlmsSecuritySupport(propertySpecService).getEncryptionAccessLevels();
    }

    protected PropertySpec getClientMacAddressPropertySpec() {
        return DeviceSecurityProperty.CLIENT_MAC_ADDRESS.getPropertySpec(this.propertySpecService);
    }

    @Override
    public List<SecuritySuite> getSecuritySuites() {
        return Arrays.asList(
                new SecuritySuite0(),
                new SecuritySuite1(),
                new SecuritySuite2());
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
        public String getDefaultTranslation() {
            return "DLMS security suite 0";
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            return new ArrayList<>();   //Not used for security suite implementations
        }

        @Override
        public List<EncryptionDeviceAccessLevel> getEncryptionAccessLevels() {
            DlmsSecuritySupport dlmsSecuritySupport = new DlmsSecuritySupport(propertySpecService);
            return Arrays.asList(
                    dlmsSecuritySupport.new NoMessageEncryption(),
                    dlmsSecuritySupport.new MessageAuthentication(),
                    dlmsSecuritySupport.new MessageEncryption(),
                    dlmsSecuritySupport.new MessageEncryptionAndAuthentication()
            );
        }

        @Override
        public List<AuthenticationDeviceAccessLevel> getAuthenticationAccessLevels() {
            DlmsSecuritySupport dlmsSecuritySupport = new DlmsSecuritySupport(propertySpecService);
            return Arrays.asList(
                    dlmsSecuritySupport.new NoAuthentication(),
                    dlmsSecuritySupport.new LowLevelAuthentication(),
                    dlmsSecuritySupport.new Md5Authentication(),
                    dlmsSecuritySupport.new Sha1Authentication(),
                    dlmsSecuritySupport.new GmacAuthentication(),
                    new Sha256Authentication()
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
        public String getDefaultTranslation() {
            return "DLMS security suite 1";
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            return new ArrayList<>();   //Not used for security suite implementations
        }

        @Override
        public List<EncryptionDeviceAccessLevel> getEncryptionAccessLevels() {
            return new ArrayList<>();   //Not used in suite 1
        }

        @Override
        public List<AuthenticationDeviceAccessLevel> getAuthenticationAccessLevels() {
            DlmsSecuritySupport dlmsSecuritySupport = new DlmsSecuritySupport(propertySpecService);
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
            return DlmsSecuritySuite1And2Support.this.getRequestSecurityLevels();
        }

        @Override
        public List<ResponseSecurityLevel> getResponseSecurityLevels() {
            return DlmsSecuritySuite1And2Support.this.getResponseSecurityLevels();
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
        public String getDefaultTranslation() {
            return "DLMS security suite 2";
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
        public String getDefaultTranslation() {
            return "No security for requests";
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
        public String getDefaultTranslation() {
            return "Requests authenticated";
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            List<PropertySpec> propertySpecs = new ArrayList<>();
            propertySpecs.add(getClientMacAddressPropertySpec());
            propertySpecs.add(DeviceSecurityProperty.ENCRYPTION_KEY.getPropertySpec(propertySpecService));
            propertySpecs.add(DeviceSecurityProperty.AUTHENTICATION_KEY.getPropertySpec(propertySpecService));
            propertySpecs.add(DeviceSecurityProperty.SERVER_SIGNATURE_CERTIFICATE.getPropertySpec(propertySpecService));
            propertySpecs.add(DeviceSecurityProperty.SERVER_KEY_AGREEMENT_CERTIFICATE.getPropertySpec(propertySpecService));
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
        public String getDefaultTranslation() {
            return "Requests encrypted";
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            List<PropertySpec> propertySpecs = new ArrayList<>();
            propertySpecs.add(getClientMacAddressPropertySpec());
            propertySpecs.add(DeviceSecurityProperty.ENCRYPTION_KEY.getPropertySpec(propertySpecService));
            propertySpecs.add(DeviceSecurityProperty.AUTHENTICATION_KEY.getPropertySpec(propertySpecService));
            propertySpecs.add(DeviceSecurityProperty.SERVER_SIGNATURE_CERTIFICATE.getPropertySpec(propertySpecService));
            propertySpecs.add(DeviceSecurityProperty.SERVER_KEY_AGREEMENT_CERTIFICATE.getPropertySpec(propertySpecService));
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
        public String getDefaultTranslation() {
            return "Requests authenticated and encrypted";
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            List<PropertySpec> propertySpecs = new ArrayList<>();
            propertySpecs.add(getClientMacAddressPropertySpec());
            propertySpecs.add(DeviceSecurityProperty.ENCRYPTION_KEY.getPropertySpec(propertySpecService));
            propertySpecs.add(DeviceSecurityProperty.AUTHENTICATION_KEY.getPropertySpec(propertySpecService));
            propertySpecs.add(DeviceSecurityProperty.SERVER_SIGNATURE_CERTIFICATE.getPropertySpec(propertySpecService));
            propertySpecs.add(DeviceSecurityProperty.SERVER_KEY_AGREEMENT_CERTIFICATE.getPropertySpec(propertySpecService));
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
        public String getDefaultTranslation() {
            return "Requests signed";
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            List<PropertySpec> propertySpecs = new ArrayList<>();
            propertySpecs.add(getClientMacAddressPropertySpec());
            propertySpecs.add(DeviceSecurityProperty.SERVER_SIGNATURE_CERTIFICATE.getPropertySpec(propertySpecService));
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
        public String getDefaultTranslation() {
            return "Requests authenticated and signed";
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            List<PropertySpec> propertySpecs = new ArrayList<>();
            propertySpecs.add(getClientMacAddressPropertySpec());
            propertySpecs.add(DeviceSecurityProperty.ENCRYPTION_KEY.getPropertySpec(propertySpecService));
            propertySpecs.add(DeviceSecurityProperty.AUTHENTICATION_KEY.getPropertySpec(propertySpecService));
            propertySpecs.add(DeviceSecurityProperty.SERVER_KEY_AGREEMENT_CERTIFICATE.getPropertySpec(propertySpecService));
            propertySpecs.add(DeviceSecurityProperty.SERVER_SIGNATURE_CERTIFICATE.getPropertySpec(propertySpecService));
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
        public String getDefaultTranslation() {
            return "Requests encrypted and signed";
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            List<PropertySpec> propertySpecs = new ArrayList<>();
            propertySpecs.add(getClientMacAddressPropertySpec());
            propertySpecs.add(DeviceSecurityProperty.ENCRYPTION_KEY.getPropertySpec(propertySpecService));
            propertySpecs.add(DeviceSecurityProperty.AUTHENTICATION_KEY.getPropertySpec(propertySpecService));
            propertySpecs.add(DeviceSecurityProperty.SERVER_KEY_AGREEMENT_CERTIFICATE.getPropertySpec(propertySpecService));
            propertySpecs.add(DeviceSecurityProperty.SERVER_SIGNATURE_CERTIFICATE.getPropertySpec(propertySpecService));
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
        public String getDefaultTranslation() {
            return "Requests authenticated, encrypted and signed";
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            List<PropertySpec> propertySpecs = new ArrayList<>();
            propertySpecs.add(getClientMacAddressPropertySpec());
            propertySpecs.add(DeviceSecurityProperty.ENCRYPTION_KEY.getPropertySpec(propertySpecService));
            propertySpecs.add(DeviceSecurityProperty.AUTHENTICATION_KEY.getPropertySpec(propertySpecService));
            propertySpecs.add(DeviceSecurityProperty.SERVER_KEY_AGREEMENT_CERTIFICATE.getPropertySpec(propertySpecService));
            propertySpecs.add(DeviceSecurityProperty.SERVER_SIGNATURE_CERTIFICATE.getPropertySpec(propertySpecService));
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
        public String getDefaultTranslation() {
            return "No security for responses";
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
        public String getDefaultTranslation() {
            return "Responses authenticated";
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            List<PropertySpec> propertySpecs = new ArrayList<>();
            propertySpecs.add(getClientMacAddressPropertySpec());
            propertySpecs.add(DeviceSecurityProperty.ENCRYPTION_KEY.getPropertySpec(propertySpecService));
            propertySpecs.add(DeviceSecurityProperty.AUTHENTICATION_KEY.getPropertySpec(propertySpecService));
            propertySpecs.add(DeviceSecurityProperty.SERVER_SIGNATURE_CERTIFICATE.getPropertySpec(propertySpecService));
            propertySpecs.add(DeviceSecurityProperty.SERVER_KEY_AGREEMENT_CERTIFICATE.getPropertySpec(propertySpecService));
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
        public String getDefaultTranslation() {
            return "Responses encrypted";
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            List<PropertySpec> propertySpecs = new ArrayList<>();
            propertySpecs.add(getClientMacAddressPropertySpec());
            propertySpecs.add(DeviceSecurityProperty.ENCRYPTION_KEY.getPropertySpec(propertySpecService));
            propertySpecs.add(DeviceSecurityProperty.AUTHENTICATION_KEY.getPropertySpec(propertySpecService));
            propertySpecs.add(DeviceSecurityProperty.SERVER_SIGNATURE_CERTIFICATE.getPropertySpec(propertySpecService));
            propertySpecs.add(DeviceSecurityProperty.SERVER_KEY_AGREEMENT_CERTIFICATE.getPropertySpec(propertySpecService));
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
        public String getDefaultTranslation() {
            return "Responses authenticated and encrypted";
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            List<PropertySpec> propertySpecs = new ArrayList<>();
            propertySpecs.add(getClientMacAddressPropertySpec());
            propertySpecs.add(DeviceSecurityProperty.ENCRYPTION_KEY.getPropertySpec(propertySpecService));
            propertySpecs.add(DeviceSecurityProperty.AUTHENTICATION_KEY.getPropertySpec(propertySpecService));
            propertySpecs.add(DeviceSecurityProperty.SERVER_SIGNATURE_CERTIFICATE.getPropertySpec(propertySpecService));
            propertySpecs.add(DeviceSecurityProperty.SERVER_KEY_AGREEMENT_CERTIFICATE.getPropertySpec(propertySpecService));
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
        public String getDefaultTranslation() {
            return "Responses signed";
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            List<PropertySpec> propertySpecs = new ArrayList<>();
            propertySpecs.add(getClientMacAddressPropertySpec());
            propertySpecs.add(DeviceSecurityProperty.SERVER_SIGNATURE_CERTIFICATE.getPropertySpec(propertySpecService));
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
        public String getDefaultTranslation() {
            return "Responses authenticated and signed";
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            List<PropertySpec> propertySpecs = new ArrayList<>();
            propertySpecs.add(getClientMacAddressPropertySpec());
            propertySpecs.add(DeviceSecurityProperty.ENCRYPTION_KEY.getPropertySpec(propertySpecService));
            propertySpecs.add(DeviceSecurityProperty.AUTHENTICATION_KEY.getPropertySpec(propertySpecService));
            propertySpecs.add(DeviceSecurityProperty.SERVER_KEY_AGREEMENT_CERTIFICATE.getPropertySpec(propertySpecService));
            propertySpecs.add(DeviceSecurityProperty.SERVER_SIGNATURE_CERTIFICATE.getPropertySpec(propertySpecService));
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
        public String getDefaultTranslation() {
            return "Responses encrypted and signed";
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            List<PropertySpec> propertySpecs = new ArrayList<>();
            propertySpecs.add(getClientMacAddressPropertySpec());
            propertySpecs.add(DeviceSecurityProperty.ENCRYPTION_KEY.getPropertySpec(propertySpecService));
            propertySpecs.add(DeviceSecurityProperty.AUTHENTICATION_KEY.getPropertySpec(propertySpecService));
            propertySpecs.add(DeviceSecurityProperty.SERVER_KEY_AGREEMENT_CERTIFICATE.getPropertySpec(propertySpecService));
            propertySpecs.add(DeviceSecurityProperty.SERVER_SIGNATURE_CERTIFICATE.getPropertySpec(propertySpecService));
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
        public String getDefaultTranslation() {
            return "Responses authenticated, encrypted and signed";
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            List<PropertySpec> propertySpecs = new ArrayList<>();
            propertySpecs.add(getClientMacAddressPropertySpec());
            propertySpecs.add(DeviceSecurityProperty.ENCRYPTION_KEY.getPropertySpec(propertySpecService));
            propertySpecs.add(DeviceSecurityProperty.AUTHENTICATION_KEY.getPropertySpec(propertySpecService));
            propertySpecs.add(DeviceSecurityProperty.SERVER_KEY_AGREEMENT_CERTIFICATE.getPropertySpec(propertySpecService));
            propertySpecs.add(DeviceSecurityProperty.SERVER_SIGNATURE_CERTIFICATE.getPropertySpec(propertySpecService));
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
        public String getDefaultTranslation() {
            return "High level authentication using SHA256";
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            List<PropertySpec> propertySpecs = new ArrayList<>();
            propertySpecs.add(getClientMacAddressPropertySpec());
            propertySpecs.add(DeviceSecurityProperty.PASSWORD.getPropertySpec(propertySpecService));
            return propertySpecs;
        }
    }

    /**
     * An authentication level specifying that ECDSA (digital signature) will be used
     * to authenticate ourselves with the device.
     * <p>
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
        public String getDefaultTranslation() {
            return "High level authentication using ECDSA";
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            List<PropertySpec> propertySpecs = new ArrayList<>();
            propertySpecs.add(getClientMacAddressPropertySpec());
            propertySpecs.add(DeviceSecurityProperty.SERVER_SIGNATURE_CERTIFICATE.getPropertySpec(propertySpecService));
            return propertySpecs;
        }
    }
}
package com.elster.jupiter.http.whiteboard.impl;

import org.opensaml.core.config.InitializationException;
import org.opensaml.core.config.InitializationService;
import org.opensaml.core.metrics.impl.MetricRegistryInitializer;
import org.opensaml.saml.common.SAMLException;
import org.opensaml.saml.config.SAMLConfigurationInitializer;
import org.opensaml.saml.config.XMLObjectProviderInitializer;
import org.opensaml.xmlsec.config.ApacheXMLSecurityInitializer;
import org.opensaml.xmlsec.config.GlobalAlgorithmRegistryInitializer;
import org.opensaml.xmlsec.config.GlobalSecurityConfigurationInitializer;
import org.opensaml.xmlsec.config.JavaCryptoValidationInitializer;

import java.io.UnsupportedEncodingException;

public final class SamlUtils {

    public static final String SAML_IDP_ENDPOINT = "https://pentalog-dev.onelogin.com/trust/saml2/http-post/sso/973243";
    public static final String SAML_ACS_ENDPOINT = "http://localhost:8080/api/apps/security/acs";
    public static final String ERROR_PROBLEM_DEFLATE_AND_ENCODE_REQUEST_TO_BASE64 = "Problem deflate AuthnRequest and encode to Base64";
    public static final String ERROR_PROBLEM_PARSING_XML_OF_THE_RESPONSE = "Problem parsing XML of the response";
    public static final String ERROR_XMLOBJECT_NOT_CAST_TO_RESPONSE = "XMLObject does not cast to Response";
    public static final String ERROR_STATUS_CODE_NOT_SUCCESS = "StatusCode was not a success";
    public static final int BACKLASH_FOR_MESSAGE_IN_SECONDS = 300;
    public static final String ERROR_PROBLEM_DECODE_RESPONSE_FROM_BASE64 = "";
    public static final String X509_CERTIFICATE = "MIID3zCCAsegAwIBAgIUai6NUwHUyrT8D3ENwa3T6hvY7LwwDQYJKoZIhvcNAQEFBQAwRjERMA8GA1UECgwIUGVudGFsb2cxFTATBgNVBAsMDE9uZUxvZ2luIElkUDEaMBgGA1UEAwwRT25lTG9naW4gQWNjb3VudCAwHhcNMTkwODE0MDgyMDAzWhcNMjQwODE0MDgyMDAzWjBGMREwDwYDVQQKDAhQZW50YWxvZzEVMBMGA1UECwwMT25lTG9naW4gSWRQMRowGAYDVQQDDBFPbmVMb2dpbiBBY2NvdW50IDCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBAKI/m3JuJO+oOWgGTPoThZ97tAvs0T1mWq48j0SdIckL+iKCRXe8Y90EKZZd2c3RBhUJwEStOdTxoHz/klRgfkNly2xqqmaqFKThmGe3xlFcAkwJp8c6N7lwSRCLorcDyxKwRTy1yY1TjSh92bWt0dH5JPG00GPQHEX/viWlqKuZTzmGh7mtje1K4q75g1INFVR4yqU/LNzoKB1erDxazN+964Ys60xST43O1uMbsxu9kDE+0cOCDZ6MtEKhEzsgqQdYmlhZ/EXdYJwKfHROoEG3blee32rZklkGc/aSmXhxUAQJNgtJv7QbDPZdxmAHYfj6VZfJpDRTaJqU1NEEIdcCAwEAAaOBxDCBwTAMBgNVHRMBAf8EAjAAMB0GA1UdDgQWBBRaAXwLErrEgkjPcPUo43yXlmbz3TCBgQYDVR0jBHoweIAUWgF8CxK6xIJIz3D1KON8l5Zm892hSqRIMEYxETAPBgNVBAoMCFBlbnRhbG9nMRUwEwYDVQQLDAxPbmVMb2dpbiBJZFAxGjAYBgNVBAMMEU9uZUxvZ2luIEFjY291bnQgghRqLo1TAdTKtPwPcQ3BrdPqG9jsvDAOBgNVHQ8BAf8EBAMCB4AwDQYJKoZIhvcNAQEFBQADggEBAAT/VBjAZd0E7iCjSxX8DcJK31o8IyrUemuaq6dss+jDVdWy9WtWJg80JZgTLF/V968D7mVV+ZDHLREVip7xfPAkOG7M15i3Nxsb8MxMEjsAK77ITGs13mTAtrfmHWn+3/ZacdMzljSLP5jhCVh0FcODXhdvLJDXK3Rc/JiEGoZhNFEjzplblxTetNM0xG0n0FYLfPGemViIX5vsVCsg3BoJIKSkEoGt9vhMOIUMhYF1KPF3YqdfH1JXEtHaGJxnhKNIoKqr1G5C574XusH4X0RSL4YuoJ7cHDG3vpgJkoxeKYGuP45ORsfj55AypRx3nmYUVsjsJCp20xP3hB8xxcc=";
    public static final String SAML_OCTA_IDP_ENDPOINT = "http://idp.oktadev.com/";
    public static final String SAML_OCTA_ACS_ENDPOINT = "http://253c50eb.ngrok.io/api/apps/security/acs";
    public static final String OCTA_X509_CERTIFICATE = "MIID3zCCAsegAwIBAgIUai6NUwHUyrT8D3ENwa3T6hvY7LwwDQYJKoZIhvcNAQEFBQAwRjERMA8GA1UECgwIUGVudGFsb2cxFTATBgNVBAsMDE9uZUxvZ2luIElkUDEaMBgGA1UEAwwRT25lTG9naW4gQWNjb3VudCAwHhcNMTkwODE0MDgyMDAzWhcNMjQwODE0MDgyMDAzWjBGMREwDwYDVQQKDAhQZW50YWxvZzEVMBMGA1UECwwMT25lTG9naW4gSWRQMRowGAYDVQQDDBFPbmVMb2dpbiBBY2NvdW50IDCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBAKI/m3JuJO+oOWgGTPoThZ97tAvs0T1mWq48j0SdIckL+iKCRXe8Y90EKZZd2c3RBhUJwEStOdTxoHz/klRgfkNly2xqqmaqFKThmGe3xlFcAkwJp8c6N7lwSRCLorcDyxKwRTy1yY1TjSh92bWt0dH5JPG00GPQHEX/viWlqKuZTzmGh7mtje1K4q75g1INFVR4yqU/LNzoKB1erDxazN+964Ys60xST43O1uMbsxu9kDE+0cOCDZ6MtEKhEzsgqQdYmlhZ/EXdYJwKfHROoEG3blee32rZklkGc/aSmXhxUAQJNgtJv7QbDPZdxmAHYfj6VZfJpDRTaJqU1NEEIdcCAwEAAaOBxDCBwTAMBgNVHRMBAf8EAjAAMB0GA1UdDgQWBBRaAXwLErrEgkjPcPUo43yXlmbz3TCBgQYDVR0jBHoweIAUWgF8CxK6xIJIz3D1KON8l5Zm892hSqRIMEYxETAPBgNVBAoMCFBlbnRhbG9nMRUwEwYDVQQLDAxPbmVMb2dpbiBJZFAxGjAYBgNVBAMMEU9uZUxvZ2luIEFjY291bnQgghRqLo1TAdTKtPwPcQ3BrdPqG9jsvDAOBgNVHQ8BAf8EBAMCB4AwDQYJKoZIhvcNAQEFBQADggEBAAT/VBjAZd0E7iCjSxX8DcJK31o8IyrUemuaq6dss+jDVdWy9WtWJg80JZgTLF/V968D7mVV+ZDHLREVip7xfPAkOG7M15i3Nxsb8MxMEjsAK77ITGs13mTAtrfmHWn+3/ZacdMzljSLP5jhCVh0FcODXhdvLJDXK3Rc/JiEGoZhNFEjzplblxTetNM0xG0n0FYLfPGemViIX5vsVCsg3BoJIKSkEoGt9vhMOIUMhYF1KPF3YqdfH1JXEtHaGJxnhKNIoKqr1G5C574XusH4X0RSL4YuoJ7cHDG3vpgJkoxeKYGuP45ORsfj55AypRx3nmYUVsjsJCp20xP3hB8xxcc=";


    private SamlUtils() {
    }

    public static void initializeOpenSAML() throws InitializationException {
        InitializationService.initialize();
        SAMLConfigurationInitializer samlConfigurationInitializer = new SAMLConfigurationInitializer();
        samlConfigurationInitializer.init();
        MetricRegistryInitializer metricRegistryInitializer = new MetricRegistryInitializer();
        metricRegistryInitializer.init();
        XMLObjectProviderInitializer xmlObjectProviderInitializer = new XMLObjectProviderInitializer();
        xmlObjectProviderInitializer.init();
        org.opensaml.xmlsec.config.XMLObjectProviderInitializer xmlObjectProviderInitializerConfig = new org.opensaml.xmlsec.config.XMLObjectProviderInitializer();
        xmlObjectProviderInitializerConfig.init();
        org.opensaml.xacml.config.XMLObjectProviderInitializer xmlObjectProviderInitializerXacml = new org.opensaml.xacml.config.XMLObjectProviderInitializer();
        xmlObjectProviderInitializerXacml.init();
        org.opensaml.soap.config.XMLObjectProviderInitializer xmlObjectProviderInitializerSoapConfig = new org.opensaml.soap.config.XMLObjectProviderInitializer();
        xmlObjectProviderInitializerSoapConfig.init();
        org.opensaml.xacml.profile.saml.config.XMLObjectProviderInitializer xmlObjectProviderInitializerProfileXacml = new org.opensaml.xacml.profile.saml.config.XMLObjectProviderInitializer();
        xmlObjectProviderInitializerProfileXacml.init();
        ApacheXMLSecurityInitializer apacheXMLSecurityInitializer = new ApacheXMLSecurityInitializer();
        apacheXMLSecurityInitializer.init();
        GlobalAlgorithmRegistryInitializer globalAlgorithmRegistryInitializer = new GlobalAlgorithmRegistryInitializer();
        globalAlgorithmRegistryInitializer.init();
        GlobalSecurityConfigurationInitializer globalSecurityConfigurationInitializer = new GlobalSecurityConfigurationInitializer();
        globalSecurityConfigurationInitializer.init();
        JavaCryptoValidationInitializer javaCryptoValidationInitializer = new JavaCryptoValidationInitializer();
        javaCryptoValidationInitializer.init();
    }

    public static byte[] getBytesWithCatch(String value, String textError) throws SAMLException {
        try {
            return value.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new SAMLException(textError, e);
        }
    }


}

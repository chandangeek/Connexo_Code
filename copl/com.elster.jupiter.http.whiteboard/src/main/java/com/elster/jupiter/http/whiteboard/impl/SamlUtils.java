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

    public static final String ERROR_PROBLEM_DEFLATE_AND_ENCODE_REQUEST_TO_BASE64 = "Problem deflate AuthnRequest and encode to Base64";
    public static final String ERROR_PROBLEM_PARSING_XML_OF_THE_RESPONSE = "Problem parsing XML of the response";
    public static final String ERROR_XMLOBJECT_NOT_CAST_TO_RESPONSE = "XMLObject does not cast to Response";
    public static final String ERROR_STATUS_CODE_NOT_SUCCESS = "StatusCode was not a success";
    public static final int BACKLASH_FOR_MESSAGE_IN_SECONDS = 300;
    public static final String ERROR_PROBLEM_DECODE_RESPONSE_FROM_BASE64 = "Problem while decode response from Base64";

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

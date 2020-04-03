package com.elster.jupiter.http.whiteboard.impl.saml;

import net.shibboleth.utilities.java.support.xml.XMLParserException;
import org.apache.cxf.common.util.CompressionUtils;
import org.bouncycastle.util.encoders.Base64;
import org.opensaml.core.config.InitializationException;
import org.opensaml.core.config.InitializationService;
import org.opensaml.core.metrics.impl.MetricRegistryInitializer;
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.core.xml.io.UnmarshallingException;
import org.opensaml.saml.common.SAMLException;
import org.opensaml.saml.config.SAMLConfigurationInitializer;
import org.opensaml.saml.config.XMLObjectProviderInitializer;
import org.opensaml.saml.saml2.core.LogoutRequest;
import org.opensaml.saml.saml2.core.LogoutResponse;
import org.opensaml.saml.saml2.core.Status;
import org.opensaml.saml.saml2.core.StatusCode;
import org.opensaml.saml.saml2.core.impl.LogoutRequestUnmarshaller;
import org.opensaml.saml.saml2.core.impl.LogoutResponseBuilder;
import org.opensaml.saml.saml2.core.impl.LogoutResponseMarshaller;
import org.opensaml.saml.saml2.core.impl.LogoutResponseUnmarshaller;
import org.opensaml.saml.saml2.core.impl.StatusBuilder;
import org.opensaml.saml.saml2.core.impl.StatusCodeBuilder;
import org.opensaml.xmlsec.config.ApacheXMLSecurityInitializer;
import org.opensaml.xmlsec.config.GlobalAlgorithmRegistryInitializer;
import org.opensaml.xmlsec.config.GlobalSecurityConfigurationInitializer;
import org.opensaml.xmlsec.config.JavaCryptoValidationInitializer;
import org.w3c.dom.Element;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.Objects;
import java.util.zip.DataFormatException;

/**
 * Singleton so we can setup security configurations once
 */
public final class SAMLUtilities {

    public static final String ERROR_PROBLEM_DEFLATE_AND_ENCODE_REQUEST_TO_BASE64 = "Problem deflate AuthnRequest and encode to Base64";
    public static final String ERROR_PROBLEM_PARSING_XML_OF_THE_RESPONSE = "Problem parsing XML of the response";
    public static final String ERROR_XMLOBJECT_NOT_CAST_TO_RESPONSE = "XMLObject does not cast to Response";
    public static final String ERROR_STATUS_CODE_NOT_SUCCESS = "StatusCode was not a success";
    public static final int BACKLASH_FOR_MESSAGE_IN_SECONDS = 300;
    public static final String ERROR_PROBLEM_DECODE_RESPONSE_FROM_BASE64 = "Problem while decode response from Base64";

    private static volatile SAMLUtilities INSTANCE;

    public synchronized static SAMLUtilities getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new SAMLUtilities();
        }
        return INSTANCE;
    }

    private SAMLUtilities() {
        Thread thread = Thread.currentThread();
        ClassLoader loader = thread.getContextClassLoader();
        thread.setContextClassLoader(InitializationService.class.getClassLoader());
        try {
            initializeOpenSAML();
        } catch (InitializationException e) {
            throw new RuntimeException(e);
        } finally {
            thread.setContextClassLoader(loader);
        }
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

    public static LogoutRequest createLogoutRequest(final String base64EncodedAndDeflatedSLORequest) throws UnmarshallingException, DataFormatException, XMLParserException {
        final Element documentElement = Objects.requireNonNull(XMLObjectProviderRegistrySupport.getParserPool()).parse(CompressionUtils.inflate(Base64.decode(base64EncodedAndDeflatedSLORequest))).getDocumentElement();
        final LogoutRequestUnmarshaller logoutRequestUnmarshaller = new LogoutRequestUnmarshaller();
        return (LogoutRequest) logoutRequestUnmarshaller.unmarshall(documentElement);
    }

    public static LogoutResponse createLogoutResponse(final String statusCodeAttribute) {
        final LogoutResponseBuilder logoutResponseBuilder = new LogoutResponseBuilder();
        final LogoutResponse logoutResponse = logoutResponseBuilder.buildObject();
        final StatusBuilder statusBuilder = new StatusBuilder();
        final StatusCodeBuilder statusCodeBuilder = new StatusCodeBuilder();
        final StatusCode statusCode = statusCodeBuilder.buildObject();
        final Status status = statusBuilder.buildObject();
        statusCode.setValue(statusCodeAttribute);
        status.setStatusCode(statusCode);
        logoutResponse.setStatus(status);
        return logoutResponse;
    }

    public static String marshallLogoutResponse(final LogoutResponse logoutResponse) throws MarshallingException, TransformerException {
        final Transformer transformer = TransformerFactory.newInstance().newTransformer();
        final StreamResult streamResult = new StreamResult(new StringWriter());
        final LogoutResponseMarshaller logoutResponseMarshaller = new LogoutResponseMarshaller();
        final Element marshalledLogoutResponse = logoutResponseMarshaller.marshall(logoutResponse);
        transformer.transform(new DOMSource(marshalledLogoutResponse), streamResult);
        final byte[] base64EncodedAndDeflatedLogoutResponse = Base64.encode(CompressionUtils.deflate(streamResult.getWriter().toString().getBytes()));
        return new String(base64EncodedAndDeflatedLogoutResponse);
    }

    public static LogoutResponse unmarshallLogoutResponse(final String base64EncodedAndDeflatedLogoutResponse) throws UnmarshallingException, DataFormatException, XMLParserException {
        final Element documentElement = Objects.requireNonNull(XMLObjectProviderRegistrySupport.getParserPool()).parse(CompressionUtils.inflate(Base64.decode(base64EncodedAndDeflatedLogoutResponse))).getDocumentElement();
        final LogoutResponseUnmarshaller logoutResponseUnmarshaller = new LogoutResponseUnmarshaller();
        return (LogoutResponse) logoutResponseUnmarshaller.unmarshall(documentElement);
    }

}

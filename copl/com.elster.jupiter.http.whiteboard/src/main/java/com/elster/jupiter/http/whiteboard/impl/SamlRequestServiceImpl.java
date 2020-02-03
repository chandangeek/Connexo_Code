package com.elster.jupiter.http.whiteboard.impl;

import org.apache.commons.codec.binary.Base64;
import org.apache.cxf.common.util.CompressionUtils;
import org.joda.time.DateTime;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.XMLObjectBuilderFactory;
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.core.xml.io.Marshaller;
import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.saml.common.SAMLException;
import org.opensaml.saml.common.SAMLObject;
import org.opensaml.saml.common.SAMLVersion;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.saml2.core.*;
import org.opensaml.saml.saml2.core.impl.AuthnRequestBuilder;
import org.osgi.service.component.annotations.Component;
import org.w3c.dom.Element;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.namespace.QName;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

@Component(name = "com.elster.jupiter.rest.whiteboard.impl.SamlRequestService", service = SamlRequestService.class, immediate = true)
public class SamlRequestServiceImpl implements SamlRequestService {

    private static final Logger LOGGER = Logger.getLogger(SamlRequestServiceImpl.class.getName());

    @Override
    public AuthnRequest createAuthnRequest(String assertionConsumerServiceUrl, String requestId, DateTime issueInstant, String issuerName) {
        XMLObjectBuilderFactory builderFactory = XMLObjectProviderRegistrySupport.getBuilderFactory();

        AuthnRequestBuilder authnRequestBuilder = (AuthnRequestBuilder) builderFactory.getBuilder(AuthnRequest.DEFAULT_ELEMENT_NAME);
        AuthnRequest request = authnRequestBuilder.buildObject();

        request.setID(requestId);
        request.setVersion(SAMLVersion.VERSION_20);
        request.setIssueInstant(issueInstant);
        request.setProtocolBinding(SAMLConstants.SAML2_POST_BINDING_URI);
        request.setAssertionConsumerServiceURL(assertionConsumerServiceUrl);

        Issuer issuer = build(Issuer.DEFAULT_ELEMENT_NAME);
        issuer.setValue(issuerName);
        request.setIssuer(issuer);

        NameIDPolicy nameIDPolicy = build(NameIDPolicy.DEFAULT_ELEMENT_NAME);
        nameIDPolicy.setFormat(NameIDType.EMAIL);
        request.setNameIDPolicy(nameIDPolicy);

        RequestedAuthnContext requestedAuthnContext = build(RequestedAuthnContext.DEFAULT_ELEMENT_NAME);
        requestedAuthnContext.setComparison(AuthnContextComparisonTypeEnumeration.EXACT);
        request.setRequestedAuthnContext(requestedAuthnContext);

        AuthnContextClassRef authnContextClassRef = build(AuthnContextClassRef.DEFAULT_ELEMENT_NAME);
        authnContextClassRef.setAuthnContextClassRef(AuthnContext.PPT_AUTHN_CTX);
        requestedAuthnContext.getAuthnContextClassRefs().add(authnContextClassRef);


        return request;
    }

    @Override
    public <T extends SAMLObject> T build(QName qName) {
        return (T) XMLObjectProviderRegistrySupport.getBuilderFactory().getBuilder(qName).buildObject(qName);
    }

    @Override
    public String convertXmlObjectToString(XMLObject xmlObject) throws SAMLException {
        try {
            Marshaller marshaller = XMLObjectProviderRegistrySupport.getMarshallerFactory().getMarshaller(xmlObject);
            Element dom = marshaller.marshall(xmlObject);
            StringWriter stringWriter = new StringWriter();
            TransformerFactory transFactory = TransformerFactory.newInstance();
            Transformer transformer = transFactory.newTransformer();
            transformer.transform(new DOMSource(dom), new StreamResult(stringWriter));
            return stringWriter.toString();
        } catch (RuntimeException | MarshallingException | TransformerException e) {
            throw new SAMLException(String.format("Problem convert %s to string", xmlObject.getElementQName()), e);
        }
    }

    @Override
    public Optional<String> createSSOAuthenticationRequest(HttpServletRequest request, HttpServletResponse response, String acsEndpoint) {
        try {
            AuthnRequest authnRequest = this.createAuthnRequest(acsEndpoint, UUID.randomUUID().toString(), DateTime.now(), request.getRequestURL().toString());
            String convertedAuthnRequest = this.convertXmlObjectToString(authnRequest);
            return Optional.of(Base64.encodeBase64String(CompressionUtils.deflate(SAMLUtilities.getBytesWithCatch(convertedAuthnRequest, SAMLUtilities.ERROR_PROBLEM_DEFLATE_AND_ENCODE_REQUEST_TO_BASE64))));

        } catch (SAMLException e) {
            LOGGER.log(Level.SEVERE, "Error while trying to create SAML Request", e);
        }
        return Optional.empty();
    }
}

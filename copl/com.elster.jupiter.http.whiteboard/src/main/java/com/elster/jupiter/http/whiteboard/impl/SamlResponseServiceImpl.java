package com.elster.jupiter.http.whiteboard.impl;

import com.elster.jupiter.users.User;
import com.nimbusds.jose.JOSEException;
import net.shibboleth.utilities.java.support.xml.XMLParserException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.core.xml.io.UnmarshallingException;
import org.opensaml.saml.common.SAMLException;
import org.opensaml.saml.saml2.core.*;
import org.opensaml.security.credential.BasicCredential;
import org.opensaml.xmlsec.signature.Signature;
import org.opensaml.xmlsec.signature.support.SignatureException;
import org.opensaml.xmlsec.signature.support.SignatureValidationProvider;
import org.opensaml.xmlsec.signature.support.SignatureValidator;
import org.opensaml.xmlsec.signature.support.provider.ApacheSantuarioSignatureValidationProviderImpl;
import org.osgi.service.component.annotations.Component;
import org.w3c.dom.Element;

import java.io.ByteArrayInputStream;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Component(name = "com.elster.jupiter.rest.whiteboard.impl.SamlResponseService", service = SamlResponseService.class, immediate = true)
public class SamlResponseServiceImpl implements SamlResponseService {

    private static final Logger LOGGER = Logger.getLogger(SamlResponseServiceImpl.class.getName());

    private volatile SecurityTokenImpl securityToken;

    @Override
    public Response createSamlResponse(String samlResponse) {
        try {
            String xml = new String(Base64.decodeBase64(SamlUtils.getBytesWithCatch(samlResponse, SamlUtils.ERROR_PROBLEM_DECODE_RESPONSE_FROM_BASE64)));
            XMLObject xmlObject = this.convertStringToXmlObject(xml);
            Response response = this.getResponseFromXmlObject(xmlObject);
            checkStatusCode(response);

            Assertion assertion = getCheckedAssertion(response);
            checkConditions(assertion);

            return response;
        } catch (SAMLException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public XMLObject convertStringToXmlObject(String xmlObject) throws SAMLException {
        try {
            Element root = XMLObjectProviderRegistrySupport.getParserPool().parse(new ByteArrayInputStream(xmlObject.getBytes())).getDocumentElement();
            return XMLObjectProviderRegistrySupport.getUnmarshallerFactory().getUnmarshaller(root).unmarshall(root);
        } catch (XMLParserException | UnmarshallingException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new SAMLException(SamlUtils.ERROR_PROBLEM_PARSING_XML_OF_THE_RESPONSE, e);
        }
    }

    @Override
    public Response getResponseFromXmlObject(XMLObject response) throws SAMLException {
        if (!Response.class.isInstance(response)) {
            throw new SAMLException(SamlUtils.ERROR_XMLOBJECT_NOT_CAST_TO_RESPONSE);
        }
        return (Response) response;
    }

    @Override
    public String getSubjectNameId(Assertion assertion) {
        return assertion.getSubject().getNameID().getValue();
    }

    @Override
    public Map<String, List<String>> getAttributeValues(Assertion assertion) {
        Map<String, List<String>> attributes = new HashMap<>();
        for (AttributeStatement attributeStatement : assertion.getAttributeStatements()) {
            for (Attribute attr : attributeStatement.getAttributes()) {
                List<String> values = attr.getAttributeValues().stream().map(value -> value.getDOM().getTextContent()).collect(Collectors.toList());
                attributes.put(attr.getName(), values);
            }
        }
        return attributes;
    }

    @Override
    public void validateSignature(Signature signature, String certificate) throws SAMLException {
        try {
            Certificate cert = getCertificate(certificate);
            BasicCredential credential = new BasicCredential(cert.getPublicKey());
            ApacheSantuarioSignatureValidationProviderImpl signatureValidator = new ApacheSantuarioSignatureValidationProviderImpl();
            signatureValidator.validate(signature, credential);
        } catch (SignatureException | CertificateException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(), e);
            throw new SAMLException("Error while validate signature", e);
        }
    }

    @Override
    public Assertion getCheckedAssertion(Response response) throws SAMLException {
        List<Assertion> assertionList = response.getAssertions();
        if (assertionList.isEmpty()) {
            throw new SAMLException("No assertion found");
        } else if (assertionList.size() > 1) {
            throw new SAMLException("More assertion than one was found");
        }
        return assertionList.get(0);
    }

    @Override
    public String generateTokenForUser(User user) throws JOSEException {

        return securityToken.createToken(user, 0, "");
    }

    private Certificate getCertificate(String certificate) throws CertificateException {
        String beginHeader = "-----BEGIN CERTIFICATE-----";
        String endHeader = "-----END CERTIFICATE-----";
        StringBuilder stringBuilder = new StringBuilder();
        if (!certificate.startsWith(beginHeader)) {
            stringBuilder.append(beginHeader);
            stringBuilder.append("\n");
        }
        stringBuilder.append(certificate);
        if (!certificate.endsWith(endHeader)) {
            stringBuilder.append("\n");
            stringBuilder.append(endHeader);
        }

        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        return cf.generateCertificate(new ByteArrayInputStream(stringBuilder.toString().getBytes()));
    }

    @Override
    public void checkStatusCode(Response response) throws SAMLException {
        String statusCode = response.getStatus().getStatusCode().getValue();
        if (!StringUtils.equals(statusCode, StatusCode.SUCCESS)) {
            throw new SAMLException(SamlUtils.ERROR_STATUS_CODE_NOT_SUCCESS);
        }
    }

    @Override
    public void checkConditions(Assertion assertion) throws SAMLException {
        Conditions conditions = assertion.getConditions();
        Date now = DateTime.now().toDate();
        Date conditionNotBefore = conditions.getNotBefore().minusSeconds(SamlUtils.BACKLASH_FOR_MESSAGE_IN_SECONDS).toDate();
        Date conditionNotOnOrAfter = conditions.getNotOnOrAfter().plusSeconds(SamlUtils.BACKLASH_FOR_MESSAGE_IN_SECONDS).toDate();
        if (now.before(conditionNotBefore)) {
            throw new SAMLException("Conditions are not yet active");
        } else if (now.after(conditionNotOnOrAfter) || now.equals(conditionNotOnOrAfter)) {
            throw new SAMLException("Conditions have expired");
        }
    }

    private String base64Encode(String input)
    {
        return java.util.Base64.getUrlEncoder().encodeToString(input.getBytes());
    }
}

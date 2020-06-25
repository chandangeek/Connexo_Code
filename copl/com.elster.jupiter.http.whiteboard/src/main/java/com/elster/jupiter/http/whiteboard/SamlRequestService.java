package com.elster.jupiter.http.whiteboard;

import org.joda.time.DateTime;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.saml.common.SAMLException;
import org.opensaml.saml.common.SAMLObject;
import org.opensaml.saml.saml2.core.AuthnRequest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.namespace.QName;
import java.util.Optional;

public interface SamlRequestService {

    AuthnRequest createAuthnRequest(String assertionConsumerServiceUrl, String requestId, DateTime issueInstant, String issuerName);

    <T extends SAMLObject> T build(QName qName);

    String convertXmlObjectToString(XMLObject xmlObject) throws SAMLException;

    Optional<String> createSSOAuthenticationRequest(HttpServletRequest request, HttpServletResponse response, String acsEndpoint, String s);

}

package com.elster.jupiter.http.whiteboard;

import com.elster.jupiter.users.User;
import com.nimbusds.jose.JOSEException;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.saml.common.SAMLException;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.xmlsec.signature.Signature;

import java.util.List;
import java.util.Map;

public interface SamlResponseService {

    Response createSamlResponse(String samlResponse);

    XMLObject convertStringToXmlObject(String xmlObject) throws SAMLException;

    Response getResponseFromXmlObject(XMLObject response) throws SAMLException;

    String getSubjectNameId(Assertion assertion);

    Map<String, List<String>> getAttributeValues(Assertion assertion);

    void validateSignature(Signature signature, String certificate) throws SAMLException;

    Assertion getCheckedAssertion(Response response) throws SAMLException;

    String generateTokenForUser(User user) throws JOSEException;

    void checkStatusCode(Response response) throws SAMLException;

    void checkConditions(Assertion assertion) throws SAMLException;
}

package com.elster.jupiter.http.whiteboard.impl;

import com.elster.jupiter.http.whiteboard.HttpAuthenticationService;
import com.elster.jupiter.http.whiteboard.SamlResponseService;
import com.elster.jupiter.http.whiteboard.impl.saml.SamlRequestServiceImpl;
import com.elster.jupiter.http.whiteboard.impl.saml.SamlResponseServiceImpl;
import org.apache.commons.codec.binary.Base64;
import org.apache.cxf.common.util.CollectionUtils;
import org.apache.cxf.helpers.IOUtils;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.saml.common.SAMLException;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.Conditions;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.saml2.core.StatusCode;
import org.opensaml.saml.saml2.core.impl.ResponseImpl;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class SamlResponseServiceImplTest extends BaseAuthenticationTest {

    private SamlResponseService samlResponseService;
    private HttpAuthenticationService httpAuthenticationService;
    private String samlResponseDecoded;


    @Before
    public void init() throws InvalidKeySpecException, NoSuchAlgorithmException, URISyntaxException, IOException {
        samlResponseService = new SamlResponseServiceImpl();
        httpAuthenticationService = getHttpAuthentication();
        samlResponseDecoded = getResourceFileAsString("saml-response-decoded.xml");
    }

    @Test
    public void createSamlResponse() throws SAMLException, UnsupportedEncodingException {
        XMLObject convertedResponse = samlResponseService.convertStringToXmlObject(samlResponseDecoded);
        Response response = samlResponseService.getResponseFromXmlObject(convertedResponse);
        Conditions conditions = response.getAssertions().get(0).getConditions();
        DateTime future = DateTime.now().plusDays(1);
        conditions.setNotOnOrAfter(future);
        assertNotNull(response);
        samlRequestService = new SamlRequestServiceImpl();
        String editedResponse = samlRequestService.convertXmlObjectToString(response);
        String encodedResponse = Base64.encodeBase64String(editedResponse.getBytes("UTF-8"));

        Response receivedResponse = samlResponseService.createSamlResponse(encodedResponse);

        assertNotNull(receivedResponse);
        assertEquals(response.getID(), receivedResponse.getID());
    }

    @Test
    public void convertStringToXmlObject() throws SAMLException {
        XMLObject convertedResponse = samlResponseService.convertStringToXmlObject(samlResponseDecoded);
        assertNotNull(convertedResponse);
        assertEquals(StatusCode.SUCCESS, ((ResponseImpl) convertedResponse).getStatus().getStatusCode().getValue());
    }

    @Test(expected = SAMLException.class)
    public void shouldThrowExceptionParsingXml() throws SAMLException{

        samlResponseService.convertStringToXmlObject("some arbitrary string");
    }

    @Test
    public void getResponseFromXmlObject() throws SAMLException {
        XMLObject convertedResponse = samlResponseService.convertStringToXmlObject(samlResponseDecoded);
        Response response = samlResponseService.getResponseFromXmlObject(convertedResponse);
        assertNotNull(response);
        assertFalse(CollectionUtils.isEmpty(response.getAssertions()));
        assertNotNull(response.getSignature());
    }

    @Test
    public void getSubjectNameId() {
        Assertion assertion = mock(Assertion.class, RETURNS_DEEP_STUBS);
        String subjectNameID = "test@connexo.nl";
        when(assertion.getSubject().getNameID().getValue()).thenReturn(subjectNameID);

        String result = samlResponseService.getSubjectNameId(assertion);

        assertEquals(subjectNameID, result);
    }

    @Test
    public void getCheckedAssertion() throws SAMLException {
        Response response = mock(Response.class, RETURNS_DEEP_STUBS);
        Assertion assertion = mock(Assertion.class);
        when(response.getAssertions().get(0)).thenReturn(assertion);

        Assertion assertionResult = samlResponseService.getCheckedAssertion(response);

        assertEquals(assertion, assertionResult);
    }

    private String getResourceFileAsString(String path) throws IOException {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(path)) {
            return IOUtils.toString(input);
        }
    }
}
package com.elster.jupiter.http.whiteboard.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.saml.common.SAMLException;
import org.opensaml.saml.common.SAMLVersion;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.saml2.core.AuthnContext;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.NameIDType;

import com.elster.jupiter.http.whiteboard.HttpAuthenticationService;
import com.elster.jupiter.http.whiteboard.SamlRequestService;
import com.elster.jupiter.http.whiteboard.impl.saml.SamlRequestServiceImpl;

@RunWith(MockitoJUnitRunner.class)
public class SamlRequestServiceImplTest extends BaseAuthenticationTest {

    private static final String ACS_ENDPONT = "https://connexo/security/acs";
    private static final String ISSUER_ID = "https://connexo/SAML2";
    private DateTime ISSUE_INSTANT = DateTime.now().withZone(DateTimeZone.UTC);

    private SamlRequestService samlRequestService;
    private HttpAuthenticationService httpAuthenticationService;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private HttpServletRequest httpServletRequest;

    @Mock
    private HttpServletResponse httpServletResponse;

    @Mock
    private AuthnRequest authnRequest;

    @Before
    public void init() throws InvalidKeySpecException, NoSuchAlgorithmException {
        samlRequestService = new SamlRequestServiceImpl();
        httpAuthenticationService = getHttpAuthentication();
    }

    @Test
    public void createAuthnRequest() {
        String issuerName = "https://connexo.com";
        String requestId = "someId";

        AuthnRequest authnRequest = samlRequestService.createAuthnRequest(ACS_ENDPONT, requestId, ISSUE_INSTANT, issuerName);

        assertEquals(authnRequest.getAssertionConsumerServiceURL(), ACS_ENDPONT);
        assertEquals(authnRequest.getID(), requestId);
        assertEquals(authnRequest.getIssueInstant(), ISSUE_INSTANT);
        assertEquals(authnRequest.getVersion(), SAMLVersion.VERSION_20);
        assertEquals(authnRequest.getProtocolBinding(), SAMLConstants.SAML2_POST_BINDING_URI);
        assertEquals(authnRequest.getIssuer().getValue(), issuerName);
        assertEquals(authnRequest.getNameIDPolicy().getFormat(), NameIDType.UNSPECIFIED);
        assertNull(authnRequest.getRequestedAuthnContext());
    }

    @Test
    public void convertXmlObjectToString() throws SAMLException {
        XMLObject xmlObject = samlRequestService.build(AuthnContext.DEFAULT_ELEMENT_NAME);

        String xml = samlRequestService.convertXmlObjectToString(xmlObject);

        assertNotNull(xml);
        assertTrue(xml.contains(xmlObject.getElementQName().getNamespaceURI()));
    }

    @Test
    public void createSSOAuthenticationRequest() throws SAMLException {
        when(httpServletRequest.getRequestURL()).thenReturn(new StringBuffer("/request/url"));

        Optional<String> samlRequest = samlRequestService.createSSOAuthenticationRequest(httpServletRequest, httpServletResponse, ACS_ENDPONT, ISSUER_ID);

        assertTrue(samlRequest.isPresent());
    }
}
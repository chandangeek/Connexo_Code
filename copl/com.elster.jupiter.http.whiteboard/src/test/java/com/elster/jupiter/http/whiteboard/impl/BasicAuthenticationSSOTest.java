package com.elster.jupiter.http.whiteboard.impl;

import com.elster.jupiter.http.whiteboard.HttpAuthenticationService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Optional;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class BasicAuthenticationSSOTest extends BaseAuthenticationTest{

    private HttpAuthenticationService httpAuthenticationService;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private HttpServletRequest httpServletRequest;

    @Mock
    private HttpServletResponse httpServletResponse;

    @Before
    public void init() throws InvalidKeySpecException, NoSuchAlgorithmException {
        httpAuthenticationService = getHttpAuthentication();
    }


    @Test
    public void shouldNotAllowRequestsForLoginPage() throws IOException {
        when(httpServletRequest.getRequestURL()).thenReturn(new StringBuffer(LOGIN_URL));
        when(httpServletRequest.getRequestURI()).thenReturn(LOGIN_URL);

        boolean result = httpAuthenticationService.handleSecurity(httpServletRequest, httpServletResponse);

        assertFalse(result);
        verify(this.httpServletRequest, times(2)).getRequestURI();
        verify(this.httpServletRequest).getRequestURL();
    }

    @Test
    public void shouldAllowRequestsForSamlRequest() throws IOException {
        final String requestUrl = "/apps/admin/";
        when(httpServletRequest.getRequestURL()).thenReturn(new StringBuffer(requestUrl));
        when(httpServletRequest.getRequestURI()).thenReturn(requestUrl);
        when(samlRequestService.createSSOAuthenticationRequest(any(), any(), anyString(), anyString())).thenReturn(Optional.of(anyString()));

        boolean result = httpAuthenticationService.handleSecurity(httpServletRequest, httpServletResponse);

        assertTrue(result);
        verify(this.httpServletRequest, times(2)).getRequestURI();
        verify(this.httpServletRequest, times(2)).getRequestURL();
        verify(this.samlRequestService).createSSOAuthenticationRequest(any(), any(), anyString(), anyString());
    }

    @Test
    public void shouldAllowRequestsForLoginWithRedirectPage() throws IOException {
        when(httpServletRequest.getRequestURL()).thenReturn(new StringBuffer(LOGIN_URL));
        when(httpServletRequest.getRequestURI()).thenReturn(LOGIN_URL);
        when(httpServletRequest.getParameter("page")).thenReturn("redirectPage");
        when(samlRequestService.createSSOAuthenticationRequest(any(), any(), anyString(), anyString())).thenReturn(Optional.of(anyString()));

        boolean result = httpAuthenticationService.handleSecurity(httpServletRequest, httpServletResponse);

        assertTrue(result);
        verify(this.httpServletRequest, times(2)).getRequestURI();
        verify(this.httpServletRequest).getRequestURL();
        verify(this.httpServletRequest, times(3)).getParameter("page");
        verify(this.samlRequestService).createSSOAuthenticationRequest(any(), any(), anyString(), anyString());
    }

    @Test
    public void shouldNotAllowRequestsForLoginWithLogoutParameter() throws IOException {
        when(httpServletRequest.getRequestURL()).thenReturn(new StringBuffer(LOGIN_URL));
        when(httpServletRequest.getRequestURI()).thenReturn(LOGIN_URL);
        when(httpServletRequest.getParameterMap().containsKey("logout")).thenReturn(true);

        boolean result = httpAuthenticationService.handleSecurity(httpServletRequest, httpServletResponse);

        assertFalse(result);
        verify(this.httpServletRequest, times(2)).getRequestURI();
        verify(this.httpServletRequest).getRequestURL();
        verify(this.httpServletRequest).getParameterMap();
    }
}

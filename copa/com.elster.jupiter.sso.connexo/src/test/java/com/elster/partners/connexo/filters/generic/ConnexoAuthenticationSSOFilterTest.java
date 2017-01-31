/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.partners.connexo.filters.generic;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by dragos on 1/29/2016.
 */
@RunWith(MockitoJUnitRunner.class)
public class ConnexoAuthenticationSSOFilterTest {

    @Mock
    FilterConfig filterConfig;

    @Mock
    HttpServletRequest httpServletRequest;

    @Mock
    HttpServletResponse httpServletResponse;

    @Mock
    FilterChain filterChain;

    @Test
    public void testNoTokenExcludeURLs() throws IOException, ServletException {
        // Given
        ConnexoAuthenticationSSOFilter ssoFilter = new ConnexoAuthenticationSSOFilter();

        when(filterConfig.getInitParameter("excludePatterns")).thenReturn("/exclude/url/*");
        ssoFilter.init(filterConfig);

        when(httpServletRequest.getRequestURI()).thenReturn("/context/exclude/url/test");
        when(httpServletRequest.getContextPath()).thenReturn("/context");

        // When
        ssoFilter.doFilter(httpServletRequest, httpServletResponse,
                filterChain);

        // Then
        verify(filterChain).doFilter(httpServletRequest, httpServletResponse);
        verify(httpServletRequest, never()).getCookies();
        verify(httpServletRequest, never()).getHeader(anyString());
        verify(httpServletRequest, never()).getHeaders(anyString());
    }

    @Test
    public void testNoTokenUnauthorized() throws IOException, ServletException {
        // Given
        ConnexoAuthenticationSSOFilter ssoFilter = new ConnexoAuthenticationSSOFilter();

        when(filterConfig.getInitParameter("unauthorizedPatterns")).thenReturn("/unauthorized/url/*");
        ssoFilter.init(filterConfig);

        when(httpServletRequest.getRequestURI()).thenReturn("/context/unauthorized/url/test");
        when(httpServletRequest.getContextPath()).thenReturn("/context");
        when(httpServletRequest.getCookies()).thenReturn(null);
        when(httpServletRequest.getHeader("Authorization")).thenReturn(null);

        HttpSession session = mock(HttpSession.class);
        when(httpServletRequest.getSession()).thenReturn(session);
        doNothing().when(session).invalidate();

        // When
        ssoFilter.doFilter(httpServletRequest, httpServletResponse,
                filterChain);

        // Then
        verify(filterChain, never()).doFilter(any(HttpServletRequest.class), any(HttpServletResponse.class));
        verify(httpServletRequest).getSession();
        verify(session).invalidate();
        verify(httpServletResponse).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    }

    @Test
    public void testNoTokenRedirectToLogin() throws IOException, ServletException {
        // Given
        ConnexoAuthenticationSSOFilter ssoFilter = new ConnexoAuthenticationSSOFilter();
        ssoFilter.init(filterConfig);

        when(httpServletRequest.getRequestURL()).thenReturn(new StringBuffer("http://localhost:9090/context/url/test"));
        when(httpServletRequest.getRequestURI()).thenReturn("/context/url/test");
        when(httpServletRequest.getContextPath()).thenReturn("/context");

        HttpSession session = mock(HttpSession.class);
        when(httpServletRequest.getSession()).thenReturn(session);
        doNothing().when(session).invalidate();

        // When
        ssoFilter.doFilter(httpServletRequest, httpServletResponse,
                filterChain);

        // Then
        verify(filterChain, never()).doFilter(any(HttpServletRequest.class), any(HttpServletResponse.class));
        verify(httpServletRequest).getSession();
        verify(session).invalidate();
        verify(httpServletResponse).sendRedirect("http://localhost:8080/apps/login/index.html?page=http://localhost:9090/context/url/test");
    }

    @Test
    public void testBasicAuthentication() throws IOException, ServletException, NoSuchFieldException, IllegalAccessException {
        // Given
        ConnexoAuthenticationSSOFilter ssoFilter = new ConnexoAuthenticationSSOFilter();
        when(httpServletRequest.getRequestURI()).thenReturn("/context/url/test");
        ssoFilter.init(filterConfig);

        String authorization = "Basic " + Base64.getEncoder().encodeToString("admin admin".getBytes());
        when(httpServletRequest.getHeader("Authorization")).thenReturn(authorization);

        ConnexoRestProxyManager restManager = mock(ConnexoRestProxyManager.class);
        Field restInstance = ConnexoRestProxyManager.class.getDeclaredField("instance");
        restInstance.setAccessible(true);
        restInstance.set(null, restManager);

        ConnexoSecurityTokenManager tokenManager = mock(ConnexoSecurityTokenManager.class);
        Field tokenInstance = ConnexoSecurityTokenManager.class.getDeclaredField("instance");
        tokenInstance.setAccessible(true);
        tokenInstance.set(null, tokenManager);

        String token = "test";
        ConnexoPrincipal principal = new ConnexoPrincipal(1, "TestUser", Arrays.asList("Role1", "Role2"), token);
        when(restManager.getConnexoAuthorizationToken(authorization)).thenReturn(token);
        when(tokenManager.verifyToken(token, true)).thenReturn(principal);

        // When
        ssoFilter.doFilter(httpServletRequest, httpServletResponse,
                filterChain);

        // Then
        verify(filterChain).doFilter(any(ConnexoAuthenticationRequestWrapper.class), eq(httpServletResponse));
    }

    @Test
    public void testCookieTokenValid() throws IOException, ServletException, NoSuchFieldException, IllegalAccessException {
        // Given
        ConnexoAuthenticationSSOFilter ssoFilter = new ConnexoAuthenticationSSOFilter();
        when(httpServletRequest.getRequestURI()).thenReturn("/context/url/test");
        ssoFilter.init(filterConfig);

        String token = "test";
        Cookie[] cookies = new Cookie[1];
        cookies[0] = new Cookie("X-CONNEXO-TOKEN", token);
        when(httpServletRequest.getCookies()).thenReturn(cookies);

        ConnexoSecurityTokenManager tokenManager = mock(ConnexoSecurityTokenManager.class);
        Field tokenInstance = ConnexoSecurityTokenManager.class.getDeclaredField("instance");
        tokenInstance.setAccessible(true);
        tokenInstance.set(null, tokenManager);

        ConnexoPrincipal principal = mock(ConnexoPrincipal.class);
        when(tokenManager.verifyToken(token, true)).thenReturn(principal);
        when(principal.getToken()).thenReturn(token);

        // When
        ssoFilter.doFilter(httpServletRequest, httpServletResponse,
                filterChain);

        // Then
        verify(httpServletResponse, never()).setHeader(eq("X-AUTH-TOKEN"), anyString());
        verify(httpServletResponse, never()).setHeader(eq("Set-Cookie"), anyString());
        verify(filterChain).doFilter(any(ConnexoAuthenticationRequestWrapper.class), eq(httpServletResponse));
    }

    @Test
    public void testHeaderTokenValid() throws IOException, ServletException, NoSuchFieldException, IllegalAccessException {
        // Given
        ConnexoAuthenticationSSOFilter ssoFilter = new ConnexoAuthenticationSSOFilter();
        when(httpServletRequest.getRequestURI()).thenReturn("/context/url/test");
        ssoFilter.init(filterConfig);

        String token = "test";
        when(httpServletRequest.getHeader("Authorization")).thenReturn("Bearer " + token);

        ConnexoSecurityTokenManager tokenManager = mock(ConnexoSecurityTokenManager.class);
        Field tokenInstance = ConnexoSecurityTokenManager.class.getDeclaredField("instance");
        tokenInstance.setAccessible(true);
        tokenInstance.set(null, tokenManager);

        ConnexoPrincipal principal = mock(ConnexoPrincipal.class);
        when(tokenManager.verifyToken(token, true)).thenReturn(principal);
        when(principal.getToken()).thenReturn(token);

        // When
        ssoFilter.doFilter(httpServletRequest, httpServletResponse,
                filterChain);

        // Then
        verify(httpServletResponse, never()).setHeader(eq("X-AUTH-TOKEN"), anyString());
        verify(httpServletResponse, never()).setHeader(eq("Set-Cookie"), anyString());
        verify(filterChain).doFilter(any(ConnexoAuthenticationRequestWrapper.class), eq(httpServletResponse));
    }

    @Test
    public void testCookieTokenRefresh() throws IOException, ServletException, NoSuchFieldException, IllegalAccessException {
        // Given
        ConnexoAuthenticationSSOFilter ssoFilter = new ConnexoAuthenticationSSOFilter();
        when(httpServletRequest.getRequestURI()).thenReturn("/context/url/test");
        ssoFilter.init(filterConfig);

        String oldToken = "oldTest";

        Cookie[] cookies = new Cookie[1];
        cookies[0] = new Cookie("X-CONNEXO-TOKEN", oldToken);
        when(httpServletRequest.getCookies()).thenReturn(cookies);

        ConnexoSecurityTokenManager tokenManager = mock(ConnexoSecurityTokenManager.class);
        Field tokenInstance = ConnexoSecurityTokenManager.class.getDeclaredField("instance");
        tokenInstance.setAccessible(true);
        tokenInstance.set(null, tokenManager);

        String newToken = "newTest";
        ConnexoPrincipal principal = mock(ConnexoPrincipal.class);
        when(tokenManager.verifyToken(oldToken, true)).thenReturn(principal);
        when(principal.getToken()).thenReturn(newToken);

        // When
        ssoFilter.doFilter(httpServletRequest, httpServletResponse,
                filterChain);

        // Then
        verify(httpServletResponse).setHeader("X-AUTH-TOKEN", newToken);
        verify(httpServletResponse).setHeader(eq("Set-Cookie"), anyString());
        verify(filterChain).doFilter(any(ConnexoAuthenticationRequestWrapper.class), eq(httpServletResponse));
    }

    @Test
    public void testHeaderTokenRefresh() throws IOException, ServletException, NoSuchFieldException, IllegalAccessException {
        // Given
        ConnexoAuthenticationSSOFilter ssoFilter = new ConnexoAuthenticationSSOFilter();
        when(httpServletRequest.getRequestURI()).thenReturn("/context/url/test");
        ssoFilter.init(filterConfig);

        String oldToken = "oldTest";
        when(httpServletRequest.getHeader("Authorization")).thenReturn("Bearer " + oldToken);

        ConnexoSecurityTokenManager tokenManager = mock(ConnexoSecurityTokenManager.class);
        Field tokenInstance = ConnexoSecurityTokenManager.class.getDeclaredField("instance");
        tokenInstance.setAccessible(true);
        tokenInstance.set(null, tokenManager);

        String newToken = "newTest";
        ConnexoPrincipal principal = mock(ConnexoPrincipal.class);
        when(tokenManager.verifyToken(oldToken, true)).thenReturn(principal);
        when(principal.getToken()).thenReturn(newToken);

        // When
        ssoFilter.doFilter(httpServletRequest, httpServletResponse,
                filterChain);

        // Then
        verify(httpServletResponse).setHeader("X-AUTH-TOKEN", newToken);
        verify(httpServletResponse).setHeader(eq("Set-Cookie"), anyString());
        verify(filterChain).doFilter(any(ConnexoAuthenticationRequestWrapper.class), eq(httpServletResponse));
    }

    @Test
    public void testNoTokenRefreshOnLongPolling() throws IOException, ServletException, NoSuchFieldException, IllegalAccessException {
        // Given
        ConnexoAuthenticationSSOFilter ssoFilter = new ConnexoAuthenticationSSOFilter();

        when(filterConfig.getInitParameter("unauthorizedPatterns")).thenReturn("/unauthorized/url/*");
        ssoFilter.init(filterConfig);

        when(httpServletRequest.getRequestURI()).thenReturn("/context/unauthorized/url/test");
        when(httpServletRequest.getContextPath()).thenReturn("/context");

        String oldToken = "oldTest";
        when(httpServletRequest.getHeader("Authorization")).thenReturn("Bearer " + oldToken);

        ConnexoSecurityTokenManager tokenManager = mock(ConnexoSecurityTokenManager.class);
        Field tokenInstance = ConnexoSecurityTokenManager.class.getDeclaredField("instance");
        tokenInstance.setAccessible(true);
        tokenInstance.set(null, tokenManager);

        String newToken = "newTest";
        ConnexoPrincipal principal = mock(ConnexoPrincipal.class);
        when(tokenManager.verifyToken(oldToken, false)).thenReturn(principal);
        when(principal.getToken()).thenReturn(oldToken);

        Map<String, String[]> map = new HashMap<>();
        map.put("wait", new String[] {"test"} );
        when(httpServletRequest.getParameterMap()).thenReturn(map);

        // When
        ssoFilter.doFilter(httpServletRequest, httpServletResponse,
                filterChain);

        // Then
        verify(httpServletResponse, never()).setHeader("X-AUTH-TOKEN", newToken);
        verify(httpServletResponse, never()).setHeader(eq("Set-Cookie"), anyString());
        verify(filterChain).doFilter(any(ConnexoAuthenticationRequestWrapper.class), eq(httpServletResponse));
    }

    @Test
    public void testCookieTokenExpired() throws IOException, ServletException, NoSuchFieldException, IllegalAccessException {
        // Given
        ConnexoAuthenticationSSOFilter ssoFilter = new ConnexoAuthenticationSSOFilter();
        when(httpServletRequest.getRequestURI()).thenReturn("/context/url/test");
        ssoFilter.init(filterConfig);

        String token = "test";
        Cookie[] cookies = new Cookie[1];
        cookies[0] = new Cookie("X-CONNEXO-TOKEN", token);
        when(httpServletRequest.getCookies()).thenReturn(cookies);

        ConnexoSecurityTokenManager tokenManager = mock(ConnexoSecurityTokenManager.class);
        Field tokenInstance = ConnexoSecurityTokenManager.class.getDeclaredField("instance");
        tokenInstance.setAccessible(true);
        tokenInstance.set(null, tokenManager);

        when(tokenManager.verifyToken(token, true)).thenReturn(null);

        // When
        ssoFilter.doFilter(httpServletRequest, httpServletResponse,
                filterChain);

        // Then
        verify(httpServletResponse).setHeader(eq("X-AUTH-TOKEN"), eq(null));
        verify(httpServletResponse).setHeader(eq("Set-Cookie"), eq("X-CONNEXO-TOKEN=null; Path=/; Expires=Thu, 01 Jan 1970 00:00:01 GMT; Max-Age=0; HttpOnly"));
        verify(filterChain, never()).doFilter(any(HttpServletRequest.class), any(HttpServletResponse.class));
    }

    @Test
    public void testHeaderTokenExpired() throws IOException, ServletException, NoSuchFieldException, IllegalAccessException {
        // Given
        ConnexoAuthenticationSSOFilter ssoFilter = new ConnexoAuthenticationSSOFilter();
        when(httpServletRequest.getRequestURI()).thenReturn("/context/url/test");
        ssoFilter.init(filterConfig);

        String token = "test";
        when(httpServletRequest.getHeader("Authorization")).thenReturn("Bearer " + token);

        ConnexoSecurityTokenManager tokenManager = mock(ConnexoSecurityTokenManager.class);
        Field tokenInstance = ConnexoSecurityTokenManager.class.getDeclaredField("instance");
        tokenInstance.setAccessible(true);
        tokenInstance.set(null, tokenManager);

        when(tokenManager.verifyToken(token, true)).thenReturn(null);

        // When
        ssoFilter.doFilter(httpServletRequest, httpServletResponse,
                filterChain);

        // Then
        verify(httpServletResponse).setHeader(eq("X-AUTH-TOKEN"), eq(null));
        verify(httpServletResponse).setHeader(eq("Set-Cookie"), eq("X-CONNEXO-TOKEN=null; Path=/; Expires=Thu, 01 Jan 1970 00:00:01 GMT; Max-Age=0; HttpOnly"));
        verify(filterChain, never()).doFilter(any(HttpServletRequest.class), any(HttpServletResponse.class));
    }
}

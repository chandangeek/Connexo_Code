/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.partners.connexo.filters.generic;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by dragos on 2/1/2016.
 */
public class ConnexoAbstractSSOFilterTest {
    @Test
    public void testInitNoConfig() throws ServletException, NoSuchFieldException, IllegalAccessException {
        // Given
        ConnexoTestSSOFilter abstractFilter = new ConnexoTestSSOFilter();
        FilterConfig filterConfig = mock(FilterConfig.class);
        when(filterConfig.getInitParameter("excludePatterns")).thenReturn(null);
        when(filterConfig.getInitParameter("unauthorizedPatterns")).thenReturn(null);

        // When
        abstractFilter.init(filterConfig);

        // Then
        assertEquals(0, abstractFilter.excludedUrls.size());
        assertEquals(0, abstractFilter.unauthorizedUrls.size());
    }

    @Test
    public void testInitWithConfig() throws ServletException, NoSuchFieldException, IllegalAccessException {
        // Given
        ConnexoTestSSOFilter abstractFilter = new ConnexoTestSSOFilter();
        FilterConfig filterConfig = mock(FilterConfig.class);
        when(filterConfig.getInitParameter("excludePatterns")).thenReturn("/test1/*");
        when(filterConfig.getInitParameter("unauthorizedPatterns")).thenReturn("/test2/*;/test3");

        // When
        abstractFilter.init(filterConfig);

        // Then
        assertEquals(1, abstractFilter.excludedUrls.size());
        assertEquals("/test1/.*?", abstractFilter.excludedUrls.get(0));
        assertEquals(2, abstractFilter.unauthorizedUrls.size());
        assertEquals("/test2/.*?", abstractFilter.unauthorizedUrls.get(0));
        assertEquals("/test3", abstractFilter.unauthorizedUrls.get(1));
    }

    @Test
    public void testShouldExclude() {
        // Given
        ConnexoTestSSOFilter abstractFilter = new ConnexoTestSSOFilter();
        abstractFilter.excludedUrls = Arrays.asList("/test1/.*?");

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getContextPath()).thenReturn("/context");
        when(request.getRequestURI()).thenReturn("/context/test1/url");

        // When
        // Then
        assertTrue(abstractFilter.shouldExcludUrl(request));
    }

    @Test
    public void testShouldNotExclude() {
        // Given
        ConnexoTestSSOFilter abstractFilter = new ConnexoTestSSOFilter();
        abstractFilter.excludedUrls = Arrays.asList("/test1/.*?");

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getContextPath()).thenReturn("/context");
        when(request.getRequestURI()).thenReturn("/test2/url");

        // When
        // Then
        assertFalse(abstractFilter.shouldExcludUrl(request));
    }

    @Test
    public void testShouldNotAuthorize() {
        // Given
        ConnexoTestSSOFilter abstractFilter = new ConnexoTestSSOFilter();
        abstractFilter.unauthorizedUrls = Arrays.asList("/test2/.*?", "/test3");

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getContextPath()).thenReturn("/context");
        when(request.getRequestURI()).thenReturn("/context/test2/url");

        // When
        // Then
        assertTrue(abstractFilter.shouldUnauthorize(request));
    }

    @Test
    public void testShouldAuthorize() {
        // Given
        ConnexoTestSSOFilter abstractFilter = new ConnexoTestSSOFilter();
        abstractFilter.unauthorizedUrls = Arrays.asList("/test2/.*?", "/test3");

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getContextPath()).thenReturn("/context");
        when(request.getRequestURI()).thenReturn("/test1/url");

        // When
        // Then
        assertFalse(abstractFilter.shouldUnauthorize(request));
    }

    @Test
    public void testShouldRefreshToken() {
        // Given
        ConnexoTestSSOFilter abstractFilter = new ConnexoTestSSOFilter();
        abstractFilter.unauthorizedUrls = Arrays.asList("/test2/.*?", "/test3");

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getContextPath()).thenReturn("/context");
        when(request.getRequestURI()).thenReturn("/context/test2/url");

        Map<String, String[]> parameterMap = new HashMap<>();
        when(request.getParameterMap()).thenReturn(parameterMap);

        // When
        // Then
        assertTrue(abstractFilter.shouldRefreshToken(request));
    }

    @Test
    public void testShouldNotRefreshToken() {
        // Given
        ConnexoTestSSOFilter abstractFilter = new ConnexoTestSSOFilter();
        abstractFilter.unauthorizedUrls = Arrays.asList("/test2/.*?", "/test3");

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getContextPath()).thenReturn("/context");
        when(request.getRequestURI()).thenReturn("/context/test2/url");

        Map<String, String[]> parameterMap = new HashMap<>();
        parameterMap.put("wait", new String[] {"anything"});
        when(request.getParameterMap()).thenReturn(parameterMap);

        // When
        // Then
        assertFalse(abstractFilter.shouldRefreshToken(request));
    }

    @Test
    public void testGetTokenFromCookie() {
        // Given
        ConnexoTestSSOFilter abstractFilter = new ConnexoTestSSOFilter();

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getCookies()).thenReturn(new Cookie[] {new Cookie("X-CONNEXO-TOKEN", "token")});

        // When
        // Then
        assertEquals("token", abstractFilter.getTokenFromCookie(request));
    }

    @Test
    public void testGetTokenFromAuthorizationHeader() {
        // Given
        ConnexoTestSSOFilter abstractFilter = new ConnexoTestSSOFilter();

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("Authorization")).thenReturn("Bearer token");

        // When
        // Then
        assertEquals("token", abstractFilter.getTokenFromAuthorizationHeader(request));
    }

    @Test
    public void testGetTokenFromBasicAuthentication() throws NoSuchFieldException, IllegalAccessException {
        // Given
        ConnexoTestSSOFilter abstractFilter = new ConnexoTestSSOFilter();

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("Authorization")).thenReturn("Basic auth");

        ConnexoRestProxyManager restManager = mock(ConnexoRestProxyManager.class);
        Field restInstance = ConnexoRestProxyManager.class.getDeclaredField("instance");
        restInstance.setAccessible(true);
        restInstance.set(null, restManager);

        when(restManager.getConnexoAuthorizationToken("Basic auth")).thenReturn("token");

        // When
        // Then
        assertEquals("token", abstractFilter.getTokenFromAuthorizationHeader(request));
    }

    @Test
    public void testUpdateToken() {
        // Given
        ConnexoTestSSOFilter abstractFilter = new ConnexoTestSSOFilter();

        HttpServletResponse response = mock(HttpServletResponse.class);

        // When
        abstractFilter.updateToken(response, "token", 300);

        // Then
        verify(response).setHeader(eq("X-AUTH-TOKEN"), eq("token"));
        verify(response).setHeader(eq("Set-Cookie"), anyString());
    }
}


class ConnexoTestSSOFilter extends ConnexoAbstractSSOFilter{
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {

    }
}
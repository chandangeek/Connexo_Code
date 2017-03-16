/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.partners.connexo.filters.generic;

import javax.servlet.http.HttpServletRequest;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;

/**
 * Created by dragos on 2/1/2016.
 */
@RunWith(MockitoJUnitRunner.class)
public class ConnexoAuthenticationRequestWrapperTest {
    @Mock
    ConnexoPrincipal principal;

    @Mock
    HttpServletRequest request;

    @Test
    public void testGetUserWithNullPrincipal(){
        ConnexoAuthenticationRequestWrapper wrapper = new ConnexoAuthenticationRequestWrapper(null, request, "token");
        assertNull(wrapper.getUserPrincipal());
    }

    @Test
    public void testGetUserWithInvalidPrincipal(){
        ConnexoAuthenticationRequestWrapper wrapper = new ConnexoAuthenticationRequestWrapper(principal, request, "token");
        when(principal.isValid()).thenReturn(false);
        assertNull(wrapper.getUserPrincipal());
    }

    @Test
    public void testGetUserPrincipal(){
        ConnexoAuthenticationRequestWrapper wrapper = new ConnexoAuthenticationRequestWrapper(principal, request, "token");
        when(principal.isValid()).thenReturn(true);
        assertEquals(principal, wrapper.getUserPrincipal());
    }

    @Test
    public void testIsUserInRoleWithNullPrincipal(){
        ConnexoAuthenticationRequestWrapper wrapper = new ConnexoAuthenticationRequestWrapper(null, request, "token");
        assertFalse(wrapper.isUserInRole("Role1"));
    }

    @Test
    public void testIsUserInRoleWithInvalidPrincipal(){
        ConnexoAuthenticationRequestWrapper wrapper = new ConnexoAuthenticationRequestWrapper(principal, request, "token");
        when(principal.isValid()).thenReturn(false);
        assertFalse(wrapper.isUserInRole("Role1"));
    }

    @Test
    public void testIsUserInRole(){
        ConnexoAuthenticationRequestWrapper wrapper = new ConnexoAuthenticationRequestWrapper(principal, request, "token");
        when(principal.isValid()).thenReturn(true);
        when(principal.isUserInRole("Role1")).thenReturn(true);
        assertTrue(wrapper.isUserInRole("Role1"));
    }

    @Test
    public void testGetHeaderNullToken(){
        ConnexoAuthenticationRequestWrapper wrapper = new ConnexoAuthenticationRequestWrapper(principal, request, null);
        when(request.getHeader("Authorization")).thenReturn("TestValue");
        assertEquals("TestValue", wrapper.getHeader("Authorization"));
    }

    @Test
    public void testGetHeaderOriginal(){
        ConnexoAuthenticationRequestWrapper wrapper = new ConnexoAuthenticationRequestWrapper(principal, request, "token");
        when(request.getHeader("TestHeader")).thenReturn("TestValue");
        assertEquals("TestValue", wrapper.getHeader("TestHeader"));
    }

    @Test
    public void testGetHeaderAuthorizationNullOriginal(){
        ConnexoAuthenticationRequestWrapper wrapper = new ConnexoAuthenticationRequestWrapper(principal, request, "token");
        when(request.getHeader("Authorization")).thenReturn(null);
        assertEquals("Bearer token", wrapper.getHeader("Authorization"));
    }

    @Test
    public void testGetHeaderAuthorizationNotBearer(){
        ConnexoAuthenticationRequestWrapper wrapper = new ConnexoAuthenticationRequestWrapper(principal, request, "token");
        when(request.getHeader("Authorization")).thenReturn("TestValue");
        assertEquals("Bearer token", wrapper.getHeader("Authorization"));
    }

    @Test
    public void testGetHeaderAuthorizationBearer(){
        ConnexoAuthenticationRequestWrapper wrapper = new ConnexoAuthenticationRequestWrapper(principal, request, "token");
        when(request.getHeader("Authorization")).thenReturn("Bearer TestValue");
        assertEquals("Bearer TestValue", wrapper.getHeader("Authorization"));
    }
}

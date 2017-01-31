/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.soap.whiteboard.cxf.impl.soap;

import com.elster.jupiter.soap.whiteboard.cxf.InboundEndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.LogLevel;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.users.Group;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;

import org.apache.cxf.common.i18n.Exception;
import org.apache.cxf.common.util.Base64Utility;
import org.apache.cxf.configuration.security.AuthorizationPolicy;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Message;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static junit.framework.TestCase.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by bvn on 6/22/16.
 */
@RunWith(MockitoJUnitRunner.class)
public class AuthenticationInInterceptorTest {

    @Mock
    private TransactionService transactionService;
    @Mock
    private UserService userService;

    private AuthorizationInInterceptor authorizationInInterceptor;
    @Mock
    private TransactionContext context;
    @Mock
    private HttpSession httpSession;
    @Mock
    private AuthorizationPolicy authorizationPolicy;
    @Mock
    private Message message;
    @Mock
    private InboundEndPointConfiguration endPointConfiguration;


    @Before
    public void setUp() throws Exception {
        authorizationInInterceptor = new AuthorizationInInterceptor(userService, transactionService);
        Group developerGroup = mock(Group.class);
        when(developerGroup.getName()).thenReturn("Developer");
        when(userService.findGroup("Developer")).thenReturn(Optional.of(developerGroup));
        User user = mock(User.class);
        when(user.getName()).thenReturn("Admin");
        when(user.isMemberOf(any(Group.class))).thenReturn(false);
        when(user.isMemberOf(developerGroup)).thenReturn(true);
        when(userService.authenticateBase64(anyString(), anyString())).thenReturn(Optional.empty());
        when(userService.authenticateBase64(Base64Utility.encode("admin:admin".getBytes()), "127.0.0.1")).thenReturn(Optional
                .of(user));
        when(endPointConfiguration.getGroup()).thenReturn(Optional.of(developerGroup));
        authorizationInInterceptor.init(endPointConfiguration);
        when(transactionService.getContext()).thenReturn(context);
        HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
        when(message.get("HTTP.REQUEST")).thenReturn(httpServletRequest);
        when(httpServletRequest.getSession()).thenReturn(httpSession);
        when(httpServletRequest.getRemoteAddr()).thenReturn("127.0.0.1");
        when(message.get(AuthorizationPolicy.class)).thenReturn(authorizationPolicy);
    }

    @Test
    public void testNormalAuthenticationNoSession() throws Exception {

        when(httpSession.getAttribute("userName")).thenReturn(null);
        when(httpSession.getAttribute("password")).thenReturn(null);
        when(authorizationPolicy.getUserName()).thenReturn("admin");
        when(authorizationPolicy.getPassword()).thenReturn("admin");

        authorizationInInterceptor.handleMessage(message);
        verify(endPointConfiguration, never()).log(any(LogLevel.class), anyString());
        verify(endPointConfiguration, never()).log(anyString(), any(Exception.class));
    }

    @Test
    public void testAuthenticationWrongPassword() throws Exception {

        when(httpSession.getAttribute("userName")).thenReturn(null);
        when(httpSession.getAttribute("password")).thenReturn(null);
        when(authorizationPolicy.getUserName()).thenReturn("admin");
        when(authorizationPolicy.getPassword()).thenReturn("wrong");

        try {
            authorizationInInterceptor.handleMessage(message);
            fail("Expected security exception");
        } catch (Fault se) {
            // This page left blank intentionally
        }
        verify(endPointConfiguration).log(LogLevel.WARNING, "User admin denied access: invalid credentials");
        verify(endPointConfiguration, never()).log(anyString(), any(Exception.class));
        verify(context, atLeastOnce()).commit();
    }

    @Test
    public void testAuthenticationNotInRole() throws Exception {
        Group special = mock(Group.class);
        when(endPointConfiguration.getGroup()).thenReturn(Optional.of(special));

        when(httpSession.getAttribute("userName")).thenReturn(null);
        when(httpSession.getAttribute("password")).thenReturn(null);
        when(authorizationPolicy.getUserName()).thenReturn("admin");
        when(authorizationPolicy.getPassword()).thenReturn("admin");

        try {
            authorizationInInterceptor.handleMessage(message);
            fail("Expected security exception");
        } catch (Fault se) {
            // This page left blank intentionally
        }
        verify(endPointConfiguration).log(LogLevel.WARNING, "User admin denied access: not in role");
        verify(endPointConfiguration, never()).log(anyString(), any(Exception.class));
        verify(context, atLeastOnce()).commit();
    }

    @Test
    public void testAuthenticationNoRole() throws Exception {
        when(endPointConfiguration.getGroup()).thenReturn(Optional.empty());

        when(httpSession.getAttribute("userName")).thenReturn(null);
        when(httpSession.getAttribute("password")).thenReturn(null);
        when(authorizationPolicy.getUserName()).thenReturn("admin");
        when(authorizationPolicy.getPassword()).thenReturn("admin");

        authorizationInInterceptor.handleMessage(message);
        verify(endPointConfiguration, never()).log(any(LogLevel.class), anyString());
        verify(endPointConfiguration, never()).log(anyString(), any(Exception.class));
    }

    @Test
    public void testAuthenticationNoRoleWrongPassword() throws Exception {
        when(endPointConfiguration.getGroup()).thenReturn(Optional.empty());

        when(httpSession.getAttribute("userName")).thenReturn(null);
        when(httpSession.getAttribute("password")).thenReturn(null);
        when(authorizationPolicy.getUserName()).thenReturn("admin");
        when(authorizationPolicy.getPassword()).thenReturn("wrong");

        try {
            authorizationInInterceptor.handleMessage(message);
            fail("Expected security exception");
        } catch (Fault se) {
            // This page left blank intentionally
        }
        verify(endPointConfiguration).log(LogLevel.WARNING, "User admin denied access: invalid credentials");
        verify(endPointConfiguration, never()).log(anyString(), any(Exception.class));
        verify(context, atLeastOnce()).commit();
    }

    @Test
    public void testAuthenticationExceptionOccurs() throws Exception {
        RuntimeException toBeThrown = new RuntimeException();
        doThrow(toBeThrown).when(userService).authenticateBase64(anyString(), anyString());

        when(httpSession.getAttribute("userName")).thenReturn(null);
        when(httpSession.getAttribute("password")).thenReturn(null);
        when(authorizationPolicy.getUserName()).thenReturn("admin");
        when(authorizationPolicy.getPassword()).thenReturn("admin");

        try {
            authorizationInInterceptor.handleMessage(message);
            fail("Expected security exception");
        } catch (Fault se) {
            // This page left blank intentionally
        }
        verify(endPointConfiguration).log("Exception while logging in admin:", toBeThrown);
        verify(context, atLeastOnce()).commit();
    }


}

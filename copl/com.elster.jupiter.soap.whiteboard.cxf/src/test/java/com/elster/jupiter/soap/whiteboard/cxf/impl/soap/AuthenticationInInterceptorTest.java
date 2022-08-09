/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.soap.whiteboard.cxf.impl.soap;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.soap.whiteboard.cxf.InboundEndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.LogLevel;
import com.elster.jupiter.soap.whiteboard.cxf.WebServicesService;
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

import static com.elster.jupiter.soap.whiteboard.cxf.WebServiceCallOccurrence.MESSAGE_CONTEXT_OCCURRENCE_ID;
import static junit.framework.TestCase.fail;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
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
    private WebServicesService webServicesService;
    @Mock
    private UserService userService;
    @Mock
    private HttpSession httpSession;
    @Mock
    private AuthorizationPolicy authorizationPolicy;
    @Mock
    private Message message;
    @Mock
    private InboundEndPointConfiguration endPointConfiguration;
    @Mock
    private ThreadPrincipalService threadPrincipalService;
    @Mock
    private EventService eventService;
    @Mock
    private User user;

    private AuthorizationInInterceptor authorizationInInterceptor;

    @Before
    public void setUp() throws Exception {
        authorizationInInterceptor = new AuthorizationInInterceptor(userService, webServicesService, threadPrincipalService, eventService);
        Group developerGroup = mock(Group.class);
        when(developerGroup.getName()).thenReturn("Developer");
        when(developerGroup.hasPrivilege(anyString(),anyString())).thenReturn(true);
        when(userService.findGroup("Developer")).thenReturn(Optional.of(developerGroup));
        when(user.getName()).thenReturn("Admin");
        when(user.isMemberOf(any(Group.class))).thenReturn(false);
        when(user.isMemberOf(developerGroup)).thenReturn(true);
        when(userService.authenticateBase64(anyString(), anyString())).thenReturn(Optional.empty());
        when(userService.authenticateBase64(Base64Utility.encode("admin:admin".getBytes()), "127.0.0.1")).thenReturn(Optional
                .of(user));
        when(endPointConfiguration.getGroup()).thenReturn(Optional.of(developerGroup));
        authorizationInInterceptor.init(endPointConfiguration);
        HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
        when(message.get("HTTP.REQUEST")).thenReturn(httpServletRequest);
        when(httpServletRequest.getSession()).thenReturn(httpSession);
        when(httpServletRequest.getRemoteAddr()).thenReturn("127.0.0.1");
        when(message.get(AuthorizationPolicy.class)).thenReturn(authorizationPolicy);
        when(userService.findUser(anyString())).thenReturn(Optional.empty());
        when(message.get(MESSAGE_CONTEXT_OCCURRENCE_ID)).thenReturn(1l);
        when(user.hasPrivilege(anyString(), anyString())).thenReturn(true);
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
            assertThat(se.getMessage(), is("Not authorized"));
            verify(webServicesService).failOccurrence(1l, "User admin denied access: invalid credentials");
        }
        verify(endPointConfiguration, never()).log(anyString(), any(Exception.class));
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
            assertThat(se.getMessage(), is("Not authorized"));
            verify(webServicesService).failOccurrence(1l, "User admin denied access: not in role");
        }
        verify(endPointConfiguration, never()).log(anyString(), any(Exception.class));
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
            assertThat(se.getMessage(), is("Not authorized"));
            verify(webServicesService).failOccurrence(1l, "User admin denied access: invalid credentials");
        }
        verify(endPointConfiguration, never()).log(anyString(), any(Exception.class));
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
            assertThat(se.getMessage(), is("Not authorized"));
            verify(webServicesService).failOccurrence(any(Long.class), any(Exception.class));
        }
    }

    @Test
    public void testAuthenticationNoPrivilege() throws Exception {
        when(endPointConfiguration.getGroup()).thenReturn(Optional.empty());

        when(httpSession.getAttribute("userName")).thenReturn(null);
        when(httpSession.getAttribute("password")).thenReturn(null);
        when(authorizationPolicy.getUserName()).thenReturn("admin");
        when(authorizationPolicy.getPassword()).thenReturn("admin");
        when(user.hasPrivilege(anyString(), anyString())).thenReturn(false);

        try {
            authorizationInInterceptor.handleMessage(message);
            fail("Expected security exception");
        } catch (Fault se) {
            // This page left blank intentionally
        }
        verify(endPointConfiguration).log(LogLevel.WARNING, "User admin denied access: no privileges");
    }
}

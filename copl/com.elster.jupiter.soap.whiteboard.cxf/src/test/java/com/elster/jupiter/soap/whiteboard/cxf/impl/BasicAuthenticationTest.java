/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.soap.whiteboard.cxf.impl;

import com.elster.jupiter.soap.whiteboard.cxf.InboundEndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.impl.rest.BasicAuthentication;
import com.elster.jupiter.users.Group;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;

import org.apache.cxf.common.util.Base64Utility;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class BasicAuthenticationTest {
    @Mock
    private UserService userService;
    @Mock
    private User user;
    @Mock
    InboundEndPointConfiguration endPointConfiguration;

    private BasicAuthentication basicAuthentication;
    private HttpServletRequest httpServletRequest;
    private HttpServletResponse httpServletResponse;


    @Before
    public void setUp() throws Exception {
        basicAuthentication = new BasicAuthentication(userService).init(endPointConfiguration);
        httpServletRequest = mock(HttpServletRequest.class);
        when(httpServletRequest.getRemoteAddr()).thenReturn("1.1.1.1");
        httpServletResponse = mock(HttpServletResponse.class);
        when(userService.authenticateBase64(anyString(), anyString())).thenReturn(Optional.empty());
        when(userService.authenticateBase64(Base64Utility.encode("admin:admin".getBytes()), "1.1.1.1")).thenReturn(Optional
                .of(user));
        when(user.getGroups()).thenReturn(Collections.emptyList());

    }

    @Test
    public void testPlainLoginNoGroup() throws Exception {
        when(endPointConfiguration.getGroup()).thenReturn(Optional.empty());
        when(httpServletRequest.getHeader("Authorization")).thenReturn("Basic " + Base64Utility.encode("admin:admin".getBytes()));

        boolean success = basicAuthentication.handleSecurity(httpServletRequest, httpServletResponse);
        assertThat(success).isTrue();
    }

    @Test
    public void testWrongPassword() throws Exception {
        when(endPointConfiguration.getGroup()).thenReturn(Optional.empty());
        when(httpServletRequest.getHeader("Authorization")).thenReturn("Basic " + Base64Utility.encode("admin:wrong".getBytes()));

        boolean success = basicAuthentication.handleSecurity(httpServletRequest, httpServletResponse);
        assertThat(success).isFalse();
    }

    @Test
    public void testNoAuthentication() throws Exception {
        when(endPointConfiguration.getGroup()).thenReturn(Optional.empty());
        when(httpServletRequest.getHeader("Authorization")).thenReturn(null);

        boolean success = basicAuthentication.handleSecurity(httpServletRequest, httpServletResponse);
        assertThat(success).isFalse();
    }

    @Test
    public void testWrongGroup() throws Exception {
        Group group1 = mock(Group.class);
        Group group2 = mock(Group.class);
        Group group3 = mock(Group.class);
        when(user.getGroups()).thenReturn(Arrays.asList(group1, group2));
        when(endPointConfiguration.getGroup()).thenReturn(Optional.of(group3));
        when(httpServletRequest.getHeader("Authorization")).thenReturn("Basic " + Base64Utility.encode("admin:admin".getBytes()));

        boolean success = basicAuthentication.handleSecurity(httpServletRequest, httpServletResponse);
        assertThat(success).isFalse();
    }

    @Test
    public void testCorrectGroup() throws Exception {
        Group group1 = mock(Group.class);
        Group group2 = mock(Group.class);
        when(user.getGroups()).thenReturn(Arrays.asList(group1, group2));
        when(endPointConfiguration.getGroup()).thenReturn(Optional.of(group2));
        when(httpServletRequest.getHeader("Authorization")).thenReturn("Basic " + Base64Utility.encode("admin:admin".getBytes()));

        boolean success = basicAuthentication.handleSecurity(httpServletRequest, httpServletResponse);
        assertThat(success).isTrue();
    }
}

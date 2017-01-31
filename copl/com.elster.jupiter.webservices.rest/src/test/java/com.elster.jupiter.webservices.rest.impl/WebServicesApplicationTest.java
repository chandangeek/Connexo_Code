/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.webservices.rest.impl;

import com.elster.jupiter.devtools.rest.FelixRestApplicationJerseyTest;
import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.domain.util.QueryParameters;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointAuthentication;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfigurationService;
import com.elster.jupiter.soap.whiteboard.cxf.InboundEndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.LogLevel;
import com.elster.jupiter.soap.whiteboard.cxf.OutboundEndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.WebServicesService;
import com.elster.jupiter.users.Group;
import com.elster.jupiter.users.UserService;

import javax.ws.rs.core.Application;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.mockito.Mock;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by bvn on 6/8/16.
 */
public class WebServicesApplicationTest extends FelixRestApplicationJerseyTest {
    @Mock
    EndPointConfigurationService endPointConfigurationService;
    @Mock
    WebServicesService webServicesService;
    @Mock
    UserService userService;

    @Override
    protected Application getApplication() {
        WebServicesApplication webServicesApplication = new WebServicesApplication();
        webServicesApplication.setNlsService(nlsService);
        webServicesApplication.setTransactionService(transactionService);
        webServicesApplication.setEndPointConfigurationService(endPointConfigurationService);
        webServicesApplication.setWebServicesService(webServicesService);
        webServicesApplication.setUserService(userService);
        return webServicesApplication;
    }

    <T> Finder<T> mockFinder(List<T> list) {
        Finder<T> finder = mock(Finder.class);

        when(finder.paged(anyInt(), anyInt())).thenReturn(finder);
        when(finder.sorted(anyString(), any(Boolean.class))).thenReturn(finder);
        when(finder.from(any(QueryParameters.class))).thenReturn(finder);
        when(finder.find()).thenReturn(list);
        when(finder.stream()).thenReturn(list.stream());
        return finder;
    }

    InboundEndPointConfiguration mockInboundEndPointConfig(long id, long version, String name, String url, String webServiceName) {
        InboundEndPointConfiguration mock = mock(InboundEndPointConfiguration.class);
        when(mock.getId()).thenReturn(id);
        when(mock.getName()).thenReturn(name);
        when(mock.getLogLevel()).thenReturn(LogLevel.INFO);
        when(mock.getUrl()).thenReturn(url);
        when(mock.getVersion()).thenReturn(version);
        when(mock.getWebServiceName()).thenReturn(webServiceName);
        Finder logFinder = mockFinder(Collections.emptyList());
        when(mock.getLogs()).thenReturn(logFinder);
        when(mock.getAuthenticationMethod()).thenReturn(EndPointAuthentication.NONE);
        when(mock.isActive()).thenReturn(true);
        when(mock.isHttpCompression()).thenReturn(true);
        when(mock.isSchemaValidation()).thenReturn(true);
        Group group = mock(Group.class);
        when(group.getName()).thenReturn("Developer");
        when(mock.getGroup()).thenReturn(Optional.of(group));
        when(mock.isTracing()).thenReturn(true);
        when(mock.getTraceFile()).thenReturn("webservices.log");
        when(mock.isInbound()).thenReturn(true);
        when(endPointConfigurationService.getEndPointConfiguration(name)).thenReturn(Optional.of(mock));
        when(endPointConfigurationService.getEndPointConfiguration(id)).thenReturn(Optional.of(mock));
        when(endPointConfigurationService.findAndLockEndPointConfigurationByIdAndVersion(id, version)).thenReturn(Optional
                .of(mock));
        return mock;
    }

    OutboundEndPointConfiguration mockOutboundEndPointConfig(long id, long version, String name, String url, String webServiceName) {
        OutboundEndPointConfiguration mock = mock(OutboundEndPointConfiguration.class);
        when(mock.getId()).thenReturn(id);
        when(mock.getName()).thenReturn(name);
        when(mock.getLogLevel()).thenReturn(LogLevel.INFO);
        when(mock.getUrl()).thenReturn(url);
        when(mock.getVersion()).thenReturn(version);
        when(mock.getWebServiceName()).thenReturn(webServiceName);
        Finder logFinder = mockFinder(Collections.emptyList());
        when(mock.getLogs()).thenReturn(logFinder);
        when(mock.getAuthenticationMethod()).thenReturn(EndPointAuthentication.NONE);
        when(mock.isActive()).thenReturn(true);
        when(mock.isHttpCompression()).thenReturn(true);
        when(mock.isSchemaValidation()).thenReturn(true);
        when(mock.isTracing()).thenReturn(true);
        when(mock.getTraceFile()).thenReturn("webservices.log");
        when(mock.getPassword()).thenReturn("password");
        when(mock.getUsername()).thenReturn("username");
        when(mock.isInbound()).thenReturn(false);
        when(endPointConfigurationService.getEndPointConfiguration(name)).thenReturn(Optional.of(mock));
        when(endPointConfigurationService.getEndPointConfiguration(id)).thenReturn(Optional.of(mock));
        when(endPointConfigurationService.findAndLockEndPointConfigurationByIdAndVersion(id, version)).thenReturn(Optional
                .of(mock));
        return mock;
    }

}

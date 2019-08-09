/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.web.queryapi;

import com.energyict.mdc.engine.config.OnlineComServer;
import com.energyict.mdc.engine.impl.core.RunningComServer;
import com.energyict.mdc.engine.impl.core.RunningComServerImpl;
import com.energyict.mdc.engine.impl.core.RunningOnlineComServer;

import com.energyict.mdc.engine.impl.monitor.ComServerMonitorImplMBean;
import com.energyict.mdc.engine.impl.monitor.ManagementBeanFactory;
import com.energyict.mdc.engine.impl.monitor.ServerEventAPIStatistics;
import com.energyict.mdc.engine.monitor.ComServerMonitor;
import com.energyict.mdc.engine.monitor.QueryAPIStatistics;
import org.fest.assertions.api.Assertions;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link QueryApiServlet} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-04-10 (16:18)
 */
@RunWith(MockitoJUnitRunner.class)
public class QueryApiServletTest {

    @Mock
    private WebSocketQueryApiService service;
    @Mock
    private OnlineComServer comServer;
    @Mock
    private RunningOnlineComServer runningComServer;
    @Mock
    private ManagementBeanFactory managementBeanFactory;
    @Mock(extraInterfaces = ComServerMonitor.class)
    private ComServerMonitorImplMBean comServerMonitor;
    @Mock
    private QueryAPIStatistics queryAPIStatistics;
    @Mock
    private RunningComServerImpl.ServiceProvider serviceProvider;

    @Before
    public void initializeMockAndFactories () {
        when(this.runningComServer.getComServer()).thenReturn(this.comServer);
        when(this.runningComServer.newWebSocketQueryApiService()).thenReturn(this.service);
    }

    @Test
    public void testNewWebSocketQueryApiServiceIsCreatedForNewSession () {
        HttpSession httpSession = mock(HttpSession.class);
        when(httpSession.getId()).thenReturn("testNewWebSocketQueryApiServiceIsCreatedForNewSession");
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getSession()).thenReturn(httpSession);
        when(request.getSession(anyBoolean())).thenReturn(httpSession);
        when(this.serviceProvider.managementBeanFactory()).thenReturn(this.managementBeanFactory);
        when(this.managementBeanFactory.findOrCreateFor(any(RunningComServer.class))).thenReturn(this.comServerMonitor);
        ComServerMonitor comServerMonitor = (ComServerMonitor) this.comServerMonitor;
        when(comServerMonitor.getQueryApiStatistics()).thenReturn(this.queryAPIStatistics);
        QueryApiServlet servlet = new QueryApiServlet(this.runningComServer, queryAPIStatistics);

        // Business method
        servlet.findOrCreateQueryApiService(request);    // Don't care about the protocol

        // Asserts
        verify(this.runningComServer).newWebSocketQueryApiService();
    }

    @Test
    public void testNewWebSocketQueryApiServiceIsNotCreatedForTheSameSession () {
        HttpSession httpSession = mock(HttpSession.class);
        when(httpSession.getId()).thenReturn("testNewWebSocketQueryApiServiceIsNotCreatedForTheSameSession");
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getSession()).thenReturn(httpSession);
        when(request.getSession(anyBoolean())).thenReturn(httpSession);
        QueryApiServlet initialsServlet = new QueryApiServlet(this.runningComServer, queryAPIStatistics);
        initialsServlet.findOrCreateQueryApiService(request);
        reset(this.runningComServer);

        // Business method
        QueryApiServlet servlet = new QueryApiServlet(this.runningComServer, queryAPIStatistics);
        servlet.findOrCreateQueryApiService(request);

        // Asserts
        verify(this.runningComServer, never()).newWebSocketQueryApiService();
        Assertions.assertThat(servlet).isSameAs(servlet);
    }

}
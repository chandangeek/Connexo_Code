/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.web.queryapi;

import com.elster.jupiter.transaction.TransactionService;
import com.energyict.mdc.common.comserver.OnlineComServer;
import com.energyict.mdc.device.data.tasks.CommunicationTaskService;
import com.energyict.mdc.device.data.tasks.ConnectionTaskService;
import com.energyict.mdc.engine.config.EngineConfigurationService;
import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.engine.impl.core.RunningComServerImpl;
import com.energyict.mdc.engine.impl.core.RunningOnlineComServer;
import com.energyict.mdc.engine.monitor.QueryAPIStatistics;

import org.eclipse.jetty.websocket.servlet.ServletUpgradeRequest;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeResponse;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
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

    private QueryAPIStatistics statistics;
    private WebSocketQueryApiCreator servlet;

    @Mock
    private WebSocketQueryApiService service;
    @Mock
    private OnlineComServer comServer;
    @Mock
    private RunningOnlineComServer runningComServer;
    @Mock
    private QueryAPIStatistics queryAPIStatistics;
    @Mock
    private ServletUpgradeRequest servletUpgradeRequest;
    @Mock
    private ServletUpgradeResponse servletUpgradeResponse;
    @Mock
    private HttpServletRequest httpServletRequest;
    @Mock
    private HttpSession httpSession;
    @Mock
    private ComServerDAO comServerDAO;
    @Mock
    private RunningComServerImpl.ServiceProvider serviceProvider;
    @Mock
    private WebSocketQueryApiServiceFactory serviceFactory;
    @Mock
    private EngineConfigurationService engineConfigurationService;
    @Mock
    private ConnectionTaskService connectionTaskService;
    @Mock
    private CommunicationTaskService communicationTaskService;
    @Mock
    private TransactionService transactionService;



    @Before
    public void initializeMockAndFactories () {
        when(this.runningComServer.getComServer()).thenReturn(this.comServer);

        servlet = new WebSocketQueryApiCreator(runningComServer, comServerDAO, engineConfigurationService, connectionTaskService, communicationTaskService, transactionService);
        WebSocketQueryApiServiceFactory.setInstance(this.serviceFactory);
        when(this.serviceFactory.newWebSocketQueryApiService(any(RunningOnlineComServer.class), any(ComServerDAO.class), any(EngineConfigurationService.class), any(ConnectionTaskService.class), any(CommunicationTaskService.class), any(TransactionService.class))).thenReturn(this.service);
        when(servletUpgradeRequest.getHttpServletRequest()).thenReturn(httpServletRequest);
        when(httpServletRequest.getSession()).thenReturn(httpSession);
        when(httpServletRequest.getSession(anyBoolean())).thenReturn(httpSession);
        //when(this.runningComServer.newWebSocketQueryApiService()).thenReturn(this.service);


    }

    @After
    public void resetFactory () {
        WebSocketQueryApiServiceFactory.setInstance(null);
    }

    @Test
    public void testNewWebSocketQueryApiServiceIsCreatedForNewSession () {
        when(httpSession.getId()).thenReturn("testNewWebSocketQueryApiServiceIsCreatedForNewSession");
        // Business method
        servlet.createWebSocket(servletUpgradeRequest, servletUpgradeResponse);

        // Asserts
        verify(this.serviceFactory).newWebSocketQueryApiService(runningComServer, comServerDAO, engineConfigurationService, connectionTaskService, communicationTaskService, transactionService);
    }

    @Test
    public void testNewWebSocketQueryApiServiceIsNotCreatedForTheSameSession () {
        when(httpSession.getId()).thenReturn("testNewWebSocketQueryApiServiceIsNotCreatedForTheSameSession");

        WebSocketQueryApiService initialWebSocket = (WebSocketQueryApiService) servlet.createWebSocket(servletUpgradeRequest, servletUpgradeResponse);
        reset(this.serviceFactory);

        // Business method
        WebSocketQueryApiService webSocket = (WebSocketQueryApiService) servlet.createWebSocket(servletUpgradeRequest, servletUpgradeResponse);

        // Asserts
        verify(this.serviceFactory, never()).newWebSocketQueryApiService(runningComServer, comServerDAO, engineConfigurationService, connectionTaskService, communicationTaskService, transactionService);
        assertThat(webSocket).isSameAs(initialWebSocket);
    }

}
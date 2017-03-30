/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.web.queryapi;

import com.energyict.mdc.engine.config.OnlineComServer;
import com.energyict.mdc.engine.impl.core.RunningOnlineComServer;

import org.eclipse.jetty.websocket.WebSocket;
import org.fest.assertions.api.Assertions;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

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
        QueryApiServlet servlet = new QueryApiServlet(this.runningComServer);

        // Business method
        servlet.doWebSocketConnect(request, "http");    // Don't care about the protocol

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
        QueryApiServlet servlet = new QueryApiServlet(this.runningComServer);
        WebSocket initialWebSocket = servlet.doWebSocketConnect(request, "http");// Don't care about the protocol
        reset(this.runningComServer);

        // Business method
        WebSocket webSocket = servlet.doWebSocketConnect(request, "http");// Don't care about the protocol

        // Asserts
        verify(this.runningComServer, never()).newWebSocketQueryApiService();
        Assertions.assertThat(webSocket).isSameAs(initialWebSocket);
    }

}
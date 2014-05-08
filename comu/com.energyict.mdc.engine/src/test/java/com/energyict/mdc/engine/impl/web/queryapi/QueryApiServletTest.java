package com.energyict.mdc.engine.impl.web.queryapi;

import com.energyict.mdc.engine.model.OnlineComServer;

import org.eclipse.jetty.websocket.WebSocket;
import org.fest.assertions.api.Assertions;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.junit.*;
import org.junit.runner.*;
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
    private WebSocketQueryApiServiceFactory serviceFactory;
    @Mock
    private WebSocketQueryApiService service;

    @Before
    public void initializeMockAndFactories () {
        WebSocketQueryApiServiceFactory.setInstance(this.serviceFactory);
        when(this.serviceFactory.newWebSocketQueryApiService(any(OnlineComServer.class))).thenReturn(this.service);
    }

    @After
    public void resetFactory () {
        WebSocketQueryApiServiceFactory.setInstance(null);
    }

    @Test
    public void testNewWebSocketQueryApiServiceIsCreatedForNewSession () {
        HttpSession httpSession = mock(HttpSession.class);
        when(httpSession.getId()).thenReturn("testNewWebSocketQueryApiServiceIsCreatedForNewSession");
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getSession()).thenReturn(httpSession);
        when(request.getSession(anyBoolean())).thenReturn(httpSession);
        OnlineComServer comServer = mock(OnlineComServer.class);
        QueryApiServlet servlet = new QueryApiServlet(comServer);

        // Business method
        servlet.doWebSocketConnect(request, "http");    // Don't care about the protocol

        // Asserts
        verify(this.serviceFactory).newWebSocketQueryApiService(comServer);
    }

    @Test
    public void testNewWebSocketQueryApiServiceIsNotCreatedForTheSameSession () {
        HttpSession httpSession = mock(HttpSession.class);
        when(httpSession.getId()).thenReturn("testNewWebSocketQueryApiServiceIsNotCreatedForTheSameSession");
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getSession()).thenReturn(httpSession);
        when(request.getSession(anyBoolean())).thenReturn(httpSession);
        OnlineComServer comServer = mock(OnlineComServer.class);
        QueryApiServlet servlet = new QueryApiServlet(comServer);
        WebSocket initialWebSocket = servlet.doWebSocketConnect(request, "http");// Don't care about the protocol
        reset(this.serviceFactory);

        // Business method
        WebSocket webSocket = servlet.doWebSocketConnect(request, "http");// Don't care about the protocol

        // Asserts
        verify(this.serviceFactory, never()).newWebSocketQueryApiService(comServer);
        Assertions.assertThat(webSocket).isSameAs(initialWebSocket);
    }

}
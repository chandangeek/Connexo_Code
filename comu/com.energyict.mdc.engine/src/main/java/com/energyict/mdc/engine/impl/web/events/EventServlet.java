package com.energyict.mdc.engine.impl.web.events;

import org.eclipse.jetty.websocket.WebSocket;
import org.eclipse.jetty.websocket.WebSocketServlet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

/**
 * Provides a servlet implementation for the ComServer event mechanism.
 * All connections will go through WebSockets to enable
 * push notifications whenever a new event occurred in the ComServer.
 * Every new client will have its dedicated component
 * that pushes the events through the established WebSocket.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-11-02 (16:30)
 */
public class EventServlet extends WebSocketServlet {

    private Map<String, WebSocketEventPublisher> eventPublishers = new HashMap<>();

    @Override
    public WebSocket doWebSocketConnect (HttpServletRequest request, String protocol) {
        return this.findOrCreateEventPublisher(request);
    }

    private WebSocketEventPublisher findOrCreateEventPublisher (HttpServletRequest request) {
        HttpSession httpSession = request.getSession(true);
        String httpSessionId = httpSession.getId();
        WebSocketEventPublisher eventPublisher = this.eventPublishers.get(httpSessionId);
        if (eventPublisher == null) {
            eventPublisher = WebSocketEventPublisherFactory.getInstance().newWebSocketEventPublisher();
            this.eventPublishers.put(httpSessionId, eventPublisher);
        }
        return eventPublisher;
    }

}
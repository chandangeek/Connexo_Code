package com.energyict.mdc.engine.impl.web.events;

import com.energyict.mdc.engine.monitor.EventAPIStatistics;
import org.eclipse.jetty.websocket.servlet.WebSocketServlet;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;

/**
 * Provides a servlet implementation for the ComServer event mechanism.
 * All connections will go through WebSockets to enable
 * push notifications whenever a new event occurred in the ComServer.
 * Every new client will have its dedicated component
 * that pushes the events through the established WebSocket.
 *
 * @author Rudi Vankeirsbilck (rudi), geert
 * @since 2012-11-02 (16:30)
 */
public class EventServlet extends WebSocketServlet {
    private EventAPIStatistics statistics;
    private WebSocketEventPublisherFactory webSocketEventPublisherFactory;

    public EventServlet(WebSocketEventPublisherFactory webSocketEventPublisherFactory, EventAPIStatistics statistics) {
        this.statistics = statistics;
        this.webSocketEventPublisherFactory = webSocketEventPublisherFactory;
    }

    @Override
    public void configure(WebSocketServletFactory webSocketServletFactory) {
        webSocketServletFactory.setCreator(new EventServletCreator(webSocketEventPublisherFactory, statistics));
    }

}
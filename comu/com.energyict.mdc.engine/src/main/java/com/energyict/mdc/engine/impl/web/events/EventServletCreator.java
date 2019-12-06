package com.energyict.mdc.engine.impl.web.events;

import com.energyict.mdc.engine.monitor.EventAPIStatistics;

import org.eclipse.jetty.websocket.api.WebSocketListener;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeRequest;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeResponse;
import org.eclipse.jetty.websocket.servlet.WebSocketCreator;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * ported from eiserver for jetty upgrade
 */
public class EventServletCreator implements WebSocketCreator, WebSocketCloseEventListener {
    private EventAPIStatistics statistics;
    private WebSocketEventPublisherFactory webSocketEventPublisherFactory;
    private List<WebSocketEventPublisher> eventPublishers = new ArrayList<>();

    public EventServletCreator(WebSocketEventPublisherFactory webSocketEventPublisherFactory, EventAPIStatistics statistics) {
        this.statistics = statistics;
        this.webSocketEventPublisherFactory = webSocketEventPublisherFactory;
    }

    @Override
    public Object createWebSocket(ServletUpgradeRequest servletUpgradeRequest, ServletUpgradeResponse servletUpgradeResponse) {
        WebSocketEventPublisher newEventPublisher = webSocketEventPublisherFactory.newWebSocketEventPublisher(this);
        eventPublishers.add(newEventPublisher);
        return newEventPublisher;
    }


    @Override
    public void closedFrom(WebSocketEventPublisher webSocketEventPublisher) {
        eventPublishers.remove(webSocketEventPublisher);
    }
}

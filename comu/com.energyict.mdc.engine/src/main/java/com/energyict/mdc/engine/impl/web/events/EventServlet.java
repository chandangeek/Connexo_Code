/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.web.events;

import org.eclipse.jetty.websocket.WebSocket;
import org.eclipse.jetty.websocket.WebSocketServlet;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

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
public class EventServlet extends WebSocketServlet implements WebSocketCloseEventListener {

    private final WebSocketEventPublisherFactory webSocketEventPublisherFactory;
    private List<WebSocketEventPublisher> eventPublishers = new ArrayList<>();

    public EventServlet(WebSocketEventPublisherFactory webSocketEventPublisherFactory) {
        super();
        this.webSocketEventPublisherFactory = webSocketEventPublisherFactory;
    }

    @Override
    public WebSocket doWebSocketConnect (HttpServletRequest request, String protocol) {
        WebSocketEventPublisher newEventPublisher = this.webSocketEventPublisherFactory.newWebSocketEventPublisher(this);
        this.eventPublishers.add(newEventPublisher);
        return newEventPublisher;
    }

    @Override
    public void closedFrom(WebSocketEventPublisher webSocketEventPublisher) {
        this.eventPublishers.remove(webSocketEventPublisher);
    }

}
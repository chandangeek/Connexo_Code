package com.energyict.mdc.engine.impl.web.events;

import com.energyict.mdc.engine.monitor.EventAPIStatistics;

import org.eclipse.jetty.websocket.servlet.ServletUpgradeRequest;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeResponse;
import org.eclipse.jetty.websocket.servlet.WebSocketCreator;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by H216758 on 5/30/2017.
 */
public class EventServletCreator implements WebSocketCreator {
    private EventAPIStatistics statistics;
    private List<WebSocketEventPublisher> eventPublishers = new ArrayList<>();

    public EventServletCreator(EventAPIStatistics statistics) {
        this.statistics = statistics;
    }

    @Override
    public Object createWebSocket(ServletUpgradeRequest servletUpgradeRequest, ServletUpgradeResponse servletUpgradeResponse) {
        WebSocketEventPublisher newEventPublisher = null;
        eventPublishers.add(newEventPublisher);
        cleanUpClosedPublishers();

        return newEventPublisher;
    }

    private void cleanUpClosedPublishers() {
        for (Iterator<WebSocketEventPublisher> it = this.eventPublishers.iterator(); it.hasNext(); ) {
            WebSocketEventPublisher eventPublisher = it.next();
            if (eventPublisher.isClosed()) {
                it.remove();
            }
        }
    }
}

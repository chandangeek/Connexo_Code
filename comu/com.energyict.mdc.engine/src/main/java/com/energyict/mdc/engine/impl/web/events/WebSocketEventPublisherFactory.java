/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.web.events;

import com.energyict.mdc.engine.impl.core.RunningComServer;
import com.energyict.mdc.engine.impl.events.EventPublisher;
import com.energyict.mdc.engine.impl.web.events.commands.RequestParser;
import com.energyict.mdc.engine.monitor.EventAPIStatistics;

/**
 * Provides factory services for {@link WebSocketEventPublisher}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-07-25 (17:25)
 */
public class WebSocketEventPublisherFactory {

    private static WebSocketEventPublisherFactory soleInstance;

    public static WebSocketEventPublisherFactory getInstance () {
        if (soleInstance == null) {
            soleInstance = new WebSocketEventPublisherFactory();
        }
        return soleInstance;
    }

    public static void setInstance (WebSocketEventPublisherFactory factory) {
        soleInstance = factory;
    }

    public WebSocketEventPublisher newWebSocketEventPublisher (RunningComServer comServer, RequestParser.ServiceProvider serviceProvider, EventPublisher eventPublisher, WebSocketCloseEventListener closeEventListener) {
        return new WebSocketEventPublisher(comServer, serviceProvider, eventPublisher, closeEventListener);
    }


    // Hide utility class constructor
    protected WebSocketEventPublisherFactory () {}

}
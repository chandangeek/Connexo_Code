package com.energyict.mdc.engine.impl.web.events;

import com.energyict.mdc.engine.impl.core.ServiceProvider;

/**
 * Provides factory services for {@link WebSocketEventPublisher}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-11-09 (13:03)
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

    public WebSocketEventPublisher newWebSocketEventPublisher () {
        return new WebSocketEventPublisher(ServiceProvider.instance.get());
    }

    // Hide utility class constructor
    protected WebSocketEventPublisherFactory () {}

}
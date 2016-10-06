package com.energyict.mdc.engine.impl.web.events;

/**
 * Provides factory services for {@link WebSocketEventPublisher}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-07-25 (17:25)
 */
public interface WebSocketEventPublisherFactory {

    WebSocketEventPublisher newWebSocketEventPublisher(WebSocketCloseEventListener closeEventListener);

}
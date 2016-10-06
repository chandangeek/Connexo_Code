package com.energyict.mdc.engine.impl.web.events;

/**
 * Models the behavior of a component that is interested
 * to be notified when a websocket is closed.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-07-15 (10:42)
 */
interface WebSocketCloseEventListener {

    void closedFrom(WebSocketEventPublisher webSocketEventPublisher);

}
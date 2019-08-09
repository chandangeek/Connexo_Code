/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.web.events;

import com.energyict.mdc.engine.monitor.EventAPIStatistics;

/**
 * Provides factory services for {@link WebSocketEventPublisher}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-07-25 (17:25)
 */
public interface WebSocketEventPublisherFactory {

    WebSocketEventPublisher newWebSocketEventPublisher(EventAPIStatistics eventAPIStatistics);

}
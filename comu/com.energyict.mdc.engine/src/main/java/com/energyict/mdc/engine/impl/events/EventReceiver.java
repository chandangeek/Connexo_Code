/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.events;

import com.energyict.mdc.engine.events.ComServerEvent;

/**
 * Receives {@link ComServerEvent}s that are published
 * by an {@link EventPublisher} after it has registered
 * with the EventPublisher.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-10-31 (09:11)
 */
public interface EventReceiver {

    /**
     * Notifies this EventReceiver that the specified {@link ComServerEvent} has occurred.
     *
     * @param event The ComServerEvent
     */
    public void receive (ComServerEvent event);

}
/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.events;

/**
 * Provides factory services for {@link FilteringEventReceiver}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-10-31 (17:40)
 */
public interface FilteringEventReceiverFactory {

    /**
     * Produces a new {@link FilteringEventReceiver}
     * that will focus on receiving and filtering
     * {@link com.energyict.mdc.engine.events.ComServerEvent}s for the {@link EventReceiver}.
     *
     * @param eventReceiver The EventReceiver
     * @return The FilteringEventReceiver
     */
    public FilteringEventReceiver newFor (EventReceiver eventReceiver);

}
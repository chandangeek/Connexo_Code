/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.events.filtering;

import com.energyict.mdc.engine.events.ComServerEvent;

/**
 * Represents a criterion to filter {@link ComServerEvent}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-10-31 (10:29)
 */
public interface EventFilterCriterion {

    /**
     * Tests if {@link ComServerEvent} matches this EventFilterCriterion.
     *
     * @param event The ComServerEvent
     * @return <code>true</code> iff the event matches this EventFilterCriterion
     */
    public boolean matches (ComServerEvent event);

}
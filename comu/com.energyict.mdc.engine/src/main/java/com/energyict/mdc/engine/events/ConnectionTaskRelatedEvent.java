/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.events;

import com.energyict.mdc.common.tasks.ConnectionTask;

import aQute.bnd.annotation.ProviderType;

/**
 * Marks a {@link ComServerEvent} as relating to a {@link ConnectionTask}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-11-02 (09:09)
 */
@ProviderType
public interface ConnectionTaskRelatedEvent extends ComServerEvent {

    /**
     * Gets the related {@link ConnectionTask}.
     *
     * @return The ConnectionTask
     */
    public ConnectionTask getConnectionTask ();

}
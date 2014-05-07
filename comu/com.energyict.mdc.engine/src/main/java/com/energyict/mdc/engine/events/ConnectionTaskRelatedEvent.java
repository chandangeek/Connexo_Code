package com.energyict.mdc.engine.events;

import com.energyict.mdc.device.data.tasks.ConnectionTask;

/**
 * Marks a {@link ComServerEvent} as relating to a {@link ConnectionTask}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-11-02 (09:09)
 */
public interface ConnectionTaskRelatedEvent extends ComServerEvent {

    /**
     * Gets the related {@link ConnectionTask}.
     *
     * @return The ConnectionTask
     */
    public ConnectionTask getConnectionTask ();

}
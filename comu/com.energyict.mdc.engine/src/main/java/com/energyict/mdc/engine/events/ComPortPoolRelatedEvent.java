package com.energyict.mdc.engine.events;

import com.energyict.mdc.engine.model.ComPortPool;

/**
 * Marks a {@link ComServerEvent} as relating to a ComPortPool.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-11-02 (09:45)
 */
public interface ComPortPoolRelatedEvent extends ComServerEvent {

    /**
     * Gets the related ComPortPool.
     *
     * @return The ComPortPool
     */
    public ComPortPool getComPortPool ();

}
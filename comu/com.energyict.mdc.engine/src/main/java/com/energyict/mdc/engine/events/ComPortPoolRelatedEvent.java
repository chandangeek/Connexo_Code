package com.energyict.mdc.engine.events;

import com.energyict.mdc.engine.config.ComPortPool;

import aQute.bnd.annotation.ProviderType;

/**
 * Marks a {@link ComServerEvent} as relating to a ComPortPool.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-11-02 (09:45)
 */
@ProviderType
public interface ComPortPoolRelatedEvent extends ComServerEvent {

    /**
     * Gets the related ComPortPool.
     *
     * @return The ComPortPool
     */
    public ComPortPool getComPortPool ();

}
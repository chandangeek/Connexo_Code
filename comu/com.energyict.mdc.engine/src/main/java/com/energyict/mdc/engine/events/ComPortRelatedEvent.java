package com.energyict.mdc.engine.events;

import com.energyict.mdc.engine.model.ComPort;

/**
 * Marks a {@link ComServerEvent} as relating to a {@link ComPort}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-11-02 (09:31)
 */
public interface ComPortRelatedEvent extends ComServerEvent {

    /**
     * Gets the related {@link ComPort}.
     *
     * @return The ComPort
     */
    public ComPort getComPort ();

}
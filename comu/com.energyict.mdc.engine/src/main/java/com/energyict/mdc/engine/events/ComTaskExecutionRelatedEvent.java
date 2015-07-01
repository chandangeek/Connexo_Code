package com.energyict.mdc.engine.events;

import com.energyict.mdc.device.data.tasks.ComTaskExecution;

import aQute.bnd.annotation.ProviderType;

/**
 * Marks a {@link ComServerEvent} as relating to a {@link ComTaskExecution}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-11-02 (09:32)
 */
@ProviderType
public interface ComTaskExecutionRelatedEvent extends ComServerEvent {

    /**
     * Gets the related {@link ComTaskExecution}.
     *
     * @return The ComTask
     */
    public ComTaskExecution getComTaskExecution ();

}
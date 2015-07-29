package com.energyict.mdc.device.config;

import com.energyict.mdc.engine.config.InboundComPortPool;

import aQute.bnd.annotation.ProviderType;

/**
 * Partial version of an InboundConnectionTask.
 *
 * @author sva
 * @since 21/01/13 - 15:22
 */
@ProviderType
public interface PartialInboundConnectionTask extends PartialConnectionTask {

    /**
     * Gets the InboundComPortPool that is used
     * by preference for actual InboundConnectionTasks.
     *
     * @return The ComPortPool
     */
    @Override
    public InboundComPortPool getComPortPool();

    void setComportPool(InboundComPortPool comPortPool);

    void setDefault(boolean asDefault);

}
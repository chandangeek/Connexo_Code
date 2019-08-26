/*
 * Copyright (c) 2019  by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.common.device.config;

import com.energyict.mdc.common.comserver.InboundComPortPool;

import aQute.bnd.annotation.ConsumerType;

/**
 * Partial version of an InboundConnectionTask.
 *
 * @author sva
 * @since 21/01/13 - 15:22
 */
@ConsumerType
public interface PartialInboundConnectionTask extends ServerPartialConnectionTask {

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
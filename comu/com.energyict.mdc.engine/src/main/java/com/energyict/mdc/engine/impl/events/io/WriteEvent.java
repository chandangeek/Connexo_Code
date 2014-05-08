package com.energyict.mdc.engine.impl.events.io;

import com.energyict.mdc.engine.impl.core.inbound.ComPortRelatedComChannel;
import com.energyict.mdc.engine.model.ComPort;

/**
 * Models an event that represent a write session for a {@link ComPortRelatedComChannel}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-11-12 (12:24)
 */
public class WriteEvent extends CommunicationEventImpl {

    /**
     * For the externalization process only.
     *
     * @param serviceProvider The ServiceProvider
     */
    public WriteEvent (ServiceProvider serviceProvider) {
        super(serviceProvider);
    }

    public WriteEvent (ComPort comPort, byte[] bytes, ServiceProvider serviceProvider) {
        super(comPort, bytes, serviceProvider);
    }

    @Override
    public boolean isWrite () {
        return true;
    }

}
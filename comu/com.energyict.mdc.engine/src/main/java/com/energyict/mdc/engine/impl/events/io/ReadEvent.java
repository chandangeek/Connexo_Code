package com.energyict.mdc.engine.impl.events.io;

import com.energyict.mdc.engine.impl.core.inbound.ComPortRelatedComChannel;
import com.energyict.mdc.engine.model.ComPort;

/**
 * Models an event that represent a read session from a {@link ComPortRelatedComChannel}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-11-12 (12:24)
 */
public class ReadEvent extends CommunicationEventImpl {

    /**
     * For the externalization process only.
     */
    public ReadEvent (ServiceProvider serviceProvider) {
        super(serviceProvider);
    }

    public ReadEvent (ComPort comPort, byte[] bytes, ServiceProvider serviceProvider) {
        super(comPort, bytes, serviceProvider);
    }

    @Override
    public boolean isRead () {
        return true;
    }

}
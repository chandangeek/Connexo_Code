/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.events.io;

import com.energyict.mdc.engine.config.ComPort;
import com.energyict.mdc.engine.impl.core.ComPortRelatedComChannel;

/**
 * Models an event that represent a read session from a {@link ComPortRelatedComChannel}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-11-12 (12:24)
 */
public class ReadEvent extends CommunicationEventImpl {

    public ReadEvent(ServiceProvider serviceProvider, ComPort comPort, byte[] bytes) {
        super(serviceProvider, comPort, bytes);
    }

    @Override
    public boolean isRead () {
        return true;
    }

}
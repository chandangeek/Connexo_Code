/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.events.io;

import com.energyict.mdc.engine.config.ComPort;
import com.energyict.mdc.engine.events.Category;
import com.energyict.mdc.engine.events.CommunicationEvent;
import com.energyict.mdc.engine.impl.events.connection.AbstractConnectionEventImpl;

import org.json.JSONException;
import org.json.JSONWriter;

/**
 * Provides code reuse opportunities for components
 * that implement the {@link CommunicationEvent} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-11-12 (12:18)
 */
public abstract class CommunicationEventImpl extends AbstractConnectionEventImpl implements CommunicationEvent {

    private byte[] bytes;

    protected CommunicationEventImpl(ServiceProvider serviceProvider, ComPort comPort, byte[] bytes) {
        super(serviceProvider, comPort);
        this.bytes = bytes;
    }

    @Override
    public boolean isRead () {
        return false;
    }

    @Override
    public boolean isWrite () {
        return false;
    }
    @Override
    public byte[] getBytes () {
        return this.bytes;
    }

    @Override
    public Category getCategory () {
        return Category.CONNECTION;
    }

    @Override
    public void toString (JSONWriter writer) throws JSONException {
        super.toString(writer);
        writer.
            key(this.jsonKeyForBytes()).
            value(this.getBytes());
    }

    private String jsonKeyForBytes () {
        if (this.isRead()) {
            return "bytes-read";
        }
        else {
            return "bytes-written";
        }
    }

}
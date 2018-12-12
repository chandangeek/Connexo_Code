/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.events.connection;

import com.elster.jupiter.util.HasId;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.engine.config.ComPort;
import com.energyict.mdc.engine.config.ComPortPool;
import com.energyict.mdc.engine.config.InboundComPort;
import com.energyict.mdc.engine.events.Category;
import com.energyict.mdc.engine.events.ComPortPoolRelatedEvent;
import com.energyict.mdc.engine.events.ConnectionEvent;
import com.energyict.mdc.engine.impl.events.AbstractComServerEventImpl;

import org.json.JSONException;
import org.json.JSONWriter;

/**
 * Represents a {@link com.energyict.mdc.engine.events.ConnectionEvent}
 * but it is yet undiscovered which device
 * is actually connecting to the InboundComPort.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-11-07 (09:28)
 */
public abstract class UndiscoveredConnectionEvent extends AbstractComServerEventImpl implements ConnectionEvent, ComPortPoolRelatedEvent {

    private InboundComPort comPort;

    public UndiscoveredConnectionEvent(ServiceProvider serviceProvider, InboundComPort comPort) {
        super(serviceProvider);
        this.comPort = comPort;
    }

    @Override
    public Category getCategory () {
        return Category.CONNECTION;
    }

    @Override
    public boolean isFailure () {
        return false;
    }

    @Override
    public String getFailureMessage () {
        return null;
    }

    @Override
    public boolean isComPortRelated () {
        return this.comPort != null;
    }

    @Override
    public ComPort getComPort () {
        return this.comPort;
    }

    @Override
    public Device getDevice () {
        return null;
    }

    @Override
    public ConnectionTask getConnectionTask () {
        return null;
    }

    @Override
    public boolean isComPortPoolRelated () {
        return this.isComPortRelated();
    }

    @Override
    public ComPortPool getComPortPool () {
        return this.comPort.getComPortPool();
    }

    private long extractId(HasId hasId) {
        if (hasId == null) {
            return 0;
        }
        else {
            return hasId.getId();
        }
    }

    @Override
    protected void toString (JSONWriter writer) throws JSONException {
        super.toString(writer);
        writer.
            key("com-port").
            value(this.extractId(this.getComPort()));
    }

}
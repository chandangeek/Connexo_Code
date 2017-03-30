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
 * Provides code reuse opportunities for classes
 * that represent a {@link ConnectionEvent}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-11-06 (10:47)
 */
public abstract class AbstractConnectionEventImpl extends AbstractComServerEventImpl implements ConnectionEvent, ComPortPoolRelatedEvent {

    private static final int NULL_INDICATOR = -1;
    private ComPort comPort;
    private ConnectionTask connectionTask;

    protected AbstractConnectionEventImpl(ServiceProvider serviceProvider, ConnectionTask connectionTask, ComPort comPort) {
        super(serviceProvider);
        this.connectionTask = connectionTask;
        this.comPort = comPort;
    }

    protected AbstractConnectionEventImpl(ServiceProvider serviceProvider, ComPort comPort) {
        super(serviceProvider);
        this.comPort = comPort;
    }

    @Override
    public Category getCategory () {
        return Category.CONNECTION;
    }

    @Override
    public boolean isClosed () {
        return false;
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
    public boolean isEstablishing () {
        return false;
    }

    @Override
    public boolean isComPortPoolRelated () {
        return this.isComPortRelated() && this.getComPort().isInbound();
    }

    @Override
    public ComPortPool getComPortPool () {
        InboundComPort inboundComPort = (InboundComPort) this.getComPort();
        return inboundComPort.getComPortPool();
    }

    @Override
    public boolean isDeviceRelated () {
        return this.isConnectionTaskRelated();
    }

    @Override
    public Device getDevice () {
        return this.getConnectionTask().getDevice();
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
    public boolean isConnectionTaskRelated () {
        return this.connectionTask != null;
    }

    @Override
    public ConnectionTask getConnectionTask () {
        return this.connectionTask;
    }

    private long extractId(HasId hasId) {
        if (hasId == null) {
            return NULL_INDICATOR;
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
            value(this.extractId(this.getComPort())).
            key("connection-task").
            value(this.extractId(this.getConnectionTask()));
    }

}
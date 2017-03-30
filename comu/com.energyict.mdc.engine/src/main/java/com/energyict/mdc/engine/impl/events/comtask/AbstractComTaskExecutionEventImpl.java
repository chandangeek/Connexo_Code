/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.events.comtask;

import com.elster.jupiter.util.HasId;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.engine.config.ComPort;
import com.energyict.mdc.engine.config.ComPortPool;
import com.energyict.mdc.engine.config.InboundComPort;
import com.energyict.mdc.engine.events.Category;
import com.energyict.mdc.engine.events.ComTaskExecutionEvent;
import com.energyict.mdc.engine.impl.events.AbstractComServerEventImpl;

import org.json.JSONException;
import org.json.JSONWriter;

import java.time.Instant;

/**
 * Provides code reuse opportunities for components
 * that represent {@link ComTaskExecutionEvent}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-11-06 (15:27)
 */
public abstract class AbstractComTaskExecutionEventImpl extends AbstractComServerEventImpl implements ComTaskExecutionEvent {

    private ComTaskExecution comTaskExecution;
    private ComPort comPort;
    private ConnectionTask connectionTask;

    protected AbstractComTaskExecutionEventImpl(ServiceProvider serviceProvider, ComTaskExecution comTaskExecution, ComPort comPort, ConnectionTask connectionTask) {
        super(serviceProvider);
        this.comTaskExecution = comTaskExecution;
        this.comPort = comPort;
        this.connectionTask = connectionTask;
    }

    @Override
    public Category getCategory () {
        return Category.COMTASK;
    }

    @Override
    public boolean isStart () {
        return false;
    }

    @Override
    public Instant getExecutionStartedTimestamp() {
        return null;
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
    public boolean isCompletion () {
        return false;
    }

    @Override
    public boolean isComTaskExecutionRelated () {
        return this.comTaskExecution != null;
    }

    @Override
    public ComTaskExecution getComTaskExecution () {
        return this.comTaskExecution;
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

    @Override
    public boolean isLoggingRelated () {
        return false;
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
            key("com-task-execution").
            value(this.extractId(this.getComTaskExecution())).
            key("com-port").
            value(this.extractId(this.getComPort())).
            key("connection-task").
            value(this.extractId(this.getConnectionTask()));
    }

}
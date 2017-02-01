/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.events.logging;

import com.elster.jupiter.util.HasId;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.engine.config.ComPort;
import com.energyict.mdc.engine.config.ComPortPool;
import com.energyict.mdc.engine.config.InboundComPort;
import com.energyict.mdc.engine.events.Category;
import com.energyict.mdc.engine.events.ComPortPoolRelatedEvent;
import com.energyict.mdc.engine.events.ComPortRelatedEvent;
import com.energyict.mdc.engine.events.ConnectionTaskRelatedEvent;
import com.energyict.mdc.engine.events.DeviceRelatedEvent;
import com.energyict.mdc.engine.events.LoggingEvent;
import com.energyict.mdc.engine.impl.events.AbstractComServerEventImpl;
import com.energyict.mdc.engine.impl.logging.LogLevel;

import org.json.JSONException;
import org.json.JSONWriter;

/**
 * Provides an implementation for the {@link LoggingEvent} interface
 * for events that relate to communication for a {@link com.energyict.mdc.protocol.api.device.BaseDevice device}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-11-08 (11:08)
 */
public class CommunicationLoggingEvent extends AbstractComServerEventImpl implements LoggingEvent, DeviceRelatedEvent, ConnectionTaskRelatedEvent, ComPortRelatedEvent, ComPortPoolRelatedEvent {

    private ComPort comPort;
    private ConnectionTask connectionTask;
    private LogLevel logLevel;
    private String logMessage;

    public CommunicationLoggingEvent(ServiceProvider serviceProvider, ConnectionTask connectionTask, ComPort comPort, LogLevel logLevel, String logMessage) {
        super(serviceProvider);
        this.logLevel = logLevel;
        this.logMessage = logMessage;
        this.connectionTask = connectionTask;
        this.comPort = comPort;
    }

    @Override
    public Category getCategory () {
        return Category.LOGGING;
    }

    @Override
    public boolean isLoggingRelated () {
        return true;
    }

    @Override
    public LogLevel getLogLevel () {
        return logLevel;
    }

    @Override
    public String getLogMessage () {
        return logMessage;
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
            return 0;
        }
        else {
            return hasId.getId();
        }
    }

    @Override
    public void toString (JSONWriter writer) throws JSONException {
        super.toString(writer);
        writer.
            key("com-port").
            value(this.extractId(this.getComPort())).
            key("connection-task").
            value(this.extractId(this.getConnectionTask())).
            key("log-level").
            value(String.valueOf(this.getLogLevel())).
            key("message").
            value(this.getLogMessage());
    }

}
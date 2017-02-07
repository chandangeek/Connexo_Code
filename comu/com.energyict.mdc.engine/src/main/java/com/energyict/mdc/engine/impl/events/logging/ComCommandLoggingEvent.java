/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.events.logging;

import com.elster.jupiter.util.HasId;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.engine.config.ComPort;
import com.energyict.mdc.engine.config.ComPortPool;
import com.energyict.mdc.engine.config.InboundComPort;
import com.energyict.mdc.engine.events.Category;
import com.energyict.mdc.engine.events.ComPortPoolRelatedEvent;
import com.energyict.mdc.engine.events.ComPortRelatedEvent;
import com.energyict.mdc.engine.events.ComTaskExecutionRelatedEvent;
import com.energyict.mdc.engine.events.ConnectionTaskRelatedEvent;
import com.energyict.mdc.engine.events.DeviceRelatedEvent;
import com.energyict.mdc.engine.events.LoggingEvent;
import com.energyict.mdc.engine.impl.events.AbstractComServerEventImpl;
import com.energyict.mdc.engine.impl.logging.LogLevel;

import org.json.JSONException;
import org.json.JSONWriter;

public class ComCommandLoggingEvent extends AbstractComServerEventImpl implements LoggingEvent, DeviceRelatedEvent, ConnectionTaskRelatedEvent, ComPortRelatedEvent, ComPortPoolRelatedEvent, ComTaskExecutionRelatedEvent {

    private ComPort comPort;
    private ConnectionTask connectionTask;
    private ComTaskExecution comTaskExecution;
    private LogLevel logLevel;
    private String logMessage;

    public ComCommandLoggingEvent(ServiceProvider serviceProvider, ComPort comPort, ConnectionTask connectionTask, ComTaskExecution comTaskExecution, LogLevel logLevel, String logMessage) {
        super(serviceProvider);
        this.comPort = comPort;
        this.connectionTask = connectionTask;
        this.comTaskExecution = comTaskExecution;
        this.logLevel = logLevel;
        this.logMessage = logMessage;
    }

    @Override
    public boolean isDeviceRelated() {
        return isConnectionTaskRelated();
    }

    @Override
    public boolean isConnectionTaskRelated() {
        return this.connectionTask != null;
    }

    @Override
    public boolean isComPortRelated() {
        return this.comPort != null;
    }

    @Override
    public boolean isComPortPoolRelated() {
        return this.isComPortRelated() && this.comPort.isInbound();
    }

    @Override
    public boolean isComTaskExecutionRelated() {
        return this.comTaskExecution != null;
    }

    @Override
    public boolean isLoggingRelated() {
        return true;
    }

    @Override
    public LogLevel getLogLevel() {
        return this.logLevel;
    }

    @Override
    public String getLogMessage() {
        return this.logMessage;
    }

    @Override
    public Category getCategory() {
        return Category.LOGGING;
    }

    @Override
    public ComPortPool getComPortPool() {
        InboundComPort inboundComPort = (InboundComPort) this.getComPort();
        return inboundComPort.getComPortPool();
    }

    @Override
    public ComPort getComPort() {
        return this.comPort;
    }

    @Override
    public ComTaskExecution getComTaskExecution() {
        return this.comTaskExecution;
    }

    @Override
    public ConnectionTask getConnectionTask() {
        return this.connectionTask;
    }

    @Override
    public Device getDevice() {
        return this.getConnectionTask().getDevice();
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
                key("com-task-execution").
                value(this.extractId(this.getComTaskExecution())).
                key("log-level").
                value(String.valueOf(this.getLogLevel())).
                key("message").
                value(this.getLogMessage());
    }

}

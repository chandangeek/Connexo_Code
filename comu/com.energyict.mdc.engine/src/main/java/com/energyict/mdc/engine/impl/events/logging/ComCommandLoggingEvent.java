package com.energyict.mdc.engine.impl.events.logging;

import com.energyict.mdc.common.HasId;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.engine.events.Category;
import com.energyict.mdc.engine.events.ComPortPoolRelatedEvent;
import com.energyict.mdc.engine.events.ComPortRelatedEvent;
import com.energyict.mdc.engine.events.ComTaskExecutionRelatedEvent;
import com.energyict.mdc.engine.events.ConnectionTaskRelatedEvent;
import com.energyict.mdc.engine.events.DeviceRelatedEvent;
import com.energyict.mdc.engine.events.LoggingEvent;
import com.energyict.mdc.engine.impl.events.AbstractComServerEventImpl;
import com.energyict.mdc.engine.impl.logging.LogLevel;
import com.energyict.mdc.engine.model.ComPort;
import com.energyict.mdc.engine.model.ComPortPool;
import com.energyict.mdc.engine.model.InboundComPort;
import com.energyict.mdc.protocol.api.device.BaseDevice;

import org.json.JSONException;
import org.json.JSONWriter;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * Provides an implementation for the {@link LoggingEvent} interface
 * for events that relate to the execution of a
 * {@link ComTaskExecution comTask} with a {@link ConnectionTask connectionTask}
 * for a {@link com.energyict.mdc.protocol.api.device.BaseDevice device} on a specific {@link ComPort comPort}.
 * <p/>
 * Copyrights EnergyICT
 * Date: 22/11/12
 * Time: 15:30
 */
public class ComCommandLoggingEvent extends AbstractComServerEventImpl implements LoggingEvent, DeviceRelatedEvent, ConnectionTaskRelatedEvent, ComPortRelatedEvent, ComPortPoolRelatedEvent, ComTaskExecutionRelatedEvent {

    private ComPort comPort;
    private ConnectionTask connectionTask;
    private ComTaskExecution comTaskExecution;
    private LogLevel logLevel;
    private String logMessage;

    /**
     * For the externalization process only.
     */
    public ComCommandLoggingEvent() {
        super();
    }

    public ComCommandLoggingEvent(ComPort comPort, ConnectionTask connectionTask, ComTaskExecution comTaskExecution, LogLevel logLevel, String logMessage) {
        super();
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
    public BaseDevice getDevice() {
        return this.getConnectionTask().getDevice();
    }

    @Override
    public void writeExternal (ObjectOutput out) throws IOException {
        super.writeExternal(out);
        LoggingEventExternalizationAssistant.writeExternal(this, out);
        out.writeLong(this.extractId(this.getComPort()));
        out.writeLong(this.extractId(this.getConnectionTask()));
        out.writeLong(this.extractId(this.getComTaskExecution()));
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
    public void readExternal (ObjectInput in) throws IOException, ClassNotFoundException {
        super.readExternal(in);
        LoggingEventExternalizationAssistant.LoggingEventPojo pojo = LoggingEventExternalizationAssistant.readExternal(in);
        this.logLevel = pojo.getLogLevel();
        this.logMessage = pojo.getLogMessage();
        this.comPort = this.findComPort(in.readLong());
        this.connectionTask = this.findConnectionTask(in.readLong());
        this.comTaskExecution = this.findComTaskExecution(in.readLong());
    }

    private ComPort findComPort (long comPortId) {
        if (comPortId != 0) {
            return this.getEngineModelService().findComPort(comPortId);
        }
        else {
            return null;
        }
    }

    private ConnectionTask findConnectionTask (long connectionTaskId) {
        if (connectionTaskId != 0) {
            return this.getDeviceDataService().findConnectionTask(connectionTaskId).orNull();
        }
        else {
            return null;
        }
    }

    private ComTaskExecution findComTaskExecution (long comTaskExecutionId) {
        if (comTaskExecutionId != 0) {
            return this.getDeviceDataService().findComTaskExecution(comTaskExecutionId);
        }
        else {
            return null;
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

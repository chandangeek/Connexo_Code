package com.energyict.mdc.engine.impl.events.comtask;

import com.energyict.mdc.common.HasId;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.engine.events.Category;
import com.energyict.mdc.engine.events.ComTaskExecutionEvent;
import com.energyict.mdc.engine.impl.events.AbstractComServerEventImpl;
import com.energyict.mdc.engine.model.ComPort;
import com.energyict.mdc.engine.model.ComPortPool;
import com.energyict.mdc.engine.model.InboundComPort;
import com.energyict.mdc.protocol.api.device.BaseDevice;

import org.json.JSONException;
import org.json.JSONWriter;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Date;

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

    /**
     * For the externalization process only.
     */
    protected AbstractComTaskExecutionEventImpl() {
        super();
    }

    protected AbstractComTaskExecutionEventImpl(ComTaskExecution comTaskExecution, ComPort comPort, ConnectionTask connectionTask) {
        super();
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
    public Date getExecutionStartedTimestamp () {
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
    public BaseDevice getDevice () {
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

    @Override
    public void writeExternal (ObjectOutput out) throws IOException {
        super.writeExternal(out);
        out.writeLong(this.extractId(this.getComTaskExecution()));
        out.writeLong(this.extractId(this.getComPort()));
        out.writeLong(this.extractId(this.getConnectionTask()));
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
        this.comTaskExecution = this.findComTaskExecution(in.readLong());
        this.comPort = this.findComPort(in.readLong());
        this.connectionTask = this.findConnectionTask(in.readLong());
    }

    private ComTaskExecution findComTaskExecution (long comTaskExecutionId) {
        if (comTaskExecutionId != 0) {
            return this.getDeviceDataService().findComTaskExecution(comTaskExecutionId);
        }
        else {
            return null;
        }
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
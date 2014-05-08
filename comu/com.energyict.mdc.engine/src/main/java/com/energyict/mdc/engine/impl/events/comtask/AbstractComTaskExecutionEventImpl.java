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
     *
     * @param serviceProvider The ServiceProvider
     */
    protected AbstractComTaskExecutionEventImpl(ServiceProvider serviceProvider) {
        super(serviceProvider);
    }

    protected AbstractComTaskExecutionEventImpl(ComTaskExecution comTaskExecution, ComPort comPort, ConnectionTask connectionTask, ServiceProvider serviceProvider) {
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
        out.writeInt(this.getBusinessObjectId(this.comTaskExecution));
        out.writeLong(this.comPort.getId());
        out.writeInt((int) this.connectionTask.getId());
    }

    private int getBusinessObjectId (HasId businessObject) {
        if (businessObject == null) {
            return 0;
        }
        else {
            return (int) businessObject.getId();
        }
    }

    @Override
    public void readExternal (ObjectInput in) throws IOException, ClassNotFoundException {
        super.readExternal(in);
        this.comTaskExecution = this.findComTaskExecution(in.readInt());
        this.comPort = this.findComPort(in.readInt());
        this.connectionTask = this.findConnectionTask(in.readInt());
    }

    private ComTaskExecution findComTaskExecution (int comTaskExecutionId) {
        return getDeviceDataService().findComTaskExecution(comTaskExecutionId);
    }

    private ComPort findComPort (int comPortId) {
        return getEngineModelService().findComPort(comPortId);
    }

    private ConnectionTask findConnectionTask (int connectionTaskId) {
        return getDeviceDataService().findConnectionTask(connectionTaskId).orNull();
    }

    @Override
    protected void toString (JSONWriter writer) throws JSONException {
        super.toString(writer);
        writer.
            key("com-task-execution").
            value(this.getBusinessObjectId(this.getComTaskExecution())).
            key("com-port").
            value(this.getComPort().getId()).
            key("connection-task").
            value(this.getConnectionTask().getId());
    }

}
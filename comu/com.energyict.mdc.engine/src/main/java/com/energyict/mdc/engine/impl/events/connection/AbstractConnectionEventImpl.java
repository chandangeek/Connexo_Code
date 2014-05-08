package com.energyict.mdc.engine.impl.events.connection;

import com.energyict.mdc.common.IdBusinessObject;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.engine.events.Category;
import com.energyict.mdc.engine.events.ComPortPoolRelatedEvent;
import com.energyict.mdc.engine.events.ConnectionEvent;
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

    /**
     * For the externalization process only.
     *
     * @param serviceProvider The ServiceProvider
     */
    protected AbstractConnectionEventImpl (ServiceProvider serviceProvider) {
        super(serviceProvider);
    }

    protected AbstractConnectionEventImpl (ConnectionTask connectionTask, ComPort comPort, ServiceProvider serviceProvider) {
        super(serviceProvider);
        this.connectionTask = connectionTask;
        this.comPort = comPort;
    }

    protected AbstractConnectionEventImpl (ComPort comPort, ServiceProvider serviceProvider) {
        this(serviceProvider);
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
    public void writeExternal (ObjectOutput out) throws IOException {
        super.writeExternal(out);
        out.writeLong(this.comPort.getId());
        out.writeInt((int) this.connectionTask.getId());
    }

    private int getBusinessObjectId (IdBusinessObject businessObject) {
        if (businessObject == null) {
            return NULL_INDICATOR;
        }
        else {
            return businessObject.getId();
        }
    }

    @Override
    public void readExternal (ObjectInput in) throws IOException, ClassNotFoundException {
        super.readExternal(in);
        this.comPort = this.findComPort(in.readInt());
        this.connectionTask = this.findConnectionTask(in.readInt());
    }

    private ComPort findComPort (int comPortId) {
        if (NULL_INDICATOR == comPortId) {
            return null;
        }
        else {
            return getEngineModelService().findComPort(comPortId);
        }
    }

    private ConnectionTask findConnectionTask (int connectionTaskId) {
        if (NULL_INDICATOR == connectionTaskId) {
            return null;
        }
        else {
            return getDeviceDataService().findConnectionTask(connectionTaskId).orNull();
        }
    }

    @Override
    protected void toString (JSONWriter writer) throws JSONException {
        super.toString(writer);
        writer.
            key("com-port").
            value(this.getComPort().getId()).
            key("connection-task").
            value(this.getConnectionTask().getId());
    }

}
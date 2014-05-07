package com.energyict.mdc.engine.impl.events.connection;

import com.elster.jupiter.util.time.Clock;
import com.energyict.mdc.device.data.DeviceDataService;
import com.energyict.mdc.engine.events.Category;
import com.energyict.mdc.engine.events.ComPortPoolRelatedEvent;
import com.energyict.mdc.engine.events.ConnectionEvent;
import com.energyict.mdc.engine.impl.events.AbstractComServerEventImpl;
import com.energyict.mdc.common.IdBusinessObject;
import com.energyict.mdc.engine.model.ComPort;
import com.energyict.mdc.engine.model.ComPortPool;
import com.energyict.mdc.engine.model.EngineModelService;
import com.energyict.mdc.engine.model.InboundComPort;
import com.energyict.mdc.protocol.api.device.BaseDevice;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import org.json.JSONException;
import org.json.JSONWriter;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

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

    /**
     * For the externalization process only.
     */
    protected UndiscoveredConnectionEvent (Clock clock, DeviceDataService deviceDataService, EngineModelService engineModelService) {
        super(clock, deviceDataService, engineModelService);
    }

    public UndiscoveredConnectionEvent (InboundComPort comPort, Clock clock, DeviceDataService deviceDataService, EngineModelService engineModelService) {
        this(clock, deviceDataService, engineModelService);
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
    public BaseDevice getDevice () {
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

    @Override
    public void writeExternal (ObjectOutput out) throws IOException {
        super.writeExternal(out);
        out.writeLong(this.comPort.getId());
    }

    private int getBusinessObjectId (IdBusinessObject businessObject) {
        if (businessObject == null) {
            return 0;
        }
        else {
            return businessObject.getId();
        }
    }

    @Override
    public void readExternal (ObjectInput in) throws IOException, ClassNotFoundException {
        super.readExternal(in);
        this.comPort = this.findComPort(in.readInt());
    }

    private InboundComPort findComPort (int comPortId) {
        return (InboundComPort) getEngineModelService().findComPort(comPortId);
    }

    @Override
    protected void toString (JSONWriter writer) throws JSONException {
        super.toString(writer);
        writer.
            key("com-port").
                value(this.getComPort().getId());
    }

}
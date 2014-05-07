package com.energyict.mdc.engine.impl.events.io;

import com.elster.jupiter.util.time.Clock;
import com.energyict.mdc.device.data.DeviceDataService;
import com.energyict.mdc.engine.events.Category;
import com.energyict.mdc.engine.events.CommunicationEvent;
import com.energyict.mdc.engine.impl.events.connection.AbstractConnectionEventImpl;
import com.energyict.mdc.engine.model.ComPort;
import com.energyict.mdc.engine.model.EngineModelService;
import org.json.JSONException;
import org.json.JSONWriter;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Date;

/**
 * Provides code reuse opportunities for components
 * that implement the {@link CommunicationEvent} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-11-12 (12:18)
 */
public abstract class CommunicationEventImpl extends AbstractConnectionEventImpl implements CommunicationEvent {

    private static final int NULL_BYTES_INDICATOR = -1;
    private byte[] bytes;

    /**
     * For the externalization process only.
     */
    protected CommunicationEventImpl (Clock clock, DeviceDataService deviceDataService, EngineModelService engineModelService) {
        super(clock, deviceDataService, engineModelService);
    }

    protected CommunicationEventImpl (Date occurrenceTimestamp, ComPort comPort, byte[] bytes, Clock clock, DeviceDataService deviceDataService, EngineModelService engineModelService) {
        super(occurrenceTimestamp, comPort, clock, deviceDataService, engineModelService);
        this.bytes = bytes;
    }

    @Override
    public boolean isRead () {
        return false;
    }

    @Override
    public boolean isWrite () {
        return false;
    }
    @Override
    public byte[] getBytes () {
        return this.bytes;
    }

    @Override
    public void writeExternal (ObjectOutput out) throws IOException {
        super.writeExternal(out);
        if (this.bytes == null) {
            out.writeInt(NULL_BYTES_INDICATOR);
        }
        else {
            out.writeInt(this.bytes.length);
            out.write(this.bytes, 0, this.bytes.length);
        }
    }

    @Override
    public void readExternal (ObjectInput in) throws IOException, ClassNotFoundException {
        super.readExternal(in);
        int numberOfBytes = in.readInt();
        if (numberOfBytes == NULL_BYTES_INDICATOR) {
            this.bytes = null;
        }
        else {
            this.bytes = new byte[numberOfBytes];
            in.read(this.bytes, 0, numberOfBytes);
        }
    }

    @Override
    public Category getCategory () {
        return Category.CONNECTION;
    }

    @Override
    public void toString (JSONWriter writer) throws JSONException {
        super.toString(writer);
        writer.
            key(this.jsonKeyForBytes()).
            value(this.getBytes());
    }

    private String jsonKeyForBytes () {
        if (this.isRead()) {
            return "bytes-read";
        }
        else {
            return "bytes-written";
        }
    }

}
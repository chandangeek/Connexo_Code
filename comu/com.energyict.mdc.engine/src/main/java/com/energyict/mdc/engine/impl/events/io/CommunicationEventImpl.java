package com.energyict.mdc.engine.impl.events.io;

import com.energyict.mdc.engine.events.Category;
import com.energyict.mdc.engine.events.CommunicationEvent;
import com.energyict.mdc.engine.impl.events.connection.AbstractConnectionEventImpl;
import com.energyict.mdc.engine.model.ComPort;

import org.json.JSONException;
import org.json.JSONWriter;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

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
    protected CommunicationEventImpl() {
        super();
    }

    protected CommunicationEventImpl(ComPort comPort, byte[] bytes) {
        super(comPort);
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
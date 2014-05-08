package com.energyict.mdc.engine.impl.events.connection;

import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.engine.model.ComPort;
import com.energyict.mdc.protocol.api.ConnectionException;

import org.json.JSONException;
import org.json.JSONWriter;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Represents a {@link com.energyict.mdc.engine.events.ConnectionEvent}
 * for an outbound connection that failed to established for a {@link com.energyict.mdc.protocol.api.device.BaseDevice device}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-11-06 (11:30)
 */
public class CannotEstablishConnectionEvent extends AbstractConnectionEventImpl {

    private String failureMessage;

    /**
     * For the externalization process only.
     *
     * @param serviceProvider The ServiceProvider
     */
    public CannotEstablishConnectionEvent (ServiceProvider serviceProvider) {
        super(serviceProvider);
    }

    public CannotEstablishConnectionEvent (ComPort comPort, ConnectionTask connectionTask, ConnectionException cause, ServiceProvider serviceProvider) {
        super(connectionTask, comPort, serviceProvider);
        this.copyFailureMessageFromException(cause);
    }

    private void copyFailureMessageFromException (ConnectionException cause) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        writer.println(cause.getMessage());
        cause.printStackTrace(writer);
        this.failureMessage = stringWriter.toString();
    }

    @Override
    public boolean isFailure () {
        return true;
    }

    @Override
    public String getFailureMessage () {
        return this.failureMessage;
    }

    @Override
    public void writeExternal (ObjectOutput out) throws IOException {
        super.writeExternal(out);
        if (this.failureMessage == null) {
            out.writeInt(-1);
        }
        else {
            out.writeInt(this.failureMessage.length());
            out.writeUTF(this.failureMessage);
        }
    }

    @Override
    public void readExternal (ObjectInput in) throws IOException, ClassNotFoundException {
        super.readExternal(in);
        int length = in.readInt();
        if (length < 0) {
            this.failureMessage = null;
        }
        else {
            this.failureMessage = in.readUTF();
        }
    }

    @Override
    protected void toString (JSONWriter writer) throws JSONException {
        super.toString(writer);
        writer.key("failure").value(this.getFailureMessage());
    }

}
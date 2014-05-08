package com.energyict.mdc.engine.impl.events.comtask;

import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.engine.model.ComPort;

import org.json.JSONException;
import org.json.JSONWriter;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Represents a {@link com.energyict.mdc.engine.events.ComTaskExecutionEvent}
 * that indicates that a ComTaskExecution failed on a {@link com.energyict.mdc.engine.model.ComPort}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-11-06 (16:45)
 */
public class ComTaskExecutionFailureEvent extends AbstractComTaskExecutionEventImpl {

    private String failureMessage;

    /**
     * For the externalization process only.
     *
     * @param serviceProvider The ServiceProvider
     */
    public ComTaskExecutionFailureEvent (ServiceProvider serviceProvider) {
        super(serviceProvider);
    }

    public ComTaskExecutionFailureEvent (ComTaskExecution comTask, ComPort comPort, ConnectionTask connectionTask, ServiceProvider serviceProvider) {
        super(comTask, comPort, connectionTask, serviceProvider);
        this.failureMessage = "Failure due to problems reported during execution";
    }

    public ComTaskExecutionFailureEvent (ComTaskExecution comTask, ComPort comPort, ConnectionTask connectionTask, Throwable cause, ServiceProvider serviceProvider) {
        super(comTask, comPort, connectionTask, serviceProvider);
        this.copyFailureMessageFromException(cause);
    }

    private void copyFailureMessageFromException (Throwable cause) {
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
        return failureMessage;
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
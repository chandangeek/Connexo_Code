/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.events.comtask;

import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.engine.config.ComPort;

import org.json.JSONException;
import org.json.JSONWriter;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Represents a {@link com.energyict.mdc.engine.events.ComTaskExecutionEvent}
 * that indicates that a ComTaskExecution failed on a {@link com.energyict.mdc.engine.config.ComPort}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-11-06 (16:45)
 */
public class ComTaskExecutionFailureEvent extends AbstractComTaskExecutionEventImpl {

    private String failureMessage;

    public ComTaskExecutionFailureEvent(ServiceProvider serviceProvider, ComTaskExecution comTask, ComPort comPort, ConnectionTask connectionTask) {
        super(serviceProvider, comTask, comPort, connectionTask);
        this.failureMessage = "Failure due to problems reported during execution";
    }

    public ComTaskExecutionFailureEvent(ServiceProvider serviceProvider, ComTaskExecution comTask, ComPort comPort, ConnectionTask connectionTask, Throwable cause) {
        super(serviceProvider, comTask, comPort, connectionTask);
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
    protected void toString (JSONWriter writer) throws JSONException {
        super.toString(writer);
        writer.key("failure").value(this.getFailureMessage());
    }

}
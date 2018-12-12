/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.events.comtask;

import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.engine.config.ComPort;

import org.json.JSONException;
import org.json.JSONWriter;

import java.time.Instant;

/**
 * Represents a {@link com.energyict.mdc.engine.events.ComTaskExecutionEvent}
 * that indicates that a ComTaskExecution started on a {@link com.energyict.mdc.engine.config.ComPort}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-11-06 (15:43)
 */
public class ComTaskExecutionStartedEvent extends AbstractComTaskExecutionEventImpl {

    private Instant executionStartedTimestamp;

    public ComTaskExecutionStartedEvent(ServiceProvider serviceProvider, ComTaskExecution comTaskExecution, ComPort comPort, ConnectionTask connectionTask) {
        this(serviceProvider, comTaskExecution, comTaskExecution.getExecutionStartedTimestamp(), comPort, connectionTask);
    }

    public ComTaskExecutionStartedEvent(ServiceProvider serviceProvider, ComTaskExecution comTask, Instant executionStartedTimestamp, ComPort comPort, ConnectionTask connectionTask) {
        super(serviceProvider, comTask, comPort, connectionTask);
        this.executionStartedTimestamp = executionStartedTimestamp;
    }

    @Override
    public boolean isStart () {
        return true;
    }

    @Override
    public Instant getExecutionStartedTimestamp() {
        return executionStartedTimestamp;
    }

    @Override
    protected void toString (JSONWriter writer) throws JSONException {
        super.toString(writer);
        writer.
            key("execution-started-timestamp").
            value(this.formatOccurrenceTimeStamp(this.getExecutionStartedTimestamp()));
    }

}
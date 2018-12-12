/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.events.comtask;

import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.history.ComTaskExecutionSession;
import com.energyict.mdc.engine.config.ComPort;

import org.json.JSONException;
import org.json.JSONWriter;

/**
 * Represents a {@link com.energyict.mdc.engine.events.ComTaskExecutionEvent}
 * that indicates that a {@link ComTaskExecution}
 * completed on a {@link ComPort}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-11-06 (16:36)
 */
public class ComTaskExecutionCompletionEvent extends AbstractComTaskExecutionEventImpl {

    private final ComTaskExecutionSession.SuccessIndicator successIndicator;
    public ComTaskExecutionCompletionEvent(ServiceProvider serviceProvider, ComTaskExecution comTask, ComTaskExecutionSession.SuccessIndicator successIndicator, ComPort comPort, ConnectionTask connectionTask) {
        super(serviceProvider, comTask, comPort, connectionTask);
        this.successIndicator = successIndicator;
    }

    @Override
    public boolean isCompletion () {
        return true;
    }

    @Override
    protected void toString (JSONWriter writer) throws JSONException {
        super.toString(writer);
        writer.key("execution-completed").value(Boolean.TRUE);
        writer.key("successIndicator").value(this.successIndicator.name());
    }

}
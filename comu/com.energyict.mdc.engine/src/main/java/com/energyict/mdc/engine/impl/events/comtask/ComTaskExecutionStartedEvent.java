package com.energyict.mdc.engine.impl.events.comtask;

import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.engine.model.ComPort;

import org.json.JSONException;
import org.json.JSONWriter;

import java.util.Date;

/**
 * Represents a {@link com.energyict.mdc.engine.events.ComTaskExecutionEvent}
 * that indicates that a ComTaskExecution started on a {@link com.energyict.mdc.engine.model.ComPort}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-11-06 (15:43)
 */
public class ComTaskExecutionStartedEvent extends AbstractComTaskExecutionEventImpl {

    private Date executionStartedTimestamp;

    /**
     * For the externalization process only.
     */
    public ComTaskExecutionStartedEvent() {
        super();
    }

    public ComTaskExecutionStartedEvent (ComTaskExecution comTaskExecution, ComPort comPort, ConnectionTask connectionTask, ServiceProvider serviceProvider) {
        this(comTaskExecution, comTaskExecution.getExecutionStartedTimestamp(), comPort, connectionTask);
    }

    public ComTaskExecutionStartedEvent(ComTaskExecution comTask, Date executionStartedTimestamp, ComPort comPort, ConnectionTask connectionTask) {
        super(comTask, comPort, connectionTask);
        this.executionStartedTimestamp = executionStartedTimestamp;
    }

    @Override
    public boolean isStart () {
        return true;
    }

    @Override
    public Date getExecutionStartedTimestamp () {
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
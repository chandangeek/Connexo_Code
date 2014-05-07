package com.energyict.mdc.engine.impl.events.comtask;

import com.elster.jupiter.util.time.Clock;
import com.energyict.mdc.device.data.DeviceDataService;
import com.energyict.mdc.engine.model.ComPort;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.engine.model.EngineModelService;
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

    /**
     * For the externalization process only.
     */
    public ComTaskExecutionCompletionEvent (Clock clock, DeviceDataService deviceDataService, EngineModelService engineModelService) {
        super(clock, deviceDataService, engineModelService);
    }

    public ComTaskExecutionCompletionEvent (ComTaskExecution comTask, ComPort comPort, ConnectionTask connectionTask, Clock clock, DeviceDataService deviceDataService, EngineModelService engineModelService) {
        super(comTask, comPort, connectionTask, clock, deviceDataService, engineModelService);
    }

    @Override
    public boolean isCompletion () {
        return true;
    }

    @Override
    protected void toString (JSONWriter writer) throws JSONException {
        super.toString(writer);
        writer.key("execution-completed").value(Boolean.TRUE);
    }

}
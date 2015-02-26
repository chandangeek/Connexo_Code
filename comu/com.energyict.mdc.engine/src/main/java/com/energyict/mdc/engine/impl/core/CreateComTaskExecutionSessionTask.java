package com.energyict.mdc.engine.impl.core;

import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.tasks.ComTask;
import com.energyict.mdc.tasks.ProtocolTask;

/**
 * A ProtocolTask that is solely used by the FrameWork to initiate the creation of a ComTaskExecutionSession.
 *
 * Copyrights EnergyICT
 * Date: 11/19/14
 * Time: 12:05 PM
 */
public class CreateComTaskExecutionSessionTask implements ProtocolTask {

    private final ComTask comTask;
    private final ComTaskExecution comTaskExecution;

    public CreateComTaskExecutionSessionTask(ComTask comTask, ComTaskExecution comTaskExecution) {
        this.comTask = comTask;
        this.comTaskExecution = comTaskExecution;
    }

    @Override
    public ComTask getComTask() {
        return comTask;
    }

    @Override
    public long getId() {
        return 0;
    }

    @Override
    public void save() {
        // intentionally left blank
    }

    public ComTaskExecution getComTaskExecution() {
        return comTaskExecution;
    }
}
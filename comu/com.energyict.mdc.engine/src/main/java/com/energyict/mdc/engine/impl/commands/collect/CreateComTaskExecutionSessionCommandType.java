package com.energyict.mdc.engine.impl.commands.collect;

import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.engine.impl.commands.store.core.CompositeComCommandImpl;
import com.energyict.mdc.engine.impl.core.CreateComTaskExecutionSessionTask;
import com.energyict.mdc.tasks.ComTask;
import com.energyict.mdc.tasks.ProtocolTask;

import java.util.List;

/**
 * Provides an implementation for the {@link ComCommandType} interface
 * for the {@link CreateComTaskExecutionSessionCommand} from the unique identifier
 * of the {@link ComTask} that will be executed by the command
 * such that each different command will be considered a unique
 * ComCommand by the {@link CompositeComCommandImpl} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-11-20 (15:26)
 */
public final class CreateComTaskExecutionSessionCommandType implements ComCommandType {

    private final long comTaskId;

    public CreateComTaskExecutionSessionCommandType(ComTask comTask) {
        super();
        this.comTaskId = comTask.getId();
    }

    @Override
    public void createLegacyCommandsFromProtocolTask(CommandRoot root, List<? extends ProtocolTask> protocolTasks, ProtocolTask protocolTask, ComTaskExecution comTaskExecution) {
        root.getCreateComTaskSessionTask((CreateComTaskExecutionSessionTask) protocolTask, root, comTaskExecution);
    }

    @Override
    public void createCommandsFromTask(CommandRoot root, ProtocolTask protocolTask, ComTaskExecution comTaskExecution) {
        root.getCreateComTaskSessionTask((CreateComTaskExecutionSessionTask) protocolTask, root, comTaskExecution);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        CreateComTaskExecutionSessionCommandType that = (CreateComTaskExecutionSessionCommandType) o;
        return comTaskId == that.comTaskId;
    }

    @Override
    public int hashCode() {
        return (int) (comTaskId ^ (comTaskId >>> 32));
    }

    @Override
    public String toString() {
        return "CREATE_COM_TASK_EXECUTION_SESSION_COMMAND(" + this.comTaskId + ")";
    }

}
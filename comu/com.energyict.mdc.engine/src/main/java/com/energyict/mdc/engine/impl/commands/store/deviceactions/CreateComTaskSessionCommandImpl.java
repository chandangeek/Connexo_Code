package com.energyict.mdc.engine.impl.commands.store.deviceactions;

import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.engine.impl.commands.collect.ComCommandTypes;
import com.energyict.mdc.engine.impl.commands.collect.CommandRoot;
import com.energyict.mdc.engine.impl.commands.collect.CreateComTaskSessionCommand;
import com.energyict.mdc.engine.impl.commands.store.core.SimpleComCommand;
import com.energyict.mdc.engine.impl.core.CreateComTaskSessionTask;
import com.energyict.mdc.engine.impl.core.ExecutionContext;
import com.energyict.mdc.protocol.api.DeviceProtocol;

/**
 * Triggers the ExecutionContext to update the taskExecutionBuilder to create a new ComTaskSession
 *
 * Copyrights EnergyICT
 * Date: 11/19/14
 * Time: 1:21 PM
 */
public class CreateComTaskSessionCommandImpl extends SimpleComCommand implements CreateComTaskSessionCommand{

    private final CreateComTaskSessionTask createComTaskSessionTask;
    private final CommandRoot commandRoot;
    private final ComTaskExecution comTaskExecution;

    public CreateComTaskSessionCommandImpl(CreateComTaskSessionTask createComTaskSessionTask, CommandRoot commandRoot, ComTaskExecution comTaskExecution) {
        super(commandRoot);
        this.createComTaskSessionTask = createComTaskSessionTask;
        this.commandRoot = commandRoot;
        this.comTaskExecution = comTaskExecution;
    }

    @Override
    public void doExecute(DeviceProtocol deviceProtocol, ExecutionContext executionContext) {
        executionContext.start(comTaskExecution, createComTaskSessionTask.getComTask());
    }

    @Override
    public ComCommandTypes getCommandType() {
        return ComCommandTypes.CREATE_COM_TASK_SESSION_COMMAND;
    }
}

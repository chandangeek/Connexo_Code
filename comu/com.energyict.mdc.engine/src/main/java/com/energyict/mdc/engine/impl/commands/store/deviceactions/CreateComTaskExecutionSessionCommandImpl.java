package com.energyict.mdc.engine.impl.commands.store.deviceactions;

import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.engine.impl.commands.collect.ComCommandType;
import com.energyict.mdc.engine.impl.commands.collect.CommandRoot;
import com.energyict.mdc.engine.impl.commands.collect.CreateComTaskExecutionSessionCommand;
import com.energyict.mdc.engine.impl.commands.collect.CreateComTaskExecutionSessionCommandType;
import com.energyict.mdc.engine.impl.commands.store.core.SimpleComCommand;
import com.energyict.mdc.engine.impl.core.CreateComTaskExecutionSessionTask;
import com.energyict.mdc.engine.impl.core.ExecutionContext;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.tasks.ComTask;

/**
 * Triggers the ExecutionContext to create a new {@link com.energyict.mdc.device.data.tasks.history.ComTaskExecutionSession}.
 *
 * Copyrights EnergyICT
 * Date: 11/19/14
 * Time: 1:21 PM
 */
public class CreateComTaskExecutionSessionCommandImpl extends SimpleComCommand implements CreateComTaskExecutionSessionCommand {

    private final CreateComTaskExecutionSessionTask createComTaskExecutionSessionTask;
    private final ComTaskExecution comTaskExecution;

    public CreateComTaskExecutionSessionCommandImpl(CreateComTaskExecutionSessionTask createComTaskExecutionSessionTask, CommandRoot commandRoot, ComTaskExecution comTaskExecution) {
        super(commandRoot);
        this.createComTaskExecutionSessionTask = createComTaskExecutionSessionTask;
        this.comTaskExecution = comTaskExecution;
    }

    @Override
    public ComTask getComTask() {
        return this.createComTaskExecutionSessionTask.getComTask();
    }

    @Override
    public void doExecute(DeviceProtocol deviceProtocol, ExecutionContext executionContext) {
        executionContext.start(comTaskExecution, createComTaskExecutionSessionTask.getComTask());
    }

    @Override
    public String getDescriptionTitle() {
        return "Create communication task session";
    }

    @Override
    public ComCommandType getCommandType() {
        return new CreateComTaskExecutionSessionCommandType(this.getComTask(), comTaskExecution);
    }

}
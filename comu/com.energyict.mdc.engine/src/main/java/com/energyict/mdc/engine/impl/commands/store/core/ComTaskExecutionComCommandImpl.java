package com.energyict.mdc.engine.impl.commands.store.core;


import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.history.CompletionCode;
import com.energyict.mdc.engine.impl.commands.MessageSeeds;
import com.energyict.mdc.engine.impl.commands.collect.ComCommand;
import com.energyict.mdc.engine.impl.commands.collect.ComCommandType;
import com.energyict.mdc.engine.impl.commands.collect.ComCommandTypes;
import com.energyict.mdc.engine.impl.commands.collect.CommandRoot;
import com.energyict.mdc.engine.impl.core.ExecutionContext;
import com.energyict.mdc.engine.impl.logging.LogLevel;
import com.energyict.mdc.engine.impl.meterdata.ComTaskExecutionCollectedData;
import com.energyict.mdc.engine.impl.meterdata.ServerCollectedData;
import com.energyict.mdc.issues.Problem;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.device.data.CollectedData;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Provides an implementation for the {@link ComTaskExecutionComCommand} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-09-17 (12:04)
 */
public class ComTaskExecutionComCommandImpl extends CompositeComCommandImpl implements ComTaskExecutionComCommand {

    private ComTaskExecution comTaskExecution;

    ComTaskExecutionComCommandImpl(GroupedDeviceCommand groupedDeviceCommand, ComTaskExecution comTaskExecution) {
        super(groupedDeviceCommand);
        this.comTaskExecution = comTaskExecution;
    }

    @Override
    public void execute(DeviceProtocol deviceProtocol, ExecutionContext executionContext) {
        doExecute(deviceProtocol, executionContext);
    }

    @Override
    public void doExecute(DeviceProtocol deviceProtocol, ExecutionContext executionContext) {
        try {
            for (ComCommand comCommand : this.getCommands().values()) {
                if (areWeAllowedToPerformTheCommand(comCommand)) {
                    try {
                        comCommand.execute(deviceProtocol, executionContext);
                    } catch (Throwable throwable) {
                        // nothing should get through here, or there must be something seriously wrong ...
                        CommandRoot.ServiceProvider serviceProvider = getGroupedDeviceCommand().getCommandRoot().getServiceProvider();
                        Problem problem = serviceProvider.issueService().newProblem(this, MessageSeeds.SOMETHING_UNEXPECTED_HAPPENED);
                        addIssue(problem, CompletionCode.UnexpectedError);
                        setExecutionState(BasicComCommandBehavior.ExecutionState.FAILED);
                        executionContext.connectionLogger.taskExecutionFailed(throwable, Thread.currentThread().getName(), getComTasksDescription(executionContext));
                        throw throwable;
                    }
                } else {
                    comCommand.setCompletionCode(CompletionCode.NotExecuted);
                }
            }
        } finally {
            if (getProblems().size() > 0) { // if one of my commands failed with an error
                setExecutionState(BasicComCommandBehavior.ExecutionState.FAILED);
                delegateToJournalistIfAny(executionContext);
            } else {
                setExecutionState(BasicComCommandBehavior.ExecutionState.SUCCESSFULLY_EXECUTED);
            }
        }
    }

    private String getComTasksDescription(ExecutionContext executionContext) {
        return executionContext.getComTaskExecution().getComTask().getName();
    }


    @Override
    public List<CollectedData> getCollectedData() {
        List<CollectedData> collectedData = new ArrayList<>();
        collectedData.add(
                new ComTaskExecutionCollectedData(
                        this.comTaskExecution,
                        this.getNestedCollectedData(),
                        this.getCommandRoot().getExecutionContext().getComPort().getComServer().getCommunicationLogLevel(),
                        this.getCommandRoot().isExposeStoringException())
        );
        return collectedData;
    }

    public List<ServerCollectedData> getNestedCollectedData() {
        Set<ServerCollectedData> collectedData = new HashSet<>();
        for (ComCommand command : this.getCommands().values()) {
            List<CollectedData> nestedCollectedData = command.getCollectedData();
            collectedData.addAll(nestedCollectedData.stream().map(data -> (ServerCollectedData) data).collect(Collectors.toList()));
        }
        return new ArrayList<>(collectedData);
    }

    @Override
    public ComCommandType getCommandType() {
        return ComCommandTypes.COM_TASK_ROOT;
    }

    @Override
    public boolean contains(ComCommand comCommand) {
        return this.getCommands().values().contains(comCommand);
    }

    @Override
    public ComTaskExecution getComTaskExecution() {
        return comTaskExecution;
    }

    private boolean areWeAllowedToPerformTheCommand(ComCommand comCommand) {
        return getGroupedDeviceCommand().areWeAllowedToPerformTheCommand(comCommand);
    }

    protected LogLevel defaultJournalingLogLevel() {
        return LogLevel.DEBUG;
    }
}
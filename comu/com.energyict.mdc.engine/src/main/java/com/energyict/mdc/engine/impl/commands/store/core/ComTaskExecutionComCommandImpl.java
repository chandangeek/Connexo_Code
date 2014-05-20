package com.energyict.mdc.engine.impl.commands.store.core;


import com.elster.jupiter.transaction.TransactionService;
import com.energyict.mdc.engine.impl.commands.collect.ComCommand;
import com.energyict.mdc.engine.impl.commands.collect.ComCommandTypes;
import com.energyict.mdc.engine.impl.commands.collect.CommandRoot;
import com.energyict.mdc.engine.impl.core.ExecutionContext;
import com.energyict.mdc.engine.impl.logging.LogLevel;
import com.energyict.mdc.engine.impl.meterdata.ComTaskExecutionCollectedData;
import com.energyict.mdc.engine.impl.meterdata.ServerCollectedData;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.device.data.CollectedData;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Provides an implementation for the {@link ComTaskExecutionComCommand} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-09-17 (12:04)
 */
public class ComTaskExecutionComCommandImpl extends CompositeComCommandImpl implements ComTaskExecutionComCommand {

    private final TransactionService transactionService;
    private ComTaskExecution comTaskExecution;

    public ComTaskExecutionComCommandImpl(CommandRoot commandRoot, TransactionService transactionService, ComTaskExecution comTaskExecution) {
        super(commandRoot);
        this.transactionService = transactionService;
        this.comTaskExecution = comTaskExecution;
    }

    @Override
    public List<CollectedData> getCollectedData () {
        List<CollectedData> collectedData = new ArrayList<>();
        collectedData.add(
                new ComTaskExecutionCollectedData(
                        transactionService, this.comTaskExecution,
                        this.getNestedCollectedData(),
                        this.getCommandRoot().getExecutionContext().getComPort().getComServer().getCommunicationLogLevel()));
        return collectedData;
    }

    public List<ServerCollectedData> getNestedCollectedData () {
        Set<ServerCollectedData> collectedData = new HashSet<>();
        for (ComCommand command : this.getCommands().values()) {
            List<CollectedData> nestedCollectedData = command.getCollectedData();
            for (CollectedData data : nestedCollectedData) {
                collectedData.add((ServerCollectedData) data);
            }
        }
        return new ArrayList<>(collectedData);
    }

    @Override
    public ComCommandTypes getCommandType () {
        return ComCommandTypes.COM_TASK_ROOT;
    }

    @Override
    public boolean contains (ComCommand comCommand) {
        return this.getCommands().values().contains(comCommand);
    }

    @Override
    public void doExecute (DeviceProtocol deviceProtocol, ExecutionContext executionContext) {
        /* All contained ComCommands are in fact also contained in the root
         * and will be executed as part of the execution of the root.
         * My only purpose in life is to group them
         */
    }

    protected LogLevel defaultJournalingLogLevel () {
        return LogLevel.DEBUG;
    }

}
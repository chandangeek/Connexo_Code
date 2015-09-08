package com.energyict.mdc.engine.impl.commands.store.core;

import com.energyict.mdc.common.ComServerRuntimeException;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.engine.exceptions.ComCommandException;
import com.energyict.mdc.engine.impl.MessageSeeds;
import com.energyict.mdc.engine.impl.commands.collect.ComCommand;
import com.energyict.mdc.engine.impl.commands.collect.ComCommandType;
import com.energyict.mdc.engine.impl.commands.collect.ComCommandTypes;
import com.energyict.mdc.engine.impl.commands.collect.CommandRoot;
import com.energyict.mdc.engine.impl.commands.collect.CompositeComCommand;
import com.energyict.mdc.engine.impl.commands.collect.CreateComTaskExecutionSessionCommand;
import com.energyict.mdc.engine.impl.core.ExecutionContext;
import com.energyict.mdc.io.ConnectionCommunicationException;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.exceptions.ConnectionFailureException;
import com.energyict.mdc.protocol.api.exceptions.ConnectionSetupException;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A CompositeComCommand can contain several {@link ComCommand ComCommands} which are executed in the order the
 * {@link ComCommand} itself desires. We are responsible for creating/fetching our own {@link ComCommand ComCommands}.
 * We are also responsible for making sure that all {@link ComCommand ComCommands} in the CommandRoot
 * are unique by ComCommandType, if not a
 * ComCommandException#uniqueCommandViolation must be thrown.<br/>
 * The {@link SimpleComCommand#doExecute(DeviceProtocol, com.energyict.mdc.engine.impl.core.ExecutionContext)} will call the {@link ComCommand#execute(DeviceProtocol, com.energyict.mdc.engine.impl.core.ExecutionContext)} of all the
 * {@link ComCommand commands} in the {@link #comCommands commandList} <b>in chronological order.</b>
 *
 * @author gna
 * @since 10/05/12 - 8:33
 */
public abstract class CompositeComCommandImpl extends SimpleComCommand implements CompositeComCommand {

    /**
     * Contains all necessary commands for this {@link ComCommand}.
     * <b>It is necessary to use a LinkedHashMap because we need the commands in chronological order</b>
     */
    private Map<ComCommandType, ComCommand> comCommands = new LinkedHashMap<>();

    protected CompositeComCommandImpl(final CommandRoot commandRoot) {
        super(commandRoot);
    }

    @Override
    public void doExecute (final DeviceProtocol deviceProtocol, ExecutionContext executionContext) {
        ComServerRuntimeException firstException = null;
        boolean canWeStillDoADisconnect = true;
        for (Map.Entry<ComCommandType, ComCommand> comCommandEntry : comCommands.entrySet()) {
            final ComCommand comCommand = comCommandEntry.getValue();
            final ComCommandType commandType = comCommandEntry.getKey();
            if(areWeAllowedToPerformTheCommand(firstException, canWeStillDoADisconnect, commandType, executionContext.basickCheckHasFailed())){
                try {
                    performTheComCommandIfAllowed(deviceProtocol, executionContext, comCommand);
                } catch (ComServerRuntimeException e) {
                    if(firstException == null){
                        firstException = e;
                    }
                    canWeStillDoADisconnect = areWeStillAbleToPerformAProperDisconnect(e);
                }
            }
        }
        if (firstException != null) {
            throw firstException;
        }
    }

    /**
     * We are allowed to perform the command if:
     * <ul>
     *     <li>We didn't have any exception and the BasicCheckTask hasn't failed</li>
     *     <li>Or the command is a logOff related command and we didn't get a Connection related exception</li>
     * </ul>
     *
     * @param firstException the firstException
     * @param canWeStillDoADisconnect indication whether the firstException was related to Communication
     * @param commandType the type fo the Command
     * @param hasBasicCheckFailed The flag that indicates if the basic check task has failed before
     * @return true or false
     */
    private boolean areWeAllowedToPerformTheCommand(ComServerRuntimeException firstException, boolean canWeStillDoADisconnect, ComCommandType commandType, boolean hasBasicCheckFailed) {
        return (firstException == null && !hasBasicCheckFailed) || areWeStillAllowedToPerformTheCommand(commandType, canWeStillDoADisconnect);
    }

    protected void performTheComCommandIfAllowed(DeviceProtocol deviceProtocol, ExecutionContext executionContext, ComCommand comCommand) {
        comCommand.execute(deviceProtocol, executionContext);
    }

    /**
     * We are allowed to do logOffs is the exception wasn't caused by a connection error.
     * The terminate and updateDeviceCache are safe to still perform.
     *
     * @param commandType the type of the comCommand
     * @param canWeStillDoADisconnect an indication whether we can still communicate (no connection exceptions)
     * @return true if we can perform the command of the given commandType
     */
    private boolean areWeStillAllowedToPerformTheCommand(ComCommandType commandType, boolean canWeStillDoADisconnect) {
        return (    canWeStillDoADisconnect
                && (commandType.equals(ComCommandTypes.DAISY_CHAINED_LOGOFF) || commandType.equals(ComCommandTypes.LOGOFF)))
            || commandType.equals(ComCommandTypes.DEVICE_PROTOCOL_TERMINATE)
            || commandType.equals(ComCommandTypes.DEVICE_PROTOCOL_UPDATE_CACHE_COMMAND);
    }

    private boolean areWeStillAbleToPerformAProperDisconnect(Exception e) {
        return !ConnectionFailureException.class.isAssignableFrom(e.getClass())
                && !ConnectionSetupException.class.isAssignableFrom(e.getClass())
                && !ConnectionCommunicationException.class.isAssignableFrom(e.getClass());
    }

    @Override
    public void addUniqueCommand(ComCommand command, ComTaskExecution comTaskExecution) {
        if (checkCommandTypeExistence(command.getCommandType(), getCommandRoot().getCommands())) {
            throw ComCommandException.uniqueCommandViolation(command, MessageSeeds.COMMAND_NOT_UNIQUE);
        }
        this.doAddCommand(command);
    }

    @Override
    public void addCommand(CreateComTaskExecutionSessionCommand command, ComTaskExecution comTaskExecution) {
        this.doAddCommand(command);
    }

    private void doAddCommand(ComCommand command) {
        this.comCommands.putIfAbsent(command.getCommandType(), command);
    }

    @Override
    public boolean checkCommandTypeExistence(final ComCommandType comCommandType, final Map<ComCommandType, ComCommand> allCommands) {
        if (allCommands.containsKey(comCommandType)) {
            return true;
        }
        boolean exists = false;
        for (ComCommand command : allCommands.values()) {
            if (command instanceof CompositeComCommand) {
                exists |= checkCommandTypeExistence(comCommandType, ((CompositeComCommand) command).getCommands());
            }
        }
        return exists;
    }

    /**
     * Get the List of ComCommands
     *
     * @return the requested list of ComCommands
     */
    public Map<ComCommandType, ComCommand> getCommands() {
        return this.comCommands;
    }

    @Override
    public Iterator<ComCommand> iterator() {
        return this.comCommands.values().iterator();
    }

}
package com.energyict.mdc.engine.impl.commands.store.core;

import com.energyict.mdc.common.ComServerRuntimeException;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.engine.exceptions.ComCommandException;
import com.energyict.mdc.engine.impl.MessageSeeds;
import com.energyict.mdc.engine.impl.commands.collect.ComCommand;
import com.energyict.mdc.engine.impl.commands.collect.ComCommandKey;
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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

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
    private Map<ComCommandKey, ComCommand> comCommands = new LinkedHashMap<>();

    protected CompositeComCommandImpl(final CommandRoot commandRoot) {
        super(commandRoot);
    }

    @Override
    public void doExecute (final DeviceProtocol deviceProtocol, ExecutionContext executionContext) {
        ComServerRuntimeException firstException = null;
        boolean canWeStillDoADisconnect = true;
        for (Map.Entry<ComCommandKey, ComCommand> comCommandEntry : comCommands.entrySet()) {
            final ComCommand comCommand = comCommandEntry.getValue();
            final ComCommandKey commandKey = comCommandEntry.getKey();
            if (areWeAllowedToPerformTheCommand(firstException, canWeStillDoADisconnect, commandKey, executionContext.basickCheckHasFailed())) {
                try {
                    performTheComCommandIfAllowed(deviceProtocol, executionContext, comCommand);
                } catch (ComServerRuntimeException e) {
                    if (firstException == null) {
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
     * @param commandKey the type fo the Command
     * @param hasBasicCheckFailed The flag that indicates if the basic check task has failed before
     * @return true or false
     */
    private boolean areWeAllowedToPerformTheCommand(ComServerRuntimeException firstException, boolean canWeStillDoADisconnect, ComCommandKey commandKey, boolean hasBasicCheckFailed) {
        return (firstException == null && !hasBasicCheckFailed) || areWeStillAllowedToPerformTheCommand(commandKey, canWeStillDoADisconnect);
    }

    protected void performTheComCommandIfAllowed(DeviceProtocol deviceProtocol, ExecutionContext executionContext, ComCommand comCommand) {
        comCommand.execute(deviceProtocol, executionContext);
    }

    /**
     * We are allowed to do logOffs is the exception wasn't caused by a connection error.
     * The terminate and updateDeviceCache are safe to still perform.
     *
     * @param commandKey the ComCommandKey
     * @param canWeStillDoADisconnect an indication whether we can still communicate (no connection exceptions)
     * @return true if we can perform the command of the given commandKey
     */
    private boolean areWeStillAllowedToPerformTheCommand(ComCommandKey commandKey, boolean canWeStillDoADisconnect) {
        return (    canWeStillDoADisconnect
                && (commandKey.equals(ComCommandTypes.DAISY_CHAINED_LOGOFF) || commandKey.equals(ComCommandTypes.LOGOFF)))
            || commandKey.equals(ComCommandTypes.DEVICE_PROTOCOL_TERMINATE)
            || commandKey.equals(ComCommandTypes.DEVICE_PROTOCOL_UPDATE_CACHE_COMMAND);
    }

    private boolean areWeStillAbleToPerformAProperDisconnect(Exception e) {
        return !ConnectionFailureException.class.isAssignableFrom(e.getClass())
                && !ConnectionSetupException.class.isAssignableFrom(e.getClass())
                && !ConnectionCommunicationException.class.isAssignableFrom(e.getClass());
    }

    @Override
    public void addUniqueCommand(ComCommand command, ComTaskExecution comTaskExecution) {
        ComCommandKey key = new ComCommandKey(command.getCommandType(), comTaskExecution, getCommandRoot().getSecuritySetCommandGroupId());
        if (this.commandAlreadyExists(key)) {
            throw ComCommandException.uniqueCommandViolation(command, MessageSeeds.COMMAND_NOT_UNIQUE);
        }
        else {
            this.doAddCommand(key, command);
        }
    }

    @Override
    public void addCommand(CreateComTaskExecutionSessionCommand command, ComTaskExecution comTaskExecution) {
        ComCommandKey key = new ComCommandKey(command.getCommandType(), comTaskExecution, getCommandRoot().getSecuritySetCommandGroupId());
        this.doAddCommand(key, command);
    }

    private void doAddCommand(ComCommandKey key, ComCommand command) {
        this.comCommands.putIfAbsent(key, command);
    }

    @Override
    public boolean contains(ComCommand comCommand) {
        return this.comCommands.values().contains(comCommand);
    }

    @Override
    public boolean commandAlreadyExists(ComCommandKey comCommandKey) {
        return this.getExistingCommand(comCommandKey).isPresent();
    }

    @Override
    public Optional<ComCommand> getExistingCommand(ComCommandKey key) {
        for (ComCommandKey comCommandTypeAndId : this.comCommands.keySet()) {
            ComCommand candidate = this.comCommands.get(comCommandTypeAndId);
            if (key.equalsIgnoreComTaskExecution(comCommandTypeAndId)) {
                return Optional.of(candidate);
            }
            if (candidate instanceof CompositeComCommand) {
                CompositeComCommand otherComposite = (CompositeComCommand) candidate;
                Optional<ComCommand> subCommand = otherComposite.getExistingCommand(key);
                if (subCommand.isPresent()) {
                    return subCommand;
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public List<ComCommand> getExistingCommandsOfType(ComCommandType type) {
        List<ComCommand> matchingCommands = new ArrayList<>();
        for (ComCommandKey comCommandTypeAndId : this.comCommands.keySet()) {
            ComCommand candidate = this.comCommands.get(comCommandTypeAndId);
            if (type.equals(comCommandTypeAndId.getCommandType())) {
                matchingCommands.add(candidate);
            }
            if (candidate instanceof CompositeComCommand) {
                CompositeComCommand otherComposite = (CompositeComCommand) candidate;
                matchingCommands.addAll(otherComposite.getExistingCommandsOfType(type));
            }
        }
        return matchingCommands;
    }

    @Override
    public List<ComCommandType> getCommandTypes() {
        return this.comCommands
                .keySet()
                .stream()
                .map(ComCommandKey::getCommandType)
                .collect(Collectors.toList());
    }

    @Override
    public List<ComCommand> getCommands() {
        return new ArrayList<>(this.comCommands.values());
    }

    @Override
    public Iterator<ComCommand> iterator() {
        return this.comCommands.values().iterator();
    }

    protected void removeCommandOfType(ComCommandType type) {
        Set<ComCommandKey> keys = new HashSet<>(this.comCommands.keySet());
        keys
            .stream()
            .filter(key -> key.getCommandType().equals(type))
            .forEach(this.comCommands::remove);
    }

    protected static void copyComCommands(CompositeComCommandImpl source, CompositeComCommandImpl target, Set<ComCommandTypes> unneccesary) {
        target.comCommands.putAll(source.comCommands);
        unneccesary.stream().forEach(target::removeCommandOfType);
    }
}
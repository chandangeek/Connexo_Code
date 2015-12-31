package com.energyict.mdc.engine.impl.commands.collect;

import com.energyict.mdc.device.data.tasks.ComTaskExecution;

import java.util.List;
import java.util.Optional;

/**
 * A CompositeComCommand can contain several {@link ComCommand}s which are executed in the order the
 * {@link ComCommand} itself desires. We are responsible for creating/fetching our own {@link ComCommand}s.
 * We are also responsible for making sure that all {@link ComCommand}s in the {@link CommandRoot root}
 * are unique by {@link ComCommandType}, if not an exception will be thrown.
 * The only exception to that rule is the {@link CreateComTaskExecutionSessionCommand}
 * that can appear multiple times, one for every {@link com.energyict.mdc.tasks.ComTask}
 * that is part of a {@link com.energyict.mdc.scheduling.model.ComSchedule}.
 *
 * @author gna
 * @since 10/05/12 - 16:03
 */
public interface CompositeComCommand extends Iterable<ComCommand>, ComCommand {

    /**
     * Adds the given ComCommand to the command list.<br/>
     * <b>Note:</b> a ComCommandException#uniqueCommandViolation(ComCommand) will be thrown
     * if the given {@link ComCommand#getCommandType()} already exists in the {@link CommandRoot}.
     *
     * @param command the command to add
     * @param comTaskExecution the referred ComTaskExecution
     */
    void addUniqueCommand(final ComCommand command, ComTaskExecution comTaskExecution);

    /**
     * Adds the given CreateComTaskExecutionSessionCommand to the command list.
     *
     * @param command the command to add
     * @param comTaskExecution the referred ComTaskExecution
     */
    void addCommand(CreateComTaskExecutionSessionCommand command, ComTaskExecution comTaskExecution);

    List<ComCommandType> getCommandTypes();

    List<ComCommand> getCommands();

    boolean contains(ComCommand comCommand);

    /**
     * Checks whether the given argument already exists in the current root.
     *
     * @param comCommandType the {@link ComCommandTypes} to check for existence
     * @return true if the ComCommand type already exists, false otherwise
     */
    boolean commandAlreadyExists(ComCommandKey comCommandType);

    /**
     * Finds the existing {@link ComCommand} with the given {@link ComCommandKey}.
     *
     * @param key The ComCommandKey
     * @return the requested ComCommand or Optional.empty if the ComCommand does not exist yet
     */
    Optional<ComCommand> getExistingCommand(ComCommandKey key);

    /**
     * Finds the existing {@link ComCommand}s of the given {@link ComCommandType}.
     *
     * @param type The ComCommandType
     * @return The List of ComCommand that are of the specified type
     */
    List<ComCommand> getExistingCommandsOfType(ComCommandType type);

}
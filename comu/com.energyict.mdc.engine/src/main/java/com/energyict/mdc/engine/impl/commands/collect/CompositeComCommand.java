package com.energyict.mdc.engine.impl.commands.collect;

import com.energyict.mdc.device.data.tasks.ComTaskExecution;

import java.util.Map;

/**
 * A CompositeComCommand can contain several {@link ComCommand ComCommands} which are executed in the order the
 * {@link ComCommand} itself desires. We are responsible for creating/fetching our own {@link ComCommand ComCommands}.
 * We are also responsible for making sure that all {@link ComCommand ComCommands} in the {@link CommandRoot root}
 * are unique by {@link ComCommandTypes ComCommandType}, if not an exception will be thrown.
 *
 * @author gna
 * @since 10/05/12 - 16:03
 */
public interface CompositeComCommand extends Iterable<ComCommand>, ComCommand {

    /**
     * Add the given ComCommand to the command list.<br/>
     * <b>Note:</b> a {@link com.energyict.comserver.exceptions.ComCommandException#uniqueCommandViolation(ComCommand)} will be thrown
     * if the given {@link ComCommand#getCommandType()} already exists in the {@link CommandRoot}.
     *
     * @param command the command to add
     * @param comTaskExecution the referred ComTaskExecution
     */
    public void addCommand(final ComCommand command, ComTaskExecution comTaskExecution);

    /**
     * Get the List of ComCommands
     *
     * @return the requested list of ComCommands
     */
    public Map<ComCommandTypes,ComCommand> getCommands();

    /**
     * Checks whether the given argument already exists in the current root.
     *
     * @param comCommandType the {@link ComCommandTypes} to check for existence
     * @return true if the ComCommand type already exists, false otherwise
     */
    public boolean checkCommandTypeExistence(final ComCommandTypes comCommandType, final Map<ComCommandTypes,ComCommand> comCommands);

}

/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.commands.collect;

import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.engine.exceptions.ComCommandException;

import java.util.Map;

/**
 * A CompositeComCommand can contain several {@link ComCommand}s which are executed in the order the
 * {@link ComCommand} itself desires. We are responsible for creating/fetching our own {@link ComCommand}s.
 * We are also responsible for making sure that all {@link ComCommand}s in the {@link CommandRoot root}
 * are unique by {@link ComCommandType}, if not an exception will be thrown.
 *
 * @author gna
 * @since 10/05/12 - 16:03
 */
public interface CompositeComCommand extends Iterable<ComCommand>, ComCommand {

    /**
     * Add the given ComCommand to the command list.<br/>
     * <b>Note:</b> a {@link ComCommandException} will be thrown
     * if the given {@link ComCommand#getCommandType()} already exists in the {@link CommandRoot} for the same device.
     *
     * @param command          the command to add
     * @param comTaskExecution the referred ComTaskExecution
     */
    public void addCommand(final ComCommand command, ComTaskExecution comTaskExecution);

    /**
     * Get the List of ComCommands
     *
     * @return the requested list of ComCommands
     */
    public Map<ComCommandType, ComCommand> getCommands();

}
package com.energyict.mdc.engine.impl.commands.store.core;

import com.energyict.mdc.commands.ComCommand;
import com.energyict.mdc.commands.CompositeComCommand;

/**
 * Models a {@link CompositeComCommand} that contains all the {@link com.energyict.mdc.commands.ComCommand}s
 * that relate to the same {@link com.energyict.mdc.tasks.ComTaskExecution}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-09-17 (14:15)
 */
public interface ComTaskExecutionComCommand extends CompositeComCommand {

    public boolean contains (ComCommand comCommand);

}
package com.energyict.mdc.engine.impl.commands.collect;

import com.energyict.mdc.tasks.RegistersTask;

/**
 * Command to collect the Registers defined on the {@link com.energyict.mdc.tasks.RegistersTask}.
 *
 * @author gna
 * @since 12/06/12 - 10:57
 */
public interface RegisterCommand extends CompositeComCommand {

    /**
     * The RegistersTask which is used for modeling this command
     *
     * @return the {@link com.energyict.mdc.tasks.RegistersTask}
     */
    public RegistersTask getRegistersTask();
}

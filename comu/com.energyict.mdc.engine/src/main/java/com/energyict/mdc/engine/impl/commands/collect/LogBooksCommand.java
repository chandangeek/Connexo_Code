package com.energyict.mdc.engine.impl.commands.collect;

import com.energyict.mdc.tasks.LogBooksTask;

/**
 * The {@link ComCommand} which can perform the necessary actions to read all the logBooks from the device
 *
 * @author sva
 * @since 07/12/12 - 11:33
 */
public interface LogBooksCommand extends CompositeComCommand {

    /**
     * The LogBooksTask which is used for modeling this command
     *
     * @return the {@link com.energyict.mdc.tasks.LogBooksTask}
     */
    public LogBooksTask getLogBooksTask();

}

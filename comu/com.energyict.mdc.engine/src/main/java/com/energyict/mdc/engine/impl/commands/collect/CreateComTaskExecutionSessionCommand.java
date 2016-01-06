package com.energyict.mdc.engine.impl.commands.collect;

import com.energyict.mdc.tasks.ComTask;

/**
 * The ComCommand that will create a new
 * {@link com.energyict.mdc.device.data.tasks.history.ComTaskExecutionSession}
 * for a {@link ComTask}.
 *
 * Copyrights EnergyICT
 * Date: 11/19/14
 * Time: 1:17 PM
 */
public interface CreateComTaskExecutionSessionCommand extends ComCommand {

    ComTask getComTask();

}
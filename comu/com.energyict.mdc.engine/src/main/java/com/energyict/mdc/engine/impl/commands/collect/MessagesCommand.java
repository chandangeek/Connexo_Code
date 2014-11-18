package com.energyict.mdc.engine.impl.commands.collect;

import com.energyict.mdc.tasks.MessagesTask;

/**
 * Command to execute DeviceMessages of a certain DeviceProtocol
 *
 * Copyrights EnergyICT
 * Date: 11/17/14
 * Time: 2:37 PM
 */
public interface MessagesCommand extends ComCommand {
    /**
     * The ProtocolTask which is used for modeling this command.
     *
     * @return the {@link MessagesTask}
     */
    public MessagesTask getMessagesTask();
}

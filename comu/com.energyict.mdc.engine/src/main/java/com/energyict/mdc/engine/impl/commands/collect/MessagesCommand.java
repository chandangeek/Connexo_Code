package com.energyict.mdc.engine.impl.commands.collect;

import com.energyict.mdc.protocol.api.device.messages.DeviceMessage;
import com.energyict.mdc.tasks.MessagesTask;

/**
 * Command to execute {@link DeviceMessage deviceMessages} of a certain {@link com.energyict.mdc.messages.DeviceMessageCategory}
 * <p/>
 * Copyrights EnergyICT
 * Date: 21/06/12
 * Time: 13:32
 */
public interface MessagesCommand extends ComCommand {

    /**
     * The {@link com.energyict.mdc.tasks.ProtocolTask} which is used for modeling this command.
     *
     * @return the {@link MessagesTask}
     */
    public MessagesTask getMessagesTask();

}
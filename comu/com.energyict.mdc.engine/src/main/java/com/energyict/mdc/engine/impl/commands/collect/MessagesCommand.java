package com.energyict.mdc.engine.impl.commands.collect;

import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.engine.impl.commands.store.core.GroupedDeviceCommand;
import com.energyict.mdc.tasks.MessagesTask;

/**
 * Command to execute DeviceMessages of a certain DeviceProtocol
 * <p>
 * Copyrights EnergyICT
 * Date: 11/17/14
 * Time: 2:37 PM
 */
public interface MessagesCommand extends ComCommand {

    void updateAccordingTo(MessagesTask messagesTask, GroupedDeviceCommand groupedDeviceCommand, ComTaskExecution comTaskExecution);

}
/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.commands.collect;

import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.engine.impl.commands.store.core.GroupedDeviceCommand;
import com.energyict.mdc.tasks.MessagesTask;

public interface MessagesCommand extends ComCommand {

    void updateAccordingTo(MessagesTask messagesTask, GroupedDeviceCommand groupedDeviceCommand, ComTaskExecution comTaskExecution);

}
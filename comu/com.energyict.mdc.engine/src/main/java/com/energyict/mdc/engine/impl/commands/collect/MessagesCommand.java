/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.commands.collect;

import com.energyict.mdc.common.tasks.ComTaskExecution;
import com.energyict.mdc.common.tasks.MessagesTask;
import com.energyict.mdc.engine.impl.commands.store.core.GroupedDeviceCommand;

public interface MessagesCommand extends ComCommand {

    void updateAccordingTo(MessagesTask messagesTask, GroupedDeviceCommand groupedDeviceCommand, ComTaskExecution comTaskExecution);

    boolean hasPendingMessages();
}
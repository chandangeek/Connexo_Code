/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.commands.collect;

import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.engine.impl.commands.store.core.GroupedDeviceCommand;
import com.energyict.mdc.tasks.ProtocolTask;

import java.util.List;

/**
 * Models the type of a {@link ComCommand}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-11-20 (15:25)
 */
public interface ComCommandType {

    void createLegacyCommandsFromProtocolTask(GroupedDeviceCommand groupedDeviceCommand, List<ProtocolTask> protocolTasks, ProtocolTask protocolTask, ComTaskExecution comTaskExecution);

    void createCommandsFromTask(GroupedDeviceCommand groupedDeviceCommand, ProtocolTask protocolTask, ComTaskExecution comTaskExecution);

}
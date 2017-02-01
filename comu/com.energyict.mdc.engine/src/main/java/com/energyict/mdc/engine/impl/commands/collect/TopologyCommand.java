/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.commands.collect;

import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.engine.impl.commands.store.core.GroupedDeviceCommand;
import com.energyict.mdc.protocol.api.tasks.TopologyAction;
import com.energyict.mdc.tasks.TopologyTask;

/**
 * The {@link ComCommand} which can perform the actions necessary for a {@link com.energyict.mdc.tasks.TopologyTask}
 *
 * @author gna
 * @since 9/05/12 - 15:06
 */
public interface TopologyCommand extends ComCommand {

    public TopologyAction getTopologyAction();

    public void updateAccordingTo(TopologyTask topologyTask, GroupedDeviceCommand groupedDeviceCommand, ComTaskExecution comTaskExecution);

}
/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.commands.collect;

import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.masterdata.RegisterGroup;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.tasks.RegistersTask;

/**
 * Command to collect the Registers defined on the {@link com.energyict.mdc.tasks.RegistersTask}.
 *
 * @author gna
 * @since 12/06/12 - 10:57
 */
public interface RegisterCommand extends CompositeComCommand {

    /**
     * Add additional {@link RegisterGroup registerGroups} which need to be collected from the device
     *
     * @param registersTask    the RegistersTask containing the additional registerGroups which also need to be collected
     * @param offlineDevice    the offline variant of the master device
     * @param comTaskExecution the comTaskExecution for which registers needs to be collected
     */
    public void addAdditionalRegisterGroups(RegistersTask registersTask, OfflineDevice offlineDevice, ComTaskExecution comTaskExecution);

}
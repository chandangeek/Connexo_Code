/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.commands.store;

import com.elster.jupiter.util.time.StopWatch;
import com.energyict.mdc.common.comserver.logging.CanProvideDescriptionTitle;
import com.energyict.mdc.common.tasks.ConnectionTask;
import com.energyict.mdc.common.tasks.history.ComSession;
import com.energyict.mdc.device.data.tasks.history.ComSessionBuilder;

/**
 * Models a {@link DeviceCommand} that will create a ComSession
 * that contains the details of a complete communication session.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-09-17 (15:43)
 */
public interface CreateComSessionDeviceCommand extends DeviceCommand, DeviceCommand.ExecutionLogger, CanProvideDescriptionTitle {

    public ComSessionBuilder getComSessionBuilder();

    public ConnectionTask getConnectionTask();

    public ComSession getComSession();

    public void setStopWatch(StopWatch stopWatch);

    /**
     * We allow to update the successIndicator in case of failures of storage etc.
     * @param successIndicator the new successIndicator
     */
    void updateSuccessIndicator(ComSession.SuccessIndicator successIndicator);
}
package com.energyict.mdc.engine.impl.core;

import com.energyict.mdc.engine.impl.commands.store.DeviceCommandExecutor;
import com.energyict.mdc.engine.model.ComServer;

/**
 * Serves as an implementation of a ScheduledJobExecutor for a single run.
 *
 * Copyrights EnergyICT
 * Date: 9/17/13
 * Time: 11:18 AM
 */
public class SingleThreadedScheduledJobExecutor extends ScheduledJobExecutor {

    public SingleThreadedScheduledJobExecutor(ScheduledJobTransactionExecutor transactionExecutor, ComServer.LogLevel logLevel, DeviceCommandExecutor deviceCommandExecutor) {
        super(transactionExecutor, logLevel, deviceCommandExecutor);
    }
}

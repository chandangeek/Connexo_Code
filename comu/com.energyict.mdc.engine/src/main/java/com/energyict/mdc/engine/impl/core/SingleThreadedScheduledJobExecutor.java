package com.energyict.mdc.engine.impl.core;

import com.elster.jupiter.transaction.TransactionService;
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

    public SingleThreadedScheduledJobExecutor(TransactionService transactionService, ComServer.LogLevel logLevel, DeviceCommandExecutor deviceCommandExecutor) {
        super(transactionService, logLevel, deviceCommandExecutor);
    }
}

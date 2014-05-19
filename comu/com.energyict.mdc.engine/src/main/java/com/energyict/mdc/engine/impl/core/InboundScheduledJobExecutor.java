package com.energyict.mdc.engine.impl.core;

import com.elster.jupiter.transaction.TransactionService;
import com.energyict.mdc.engine.impl.commands.store.DeviceCommandExecutor;
import com.energyict.mdc.engine.model.ComServer;

/**
 * Serves as an implementation of a ScheduledJobExecutor for an Inbound job
 *
 * Copyrights EnergyICT
 * Date: 9/17/13
 * Time: 11:19 AM
 */
public class InboundScheduledJobExecutor extends ScheduledJobExecutor {

    public InboundScheduledJobExecutor(TransactionService transactionExecutor, ComServer.LogLevel logLevel, DeviceCommandExecutor deviceCommandExecutor) {
        super(transactionExecutor, logLevel, deviceCommandExecutor);
    }
}

package com.energyict.mdc.engine.impl.core;

import com.energyict.comserver.commands.DeviceCommandExecutor;
import com.energyict.mdc.engine.model.ComServer;

/**
 * Serves as an implementation of a ScheduledJobExecutor for an Inbound job
 *
 * Copyrights EnergyICT
 * Date: 9/17/13
 * Time: 11:19 AM
 */
public class InboundScheduledJobExecutor extends ScheduledJobExecutor {

    public InboundScheduledJobExecutor(ScheduledJobTransactionExecutor transactionExecutor, ComServer.LogLevel logLevel, DeviceCommandExecutor deviceCommandExecutor) {
        super(transactionExecutor, logLevel, deviceCommandExecutor);
    }
}

/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.core;

import com.elster.jupiter.transaction.TransactionService;
import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.engine.impl.commands.store.DeviceCommandExecutor;

public class SingleThreadedScheduledJobExecutor extends ScheduledJobExecutor {

    public SingleThreadedScheduledJobExecutor(TransactionService transactionService, ComServer.LogLevel logLevel, DeviceCommandExecutor deviceCommandExecutor) {
        super(transactionService, logLevel, deviceCommandExecutor);
    }
}

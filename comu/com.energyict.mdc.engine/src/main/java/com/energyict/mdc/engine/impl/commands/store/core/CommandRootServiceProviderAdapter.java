package com.energyict.mdc.engine.impl.commands.store.core;

import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.time.Clock;
import com.energyict.mdc.device.data.DeviceDataService;
import com.energyict.mdc.engine.impl.commands.collect.CommandRoot;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;
import com.energyict.mdc.tasks.history.TaskHistoryService;

/**
* Copyrights EnergyICT
* Date: 19/05/2014
* Time: 13:45
*/
public class CommandRootServiceProviderAdapter implements CommandRoot.ServiceProvider {
    private final com.energyict.mdc.engine.impl.core.ServiceProvider delegate;

    public CommandRootServiceProviderAdapter(com.energyict.mdc.engine.impl.core.ServiceProvider delegate) {
        this.delegate = delegate;
    }

    @Override
    public IssueService getIssueService() {
        return delegate.issueService();
    }

    @Override
    public Clock getClock() {
        return delegate.clock();
    }

    @Override
    public DeviceDataService getDeviceDataService() {
        return delegate.deviceDataService();
    }

    @Override
    public MdcReadingTypeUtilService getMdcReadingTypeUtilService() {
        return delegate.mdcReadingTypeUtilService();
    }

    @Override
    public TaskHistoryService getTaskHistoryService() {
        return delegate.taskHistoryService();
    }

    @Override
    public TransactionService transactionService() {
        return delegate.transactionService();
    }
}

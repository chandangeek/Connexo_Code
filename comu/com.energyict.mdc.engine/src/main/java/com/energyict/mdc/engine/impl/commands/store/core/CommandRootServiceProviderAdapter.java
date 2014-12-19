package com.energyict.mdc.engine.impl.commands.store.core;

import com.elster.jupiter.transaction.TransactionService;
import java.time.Clock;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.LogBookService;
import com.energyict.mdc.engine.impl.commands.collect.CommandRoot;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;
import com.energyict.mdc.protocol.api.services.IdentificationService;

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
    public IssueService issueService() {
        return delegate.issueService();
    }

    @Override
    public Clock clock() {
        return delegate.clock();
    }

    @Override
    public LogBookService logBookService() {
        return delegate.logBookService();
    }

    @Override
    public DeviceService deviceService() {
        return delegate.deviceService();
    }

    @Override
    public MdcReadingTypeUtilService mdcReadingTypeUtilService() {
        return delegate.mdcReadingTypeUtilService();
    }

    @Override
    public TransactionService transactionService() {
        return delegate.transactionService();
    }

    @Override
    public IdentificationService identificationService() {
        return delegate.identificationService();
    }
}

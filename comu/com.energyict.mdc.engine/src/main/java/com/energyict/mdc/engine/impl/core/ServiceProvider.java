package com.energyict.mdc.engine.impl.core;

import com.energyict.mdc.device.data.DeviceDataService;
import com.energyict.mdc.engine.EngineService;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;
import com.energyict.mdc.protocol.api.services.HexService;

import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.time.Clock;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Provides acces to the OSGi services that are needed by
 * the core ComServer components.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-05-08 (09:32)
 */
public interface ServiceProvider {

    public final AtomicReference<ServiceProvider> instance = new AtomicReference<>();

    public TransactionService transactionService();

    public Clock clock();

    public IssueService issueService();

    public HexService hexService();

    public DeviceDataService deviceDataService();

    public MdcReadingTypeUtilService mdcReadingTypeUtilService();

    public EngineService engineService();

}